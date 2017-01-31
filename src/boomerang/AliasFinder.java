package boomerang;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Stopwatch;
import com.sun.istack.internal.Nullable;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardProblem;
import boomerang.backward.BackwardSolver;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.cache.ResultCache.NoContextRequesterQueryCache;
import boomerang.cache.ResultCache.RecursiveQueryCache;
import boomerang.cache.ResultCache.WithContextRequesterQueryCache;
import boomerang.context.ContextResolver;
import boomerang.context.IContextRequester;
import boomerang.context.NoContextRequester;
import boomerang.debug.BoomerangDebugger;
import boomerang.forward.ForwardProblem;
import boomerang.forward.ForwardSolver;
import boomerang.pointsofindirection.BackwardBackwardHandler;
import boomerang.pointsofindirection.BackwardForwardHandler;
import boomerang.pointsofindirection.PointOfIndirection;
import grph.Grph;
import heros.solver.Pair;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AliasFinder {

	public final static SootField ARRAY_FIELD = new SootField("array", RefType.v("java.lang.Object")) {
		@Override
		public String toString() {
			return "ARRAY";
		}
	};

	private static final boolean DEBUG = false;

	/**
	 * General context object, this object is globally accessible for the entire
	 * analysis, i.e. the ICFG is stored within this context.
	 */
	public BoomerangContext context;

	/**
	 * Methods to be ignored by the analysis.
	 */
	public static Set<SootMethod> IGNORED_METHODS = new HashSet<SootMethod>();

	/**
	 * Holds the System.arraycopy method and is used within the flow functions
	 * to describe aliases flow upon calling this method.
	 */
	public static SootMethod ARRAY_COPY;

	/**
	 * Constructs an AliasFinder with the provided interprocedural control flow
	 * graph.
	 * 
	 * @param cfg
	 *            the interprocedural control flow graph.
	 */
	public AliasFinder(IInfoflowCFG cfg) {
		this(cfg, new BackwardsInfoflowCFG(cfg), new BoomerangOptions());
	}

	public AliasFinder(IInfoflowCFG cfg, BoomerangOptions options) {
		this(cfg, new BackwardsInfoflowCFG(cfg), options);
	}

	public AliasFinder(IInfoflowCFG cfg, BackwardsInfoflowCFG bwcfg, BoomerangOptions options) {
		this.context = new BoomerangContext(cfg, bwcfg, options);
    if (DEBUG)
      context.debugger = new BoomerangDebugger();
	}
	/**
	 * Constructs an AliasFinder with the provided context. The context carries
	 * global information, such as the call graph.
	 * 
	 * @param context
	 *            Context object holding information for the overall analysis.
	 */
	public AliasFinder(BoomerangContext context) {
		this.context = context;
    if (DEBUG)
      context.debugger = new BoomerangDebugger();
	}

	/**
	 * This method triggers a query for the provided local variable with
	 * specified type (can be null). Additionally, the first field of the access
	 * graph has to be constructed. The context to be searched for is the
	 * NoContextRequester, thus all aliases to be considered will be within the
	 * method of param stmt (or any of its transitively callees). Thus aliases
	 * with occur due to aliasing previously to any call site of the method of
	 * stmt are ignored.
	 * 
	 * @param local
	 *            The base variable of the access graph to search for
	 * @param type
	 *            Its type
	 * @param field
	 *            The first field (can be null)
	 * @param stmt
	 *            The statement at which the query for aliases should be
	 *            triggered.
	 * @return A multimap where the keys represent the allocation site and the
	 *         associated values are sets of access graph pointing to the
	 *         particular allocation site.
	 */
	public AliasResults findAliasAtStmt(Local local, Type t, WrappedSootField field, Unit stmt) {
		return findAliasAtStmt(local, t, field, stmt, new NoContextRequester());
	}

	/**
	 * This method triggers a query for the provided local variable with
	 * specified type (can be null). Additionally, the first field of the access
	 * graph has to be constructed. The context to be searched within is
	 * specified within the IContextRequestor.
	 * 
	 * @param local
	 *            The base variable of the access graph to search for
	 * @param type
	 *            Its type
	 * @param field
	 *            The first field (can be null)
	 * @param stmt
	 *            The statement at which the query for aliases should be
	 *            triggered.
	 * @param req
	 *            A IContextRequestor specifying under which call stack to be
	 *            looking for aliases.
	 * @return A multimap where the keys represent the allocation site and the
	 *         associated values are sets of access graph pointing to the
	 *         particular allocation site.
	 */
	public AliasResults findAliasAtStmt(Local local, Type type, @Nullable WrappedSootField field, Unit stmt,
			IContextRequester req) {
		AccessGraph ap;
		if (field == null) {
			ap = new AccessGraph(local, type);
		} else {
			ap = new AccessGraph(local, type, field);
		}
		return findAliasAtStmt(ap, stmt, req);
	}

	/**
	 * This method triggers a query for the provided {@link AccessGraph} at the
	 * given statement. The context to be searched for is the
	 * NoContextRequester, thus all aliases to be considered will be within the
	 * method of param stmt (or any of its transitively callees). Thus aliases
	 * with occur due to aliasing previously to any call site of the method of
	 * stmt are ignored.
	 * 
	 * @param ap
	 *            An access graph for which aliases should be searched
	 * @param stmt
	 *            The statement at which the query for aliases should be
	 *            triggered.
	 * @return A multimap where the keys represent the allocation site and the
	 *         associated values are sets of access graph pointing to the
	 *         particular allocation site.
	 */
	public AliasResults findAliasAtStmt(AccessGraph ap, Unit stmt) {
		return findAliasAtStmt(ap, stmt, new NoContextRequester());
	}

	/**
	 * This method triggers a query for the provided access graph at the given
	 * statement. The context to be searched for is specified within the
	 * IContextRequestor.
	 * 
	 * @param ap
	 *            An access graph for which aliases should be searched
	 * @param stmt
	 *            The statement at which the query for aliases should be
	 *            triggered.
	 * @param req
	 *            A IContextRequestor specifying under which call stack to be
	 *            looking for aliases.
	 * @return A multimap where the keys represent the allocation site and the
	 *         associated values are sets of access graph pointing to the
	 *         particular allocation site.
	 */
	private AliasResults internalFindAliasAtStmt(Query q, IContextRequester req) {
		if (context.startTime == null)
			throw new RuntimeException("Call startQuery() before triggering a Query!");
		Unit stmt = q.getStmt();
		AccessGraph ap = q.getAp();
		assert stmt != null;
		context.validateInput(ap, stmt);
		Grph.useCache = false;
		NoContextRequesterQueryCache cache = context.querycache.contextlessQueryCache();
		if (context.isOutOfBudget()) {
			throw new BoomerangTimeoutException();
		}
		if (stmt instanceof ThrowStmt)
			return new AliasResults();

		if (cache.isDone(q)) {
			sanityCheck(cache.getResults(q), stmt);
			if (req instanceof NoContextRequester)
				return cache.getResults(q);
			context.debugger.onLoadingQueryFromCache(q, cache.getResults(q));
			return resolveContext(stmt, req, cache.getResults(q), q);
		}

		if (cache.isProcessing(q)) {
			context.debugger.onCurrentlyProcessingRecursiveQuery(q);
			context.setRecursive(q);
			return new AliasResults();
		}
		cache.start(q);

		context.push(new SubQueryContext(q, context, (context.getSubQuery() != null ? context.getSubQuery() : null)));
		context.debugger.startQuery(q);
		AliasResults res = fixpointIteration();
		if (context.isRecursive(q)) {
			res = addStars(res);
		}
		cache.setResults(q, res);
		sanityCheck(cache.getResults(q), stmt);
		context.debugger.finishedQuery(q, res);
		context.pop();
		if (!(req instanceof NoContextRequester))
			res = resolveContext(stmt, req, res, q);

		return res;
	}

	public AliasResults findAliasAtStmt(AccessGraph ap, Unit stmt, IContextRequester req) {
		AliasResults res = null;
		Query q = new Query(ap, stmt, context.icfg.getMethodOf(stmt));
		try {
			res = internalFindAliasAtStmt(q, req);
		} catch (BoomerangTimeoutException e) {
			if (context.size() == 0)
				context.debugger.onAliasTimeout(q);
			throw new BoomerangTimeoutException();
		} finally {
			if (context.size() == 0)
				context.debugger.onAliasQueryFinished(q, res);
		}
		return res;
	}

	private AliasResults addStars(AliasResults res) {
		AliasResults withStar = new AliasResults();

		for (Entry<Pair<Unit, AccessGraph>, AccessGraph> e : res.entries()) {
			AccessGraph g = e.getValue();
			withStar.put(e.getKey(), g);
			for (AccessGraph alias : res.mayAliasSet()) {
				if (alias.getFieldCount() == 0) {
					withStar.put(e.getKey(), g.deriveWithNewLocal(alias.getBase(), alias.getBaseType()));
				}
			}

			if (g.getFieldCount() > 0) {
				withStar.put(e.getKey(), g.appendGraph(g.getFieldGraph()));
			}
		}
		return withStar;
	}

	/**
	 * Enables or disables type checking on the access graph. When accesses are
	 * appended to an access graph, a check is performed by evaluating the type
	 * of the fields if the access graph might actually exist.
	 * 
	 * @param val
	 *            Boolean value to enable or disable type checking.
	 */
	public static void setTypeChecking(boolean val) {
		WrappedSootField.TRACK_TYPE = val;
	}

	/**
	 * Computes aliases of access graphs recursive. That is, if supplying a.f.g
	 * The analysis will first search for aliases of a, then append .f to all of
	 * theses. Another search for all aliases of theses constructed access graph
	 * is performed. Then, .g is appended to all of theses. This is done until a
	 * fixed point is reached. The computation is performed with
	 * {@link NoContextRequester}.
	 * 
	 * @param a
	 *            Access graph to perform the operation on.
	 * @param stmt
	 *            The statement at which the query is triggered.
	 * @return Set of access graph which alias to a at stmt.
	 */
	public Set<AccessGraph> findAliasAtStmtRec(AccessGraph a, Unit stmt) {
		Query query = new Query(a, stmt, context.icfg.getMethodOf(stmt));
		RecursiveQueryCache cache = context.querycache.recursiveQueryCache();
		if (cache.isDone(query))
			return cache.getResults(query);
		if (cache.isProcessing(query))
			return Collections.emptySet();
		cache.start(query);
		Set<AccessGraph> out = findAliasAtStmtRec(a, stmt, new NoContextRequester());
		cache.setResults(query, new HashSet<>(out));
		return out;
	}

	/**
	 * Computes aliases of access graphs recursive. That is, if supplying a.f.g
	 * The analysis will first search for aliases of a, then append .f to all of
	 * theses. Another search for all aliases of theses constructed access graph
	 * is performed. Then, .g is appended to all of theses. This is done until a
	 * fixed point is reached.
	 * 
	 * @param a
	 *            Access graph to perform the operation on.
	 * @param stmt
	 *            The statement at which the query is triggered.
	 * @param requester
	 *            Provides the context under which the query is to be evaluted.
	 * @return Set of access graph which alias to a at stmt.
	 */
	public Set<AccessGraph> findAliasAtStmtRec(AccessGraph a, Unit stmt, IContextRequester requester) {
		if (context.isOutOfBudget()) {
			throw new BoomerangTimeoutException();
		}
		AccessGraph askFor = new AccessGraph(a.getBase(), a.getBaseType());
		if (!context.isValidQuery(askFor, stmt))
			return Collections.emptySet();
		Set<AccessGraph> prevAliases = internalFindAliasAtStmt(new Query(askFor, stmt, context.icfg.getMethodOf(stmt)),
				requester).mayAliasSet();
		if (a.getFieldCount() < 1) {
			return prevAliases;
		}
		WrappedSootField[] nodes = a.getRepresentative();

		Set<AccessGraph> out = new HashSet<>();

		out.addAll(AliasResults.appendFields(prevAliases, nodes, context));
		Set<AccessGraph> changeSet = prevAliases;
		for (int i = 0; i < nodes.length; i++) {
			Set<AccessGraph> withFieldsSet = AliasResults.appendField(changeSet, nodes[i], context);
      if (i != nodes.length) {
				WrappedSootField[] subFields = new WrappedSootField[i + 1];
				System.arraycopy(nodes, 0, subFields, 0, i + 1);
				AccessGraph original = new AccessGraph(a.getBase(), a.getBaseType(), subFields);
				withFieldsSet.add(original);
			}

			prevAliases = askForEach(withFieldsSet, stmt, requester);
			WrappedSootField[] subFields = new WrappedSootField[nodes.length - i - 1];
			System.arraycopy(nodes, i + 1, subFields, 0, nodes.length - i - 1);
			if (subFields.length > 0) {
				changeSet = new HashSet<>();
				for (AccessGraph alias : prevAliases) {
					if (AliasResults.canAppend(alias, subFields[0])) {
						AccessGraph appendFields = alias.appendFields(subFields);
						if (out.contains(appendFields))
							continue;
						out.add(appendFields);
						changeSet.add(alias);
					}
				}
			} else {
				out.addAll(prevAliases);
			}
		}
		return out;
	}

	private AliasResults fixpointIteration() {
		if (context.getSubQuery() == null)
			return new AliasResults();
		Unit stmt = context.getSubQuery().getStmt();
		AccessGraph accessPath = context.getSubQuery().getAccessPath();

		BackwardProblem problem = new BackwardProblem(context);
		BackwardSolver backwardsolver = new BackwardSolver(problem, context);
		context.addSolver(backwardsolver);
		backwardsolver.startPropagation(accessPath, stmt);
		while (!context.getSubQuery().isEmpty()) {
			if (context.isOutOfBudget()) {
				throw new BoomerangTimeoutException();
			}
			PointOfIndirection first = context.getSubQuery().removeFirst();

			processPOI(first, backwardsolver);
		}
		if (Thread.interrupted()) {
			context.forceTerminate();
			return new AliasResults();
		}
		context.removeSolver(backwardsolver);
		AliasResults res = new AliasResults();
		res.putAll(context.getForwardPathEdges().getResultAtStmtContainingValue(stmt, accessPath));

		AliasResults aliasRes = new AliasResults(res);
		return aliasRes;
	}

	private void processPOI(PointOfIndirection poi, BackwardSolver backwardsolver) {
		if (poi instanceof BackwardForwardHandler) {
			ForwardSolver solver = createNewForwardSolver();
			((BackwardForwardHandler) poi).execute(solver, context);
			context.removeSolver(solver);
		} else if (poi instanceof BackwardBackwardHandler) {
			((BackwardBackwardHandler) poi).execute(backwardsolver, context);
		} else {
			throw new RuntimeException("Not supported type of POI");
		}

	}

	private ForwardSolver createNewForwardSolver() {
		ForwardProblem forwardProblem = new ForwardProblem(context);
		ForwardSolver solver = new ForwardSolver(forwardProblem, context);
		context.addSolver(solver);
    context.setCurrentForwardSolver(solver);
		return solver;
	}

	private AliasResults resolveContext(Unit stmt, IContextRequester req, AliasResults res, Query q) {
		Pair<Query, AliasResults> pair = new Pair<Query, AliasResults>(q, res);
		WithContextRequesterQueryCache cache = context.querycache.resolvedCache();
		if (cache.isDone(pair)) {
			return cache.getResults(pair);
		}
		if (cache.isProcessing(pair)) {
			return new AliasResults();
		}
		cache.start(pair);
		ContextResolver contextResolver = new ContextResolver(req, q, context);

		res = contextResolver.resolve(res, stmt);
		cache.setResults(pair, new AliasResults(res));
		return res;
	}

	private void sanityCheck(AliasResults res, Unit stmt) {
		SootMethod methodOf = context.icfg.getMethodOf(stmt);
		for (AccessGraph a : res.values()) {
			if (a.isStatic())
				continue;
			assert methodOf.getActiveBody().getLocals().contains(a.getBase());
		}
		for (Pair<Unit, AccessGraph> key : res.keySet()) {
			assert methodOf.getActiveBody().getUnits().contains(key
					.getO1()) : "The allocation sites should always be local to a method, when no context was resolved!";
		}
	}

	private Set<AccessGraph> askForEach(Collection<AccessGraph> collection, Unit stmt, IContextRequester requestor) {
		Set<AccessGraph> res = new HashSet<>();
		for (AccessGraph a : collection) {
			if (context.isOutOfBudget()) {
				throw new BoomerangTimeoutException();
			}
			if (!context.icfg.accessesField(context.icfg.getMethodOf(stmt), a.getFirstField().getField()))
				res.add(a);
			else
				res.addAll(
						internalFindAliasAtStmt(new Query(a.clone(), stmt, context.icfg.getMethodOf(stmt)), requestor)
								.mayAliasSet());
		}
		return res;
	}

	public void startQuery() {
		context.startTime = Stopwatch.createStarted();
	}
}
