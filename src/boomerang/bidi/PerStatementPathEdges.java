package boomerang.bidi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.AliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

class PerStatementPathEdges {
	private Multimap< AccessGraph, Pair<Unit, AccessGraph>> forwardPathEdges = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, AccessGraph> reversePathEdges = HashMultimap.create();
	private Set<IPathEdge<Unit, AccessGraph>> processedPathEdges = new HashSet<>();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> targetToPOI = HashMultimap.create();
	private Multimap<AccessGraph, PointOfIndirection> originToPOI = HashMultimap.create();
	private Multimap<AccessGraph, PointOfIndirection> parameterOriginToPOI = HashMultimap.create();
	private Multimap<PointOfIndirection, AliasCallback> poisToCallback = HashMultimap.create();
	private Set<PointOfIndirection> pois = new HashSet<>();
	private final BoomerangContext context;
	private Direction direction;
	private SootMethod method;
	public PerStatementPathEdges(BoomerangContext context, Direction direction,SootMethod method) {
		this.context = context;
		this.direction = direction;
		this.method = method;
	}

	void register(IPathEdge<Unit, AccessGraph> pe) {
		Pair<Unit, AccessGraph> typeLessBackwardNode = new Pair<Unit, AccessGraph>(pe.getTarget(),
				pe.factAtTarget().noType());
		forwardPathEdges.put(pe.factAtSource(), pe.getTargetNode());
		reversePathEdges.put(typeLessBackwardNode, pe.factAtSource());
		if (!processedPathEdges.add(pe))
			return;
		if(direction == Direction.BACKWARD)
			return;
		for (PointOfIndirection p : targetToPOI.get(typeLessBackwardNode)) {
			registerPOIWithTarget(typeLessBackwardNode, p);
		}
		for (PointOfIndirection p : originToPOI.get(pe.factAtSource())) {
			for (AliasCallback cb : poisToCallback.get(p)) {
				cb.newAliasEncountered(p, pe.factAtTarget(),pe.factAtSource());
			}
		}
		if (!pe.factAtSource().hasAllocationSite()) {
			for (Entry<AccessGraph, PointOfIndirection> e : parameterOriginToPOI.entries()) {
				PointOfIndirection p = e.getValue();
				// TODO add check only if type match
				if (aliasInContext(e.getKey(), pe.factAtSource())) {
					for (AliasCallback cb : poisToCallback.get(p)) {
						cb.newAliasEncountered(p, pe.factAtTarget(),pe.factAtSource());
					}
				}
			}
		}
	}

	private boolean aliasInContext(AccessGraph potentialAlias1, AccessGraph potentialAlias2) {
		return aliasInContext(method, potentialAlias1, potentialAlias2, new HashSet<SootMethod>());
	}

	Map<CacheKey, Boolean> cache = new HashMap<>();
	private class CacheKey{
		private SootMethod startNodeOfCallee;
		private AccessGraph potentialAlias1;
		private AccessGraph potentialAlias2;

		public CacheKey(SootMethod startNodeOfCallee, AccessGraph potentialAlias1, AccessGraph potentialAlias2) {
			this.startNodeOfCallee = startNodeOfCallee;
			this.potentialAlias1 = potentialAlias1;
			this.potentialAlias2 = potentialAlias2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((potentialAlias1 == null) ? 0 : potentialAlias1.hashCode());
			result = prime * result + ((potentialAlias2 == null) ? 0 : potentialAlias2.hashCode());
			result = prime * result + ((startNodeOfCallee == null) ? 0 : startNodeOfCallee.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (potentialAlias1 == null) {
				if (other.potentialAlias1 != null)
					return false;
			} else if (!potentialAlias1.equals(other.potentialAlias1))
				return false;
			if (potentialAlias2 == null) {
				if (other.potentialAlias2 != null)
					return false;
			} else if (!potentialAlias2.equals(other.potentialAlias2))
				return false;
			if (startNodeOfCallee == null) {
				if (other.startNodeOfCallee != null)
					return false;
			} else if (!startNodeOfCallee.equals(other.startNodeOfCallee))
				return false;
			return true;
		}
	}
	
