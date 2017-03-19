package boomerang;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Stopwatch;
import com.sun.istack.internal.Nullable;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.SetBasedFieldGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardSolver;
import boomerang.context.IContextRequester;
import boomerang.context.NoContextRequester;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IFDSSolver;
import heros.solver.Pair;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AliasFinder {

	public static boolean STRONG_UPDATES_FIELDS = true;
	public static boolean HANDLE_EXCEPTION_FLOW = true;
	public final static SootField ARRAY_FIELD = new SootField("array", RefType.v("java.lang.Object")) {
		@Override
		public String toString() {
			return "ARRAY";
		}
	};

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
	private static SootMethod ARRAY_COPY;

	public static SootMethod arrayCopy(){
		if(ARRAY_COPY == null)
		    try {
		      ARRAY_COPY =
		          Scene.v().getMethod(
		              "<java.lang.System: void arraycopy(java.lang.Object,int,java.lang.Object,int,int)>");
		    } catch (RuntimeException e) {

		    }
		return ARRAY_COPY;
	} 
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
		context.setContextRequester(req);
		if (stmt instanceof ThrowStmt)
			return new AliasResults();

		context.debugger.startQuery(q);
		AliasResults res = fixpointIteration(stmt,ap);
		context.debugger.finishedQuery(q, res);
		return res;
	}

	public AliasResults findAliasAtStmt(AccessGraph ap, Unit stmt, IContextRequester req) {

		AliasResults res = null;
		Query q = new Query(ap, stmt, context.icfg.getMethodOf(stmt));
		try {
			res = internalFindAliasAtStmt(q, req);
		} catch (BoomerangTimeoutException e) {
			throw new BoomerangTimeoutException();
		} finally {
			context.debugger.onAliasQueryFinished(q, res);
		}
		return res;
	}

	private AliasResults fixpointIteration(Unit stmt, AccessGraph accessGraph) {
		BackwardSolver backwardSolver = context.getBackwardSolver();
		ForwardSolver forwardSolver = context.getForwardSolver();
		boolean timedout = false;
		context.addAsVisitedBackwardMethod(context.icfg.getMethodOf(stmt));
		try{
			backwardSolver.startPropagation(accessGraph, stmt);
			backwardSolver.awaitExecution();
			while(!backwardSolver.isDone() || !forwardSolver.isDone()){
				forwardSolver.awaitExecution();
				backwardSolver.awaitExecution();
			}
		} catch(BoomerangTimeoutException e){
			timedout = true;
		}
		AliasResults res = new AliasResults();
		res.putAll(context.getForwardPathEdges().getResultAtStmtContainingValue(stmt, accessGraph, new HashSet<Pair<Unit,AccessGraph>>()));

		if(timedout)
			res.setTimedout();
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

	public void startQuery() {
		ARRAY_COPY = null;
		context.startTime = Stopwatch.createStarted();
		if (SetBasedFieldGraph.allFields != null)
			SetBasedFieldGraph.allFields.clear();
	}
}
