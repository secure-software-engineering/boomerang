package boomerang.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import heros.solver.Pair;
import soot.Scene;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewArrayExpr;

/**
 * The context graph holds the context independent {@link AliasResults} at each
 * node. Each node is of type {@link Context}. Each context has alias results
 * associated which it. These are then pushed up and down along the edges of the
 * graph, until a fixed point is reached.
 * 
 * @author Johannes Späth
 *
 */
class ContextGraph {
	private Multimap<Context, Context> calleeToCaller = HashMultimap.create();
	private Multimap<Context, Context> callerToCallee = HashMultimap.create();
	private Map<Context, Integer> toInitialContextDistance = new HashMap<>();

	private Map<Context, AliasResults> stmtToResults = new HashMap<>();
	private IContextRequester contextRequestor;
	private BoomerangContext dartcontext;
	private Query query;
	private Context initialContext;

	ContextGraph(IContextRequester req, BoomerangContext dartcontext, Query query, Context initialContext) {
		this.contextRequestor = req;
		this.dartcontext = dartcontext;
		this.query = query;
		this.initialContext = initialContext;
		toInitialContextDistance.put(initialContext, 0);
	}

	Collection<Context> getCaller(Context callee) {
		if (!calleeToCaller.containsKey(callee))
			expandGraph(callee);
		return calleeToCaller.get(callee);
	}

	Collection<Context> getCallee(Context caller) {
		return callerToCallee.get(caller);
	}

	boolean isInitialContext(Context c) {
		return initialContext.equals(c);
	}

	/**
	 * This method stores the provided results at the context. For each newly
	 * added pair of entries of the @param res, an {@link IWorklistEntry} is
	 * generated, if there must be more post-processing be done. (E.g. there is
	 * still missing information of an access graph with a parameter (or this)
	 * as base).
	 * 
	 * @param context
	 *            The context at which the results are stored.
	 * @param res
	 *            The alias result to store at the context.
	 * @return The entries which have to be added to the worklist.
	 */
	Set<IWorklistEntry> storeResults(Context context, AliasResults res) {
		if (dartcontext.isOutOfBudget())
			throw new BoomerangTimeoutException();
		if (res.isEmpty())
			return Collections.emptySet();
		AliasResults alreadyAvailable = stmtToResults.get(context);
		Set<IWorklistEntry> out = new HashSet<>();
		if (alreadyAvailable == null) {
			stmtToResults.put(context, new AliasResults(res));
			ParamAllocWorklistEntry params = new ParamAllocWorklistEntry(context, new HashSet<>(res.keySet()),
					contextRequestor, dartcontext);
			AliasWorklistEntry entries = new AliasWorklistEntry(context, new AliasResults(res), contextRequestor,
					dartcontext);
			out.add(params);
			out.add(entries);
			return out;
		}

		// Get difference of keys
		Set<Pair<Unit, AccessGraph>> oldKeys = alreadyAvailable.keySet();
		Set<Pair<Unit, AccessGraph>> newKeys = new HashSet<>(res.keySet());
		newKeys.removeAll(oldKeys);

		boolean putAll = alreadyAvailable.putAll(res);
		stmtToResults.put(context, alreadyAvailable);

		if (!putAll) {
			return Collections.emptySet();
		}

		ParamAllocWorklistEntry params = new ParamAllocWorklistEntry(context, newKeys, contextRequestor, dartcontext);
		AliasWorklistEntry entries = new AliasWorklistEntry(context, new AliasResults(res), contextRequestor,
				dartcontext);
		out.add(params);
		out.add(entries);
		return out;
	}

	private void expandGraph(Context callee) {
		Collection<Context> callSiteOf = contextRequestor.getCallSiteOf(callee);
    Integer integer = toInitialContextDistance.get(callee);
    Integer distance = integer++;
		for (Context caller : callSiteOf) {
			this.callerToCallee.put(caller, callee);
			this.calleeToCaller.put(callee, caller);
      toInitialContextDistance.put(caller, distance);
		}
	}

	public String toString() {
		String str = "";
		for (Context callee : calleeToCaller.keySet()) {
			str += callee.toString() + "\n";
			Collection<Context> collection = calleeToCaller.get(callee);
			for (Context caller : collection) {
				str += "\t -> " + caller.toString() + "\n";
			}

		}
		str += Joiner.on("\n").withKeyValueSeparator("\n\t :::: ").join(stmtToResults);

		return str;
	}

	public Map<Context, AliasResults> getResultGraph() {
		return stmtToResults;
	}

	private AliasResults mustContainQuery(AliasResults intermediateResults) {
		AliasResults out = new AliasResults();
		for (Pair<Unit, AccessGraph> k : intermediateResults.keySet()) {
			Collection<AccessGraph> collection = intermediateResults.get(k);
			if (collection.contains(query.getAp())) {
				out.putAll(k, collection);
			}
		}
		return out;
	}

	AliasResults extractResults(Context initialContext) {
		AliasResults out = new AliasResults();
		Map<Context, AliasResults> resultGraph = getResultGraph();
		for (Context u : resultGraph.keySet()) {
			AliasResults res = resultGraph.get(u);
			for (Pair<Unit, AccessGraph> k : res.keySet()) {
				if (k.getO2().hasAllocationSite() && (isArrayAlloc(k.getO2().getSourceStmt())
						|| Scene.v().getFastHierarchy().canStoreType(k.getO2().getType(), query.getType()))) {
					out.putAll(k, conntectedResult(u, res.get(k), initialContext,
							HashMultimap.<Context, AccessGraph> create()));
				}
			}
		}
		return mustContainQuery(out);
	}

	private boolean isArrayAlloc(Unit sourceStmt) {
		if (sourceStmt instanceof AssignStmt) {
			AssignStmt as = (AssignStmt) sourceStmt;
			if (as.getRightOp() instanceof NewArrayExpr)
				return true;
		}
		return false;
	}

	private Collection<AccessGraph> conntectedResult(Context currContext, Collection<AccessGraph> collection,
			Context initialContext, Multimap<Context, AccessGraph> visited) {
		if (currContext.equals(initialContext))
			return collection;
		if (visited.containsKey(currContext) && visited.get(currContext).containsAll(collection))
			return Collections.emptySet();
		visited.putAll(currContext, collection);
		Set<AccessGraph> out = new HashSet<>();
		for (Context calleeContext : getCallee(currContext)) {
			for (AccessGraph a : collection) {
				Set<AccessGraph> forwardTargetsFor = ContextResolver.getForwardTargetsFor(a, currContext.getStmt(),
						dartcontext.icfg.getMethodOf(calleeContext.getStmt()), dartcontext);
				AliasResults res = getResultGraph().get(calleeContext);
				if (res != null) {
					for (Pair<Unit, AccessGraph> sp : res.keySet()) {
						if (forwardTargetsFor.contains(sp.getO2())) {
							out.addAll(conntectedResult(calleeContext, res.get(sp), initialContext, visited));
						}
					}
				}
			}
		}

		return out;
	}
}