	private boolean aliasInContext(SootMethod method, AccessGraph potentialAlias1, AccessGraph potentialAlias2, Set<SootMethod> visited) {
		if(!visited.add(method))
			return false;
		if(potentialAlias1.hasSetBasedFieldGraph() || potentialAlias2.hasSetBasedFieldGraph())
			return false;
		CacheKey key = new CacheKey(method,potentialAlias1,potentialAlias2);
		if(cache.containsKey(key))
			return cache.get(key);
		key = new CacheKey(method,potentialAlias2,potentialAlias1);
		if(cache.containsKey(key))
			return cache.get(key);
		Set<? extends IPathEdge<Unit, AccessGraph>> pathEdges1 = context
				.getForwardIncomings( potentialAlias1,method);
		Set<? extends IPathEdge<Unit, AccessGraph>> pathEdges2 = context
				.getForwardIncomings( potentialAlias2,method);
		for (IPathEdge<Unit, AccessGraph> edge1 : pathEdges1) {
			for (IPathEdge<Unit, AccessGraph> edge2 : pathEdges2) {
				SootMethod callerMethod1 = context.icfg.getMethodOf(edge1.getTarget());
				SootMethod callerMethod2 = context.icfg.getMethodOf(edge2.getTarget());
				if (edge1.factAtSource().equals(edge2.factAtSource())) {
					cache.put(new CacheKey(method,potentialAlias1,potentialAlias2), true);
					return true;
				}
				if (!edge1.factAtSource().hasAllocationSite() && !edge1.factAtSource().hasAllocationSite()
						&&  callerMethod1.equals(callerMethod2)) {
					boolean res = aliasInContext(callerMethod1, edge1.factAtSource(), edge2.factAtSource(),visited);
					cache.put(new CacheKey(callerMethod1,potentialAlias1,potentialAlias2), res);
					return res;
				}
			}
		}
		cache.put(new CacheKey(method,potentialAlias1,potentialAlias2), false);
		return false;
	}

	
	
	public void registerPointOfIndirectionAt(PointOfIndirection poi, AliasCallback callback) {
		Pair<Unit, AccessGraph> aliasTarget = poi.getTarget();
		if (poisToCallback.put(poi, callback))
			executeCallback(aliasTarget, poi, callback);
		if (pois.add(poi)) {
			if (targetToPOI.put(aliasTarget, poi)) {
				poi.sendBackward();
			}

		}
	}

	private void executeCallback(Pair<Unit, AccessGraph> aliasTarget, PointOfIndirection poi, AliasCallback cb) {
		for (AccessGraph origin : reversePathEdges.get(aliasTarget)) {
			for (Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)) {
				cb.newAliasEncountered(poi, aliases.getO2(),origin);
			}
			if (!origin.hasAllocationSite()) {
				// TODO Check all existing path edges with no origin if they
				// alias.
				for (AccessGraph existingPathEdgeOrigin : forwardPathEdges.keySet()) {
					if (!existingPathEdgeOrigin.hasNullAllocationSite()) {
						if (!aliasInContext(origin, existingPathEdgeOrigin))
							continue;
						for(Pair<Unit,AccessGraph> target : forwardPathEdges.get(existingPathEdgeOrigin)){
							cb.newAliasEncountered(poi, target.getO2(),existingPathEdgeOrigin);
						}
					}
				}
			}
		}
	}

	private void registerPOIWithTarget(Pair<Unit, AccessGraph> aliasTarget, PointOfIndirection poi) {
		for (AccessGraph origin : reversePathEdges.get(aliasTarget)) {
			if (originToPOI.put(origin, poi)) {
				for (Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)) {
					for (AliasCallback cb : poisToCallback.get(poi)) {
						cb.newAliasEncountered(poi, aliases.getO2(),origin);
					}
				}
			}
			if (!origin.hasAllocationSite()) {
				parameterOriginToPOI.put(origin, poi);
				// TODO Check all existing path edges with no origin if they
				// alias.
				for (AccessGraph existingPathEdgeOrigin : forwardPathEdges.keySet()) {
					if (!existingPathEdgeOrigin.hasNullAllocationSite()) {
						if (!aliasInContext(origin, existingPathEdgeOrigin))
							continue;
						for (AliasCallback cb : poisToCallback.get(poi)) {
							for(Pair<Unit,AccessGraph> target : forwardPathEdges.get(existingPathEdgeOrigin)){
								cb.newAliasEncountered(poi, target.getO2(),existingPathEdgeOrigin);
							}
						}
					}
				}
			}
		}
	}

	Multimap<AccessGraph, AccessGraph> getResultsAtStmtContainingValue(Unit stmt, AccessGraph fact,
			Set<Pair<Unit, AccessGraph>> visited) {
		Pair<Unit, AccessGraph> visit = new Pair<Unit, AccessGraph>(stmt, fact);
		if (visited.contains(visit)) {
			return HashMultimap.create();
		}
		visited.add(visit);
		Multimap< AccessGraph, AccessGraph> pathEdges = HashMultimap.create();
		Pair<Unit, AccessGraph> o = new Pair<>(stmt, fact.noType());
		if (!reversePathEdges.containsKey(o))
			return HashMultimap.create();

		Collection<AccessGraph> matchingStarts = new HashSet<>();
		matchingStarts = reversePathEdges.get(o);
		for (AccessGraph start : matchingStarts) {
			Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges.get(start);
			for (Pair<Unit, AccessGraph> target : fwPair) {
				pathEdges.put(start, target.getO2());
			}
		}
		Multimap<AccessGraph, AccessGraph> out = HashMultimap.create();
		for (AccessGraph key : pathEdges.keySet()) {
			boolean maintainPathEdge = false;
			if (!key.hasAllocationSite()) {
				for (Unit callSite : context.icfg.getCallersOf(method)) {
//					maintainPathEdge |= !context.getContextRequester().continueAtCallSite(callSite, callee);
					Set<? extends IPathEdge<Unit, AccessGraph>> pathEdgesAtCallSite = context.getForwardIncomings(key, method);
					for (IPathEdge<Unit, AccessGraph> pathEdgeAtCallSite : pathEdgesAtCallSite) {
						Multimap<AccessGraph, AccessGraph> aliasesAtCallSite = context.getForwardPathEdges()
								.getResultAtStmtContainingValue(callSite, pathEdgeAtCallSite.factAtTarget(), visited);
						for (Entry<AccessGraph, AccessGraph> aliasEntry : aliasesAtCallSite.entries()) {
							Set<AccessGraph> withinCalleeFacts = context.getForwardTargetsFor(aliasEntry.getValue(),
									callSite, method);
							for (AccessGraph withinCalleeFact : withinCalleeFacts) {

								Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges
										.get( withinCalleeFact);
								for (Pair<Unit, AccessGraph> target : fwPair) {
									out.put(aliasEntry.getKey(), target.getO2());
								}
							}
						}
					}
				}
			} else {
				maintainPathEdge = true;
			}
			if (maintainPathEdge) {
				out.putAll(key, pathEdges.get(key));
			}
		}
		return out;
	}

	boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
		if (processedPathEdges.contains(pe))
			return true;
		return false;
	}

	int size() {
		return forwardPathEdges.size();
	}

//	void groupByStartUnit() {
//		for (Pair<Unit, AccessGraph> pe : forwardPathEdges.keySet()) {
//			System.out.println(pe + " :: " + forwardPathEdges.get(pe).size());
//			prettyPrint(forwardPathEdges.get(pe));
//		}
//	}

	private void prettyPrint(Collection<Pair<Unit, AccessGraph>> collection) {
		Multimap<Unit, AccessGraph> unitToAccessPath = HashMultimap.create();
		for (Pair<Unit, AccessGraph> p : collection) {
			unitToAccessPath.put(p.getO1(), p.getO2());
		}
		TreeMap<Integer, Unit> count = new TreeMap<>(Collections.reverseOrder());
		for (Unit u : unitToAccessPath.keys()) {
			count.put(unitToAccessPath.get(u).size(), u);
		}
		int i = 0;
		for (Entry<Integer, Unit> pe : count.entrySet()) {
			System.out.println("\t\t\t\t\t" + pe.getValue() + "    " + pe.getKey());
			i++;
			if (i > 10)
				break;

		}
	}

	void clear() {
		forwardPathEdges.clear();
		reversePathEdges.clear();
		processedPathEdges.clear();

		forwardPathEdges = null;
		reversePathEdges = null;
		processedPathEdges = null;
	}

}
