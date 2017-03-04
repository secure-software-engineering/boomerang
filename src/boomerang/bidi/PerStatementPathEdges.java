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
	private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> forwardPathEdges = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> reversePathEdges = HashMultimap.create();
	private Set<IPathEdge<Unit, AccessGraph>> processedPathEdges = new HashSet<>();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> targetToPOI = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> originToPOI = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> parameterOriginToPOI = HashMultimap.create();
	private Multimap<PointOfIndirection, AliasCallback> poisToCallback = HashMultimap.create();
	private Set<PointOfIndirection> pois = new HashSet<>();
	private final BoomerangContext context;
	private Direction direction;

	public PerStatementPathEdges(BoomerangContext context, Direction direction) {
		this.context = context;
		this.direction = direction;
	}

	void register(IPathEdge<Unit, AccessGraph> pe) {
		Pair<Unit, AccessGraph> typeLessBackwardNode = new Pair<Unit, AccessGraph>(pe.getTarget(),
				pe.factAtTarget().noType());
		forwardPathEdges.put(pe.getStartNode(), pe.getTargetNode());
		reversePathEdges.put(typeLessBackwardNode, pe.getStartNode());
		if (!processedPathEdges.add(pe))
			return;
		if(direction == Direction.BACKWARD)
			return;
		for (PointOfIndirection p : targetToPOI.get(typeLessBackwardNode)) {
			registerPOIWithTarget(typeLessBackwardNode, p);
		}
		for (PointOfIndirection p : originToPOI.get(pe.getStartNode())) {
			for (AliasCallback cb : poisToCallback.get(p)) {
				cb.newAliasEncountered(p, pe.factAtTarget());
			}
		}
		if (!pe.factAtSource().hasAllocationSite()) {
			for (Entry<Pair<Unit, AccessGraph>, PointOfIndirection> e : parameterOriginToPOI.entries()) {
				PointOfIndirection p = e.getValue();
				// TODO add check only if type match
				if (aliasInContext(pe.getStart(), e.getKey().getO2(), pe.factAtSource())) {
					for (AliasCallback cb : poisToCallback.get(p)) {
						cb.newAliasEncountered(p, pe.factAtTarget());
					}
				}
			}
		}
	}

	private boolean aliasInContext(Unit start, AccessGraph potentialAlias1, AccessGraph potentialAlias2) {
		return aliasInContext(start,potentialAlias1, potentialAlias2, new HashSet<Unit>());
	}

	Map<CacheKey, Boolean> cache = new HashMap<>();
	private class CacheKey{
		private Unit startNodeOfCallee;
		private AccessGraph potentialAlias1;
		private AccessGraph potentialAlias2;

		public CacheKey(Unit startNodeOfCallee, AccessGraph potentialAlias1, AccessGraph potentialAlias2) {
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
	
	private boolean aliasInContext(Unit startNodeOfCallee, AccessGraph potentialAlias1, AccessGraph potentialAlias2, Set<Unit> visited) {
		if(!visited.add(startNodeOfCallee))
			return false;
		if(potentialAlias1.hasSetBasedFieldGraph() || potentialAlias2.hasSetBasedFieldGraph())
			return false;
		CacheKey key = new CacheKey(startNodeOfCallee,potentialAlias1,potentialAlias2);
		if(cache.containsKey(key))
			return cache.get(key);
		key = new CacheKey(startNodeOfCallee,potentialAlias2,potentialAlias1);
		if(cache.containsKey(key))
			return cache.get(key);
		Set<? extends IPathEdge<Unit, AccessGraph>> pathEdges1 = context
				.getForwardIncomings(new Pair<Unit, AccessGraph>(startNodeOfCallee, potentialAlias1));
		Set<? extends IPathEdge<Unit, AccessGraph>> pathEdges2 = context
				.getForwardIncomings(new Pair<Unit, AccessGraph>(startNodeOfCallee, potentialAlias2));
		for (IPathEdge<Unit, AccessGraph> edge1 : pathEdges1) {
			for (IPathEdge<Unit, AccessGraph> edge2 : pathEdges2) {
				if (edge1.getStartNode().equals(edge2.getStartNode())) {
					cache.put(new CacheKey(startNodeOfCallee,potentialAlias1,potentialAlias2), true);
					return true;
				}
				if (!edge1.factAtSource().hasAllocationSite() && !edge1.factAtSource().hasAllocationSite()
						&& edge2.getStart().equals(edge1.getStart())) {
					boolean res = aliasInContext(edge1.getStart(), edge1.factAtSource(), edge2.factAtSource(),visited);
					cache.put(new CacheKey(startNodeOfCallee,potentialAlias1,potentialAlias2), res);
					return res;
				}
			}
		}
		cache.put(new CacheKey(startNodeOfCallee,potentialAlias1,potentialAlias2), false);
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
		for (Pair<Unit, AccessGraph> origin : reversePathEdges.get(aliasTarget)) {
			for (Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)) {
				cb.newAliasEncountered(poi, aliases.getO2());
			}
			if (!origin.getO2().hasAllocationSite()) {
				// TODO Check all existing path edges with no origin if they
				// alias.
				for (Pair<Unit, AccessGraph> existingPathEdgeOrigin : forwardPathEdges.keySet()) {
					if (!existingPathEdgeOrigin.getO2().hasNullAllocationSite()) {
						if (!aliasInContext(origin.getO1(), origin.getO2(), existingPathEdgeOrigin.getO2()))
							continue;
						for(Pair<Unit,AccessGraph> target : forwardPathEdges.get(existingPathEdgeOrigin)){
							cb.newAliasEncountered(poi, target.getO2());
						}
					}
				}
			}
		}
	}

	private void registerPOIWithTarget(Pair<Unit, AccessGraph> aliasTarget, PointOfIndirection poi) {
		for (Pair<Unit, AccessGraph> origin : reversePathEdges.get(aliasTarget)) {
			if (originToPOI.put(origin, poi)) {
				for (Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)) {
					for (AliasCallback cb : poisToCallback.get(poi)) {
						cb.newAliasEncountered(poi, aliases.getO2());
					}
				}
			}
			if (!origin.getO2().hasAllocationSite()) {
				parameterOriginToPOI.put(origin, poi);
				// TODO Check all existing path edges with no origin if they
				// alias.
				for (Pair<Unit, AccessGraph> existingPathEdgeOrigin : forwardPathEdges.keySet()) {
					if (!existingPathEdgeOrigin.getO2().hasNullAllocationSite()) {
						if (!aliasInContext(origin.getO1(), origin.getO2(), existingPathEdgeOrigin.getO2()))
							continue;
						for (AliasCallback cb : poisToCallback.get(poi)) {
							for(Pair<Unit,AccessGraph> target : forwardPathEdges.get(existingPathEdgeOrigin)){
								cb.newAliasEncountered(poi, target.getO2());
							}
						}
					}
				}
			}
		}
	}

	Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultsAtStmtContainingValue(Unit stmt, AccessGraph fact,
			Set<Pair<Unit, AccessGraph>> visited) {
		Pair<Unit, AccessGraph> visit = new Pair<Unit, AccessGraph>(stmt, fact);
		if (visited.contains(visit)) {
			return HashMultimap.create();
		}
		visited.add(visit);
		Multimap<Pair<Unit, AccessGraph>, AccessGraph> pathEdges = HashMultimap.create();
		Pair<Unit, AccessGraph> o = new Pair<>(stmt, fact.noType());
		if (!reversePathEdges.containsKey(o))
			return HashMultimap.create();

		Collection<Pair<Unit, AccessGraph>> matchingStarts = new HashSet<>();
		matchingStarts = reversePathEdges.get(o);
		for (Pair<Unit, AccessGraph> start : matchingStarts) {
			Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges.get(start);
			for (Pair<Unit, AccessGraph> target : fwPair) {
				pathEdges.put(start, target.getO2());
			}
		}
		Multimap<Pair<Unit, AccessGraph>, AccessGraph> out = HashMultimap.create();
		for (Pair<Unit, AccessGraph> key : pathEdges.keySet()) {
			Unit pathEdgeStart = key.getO1();
			SootMethod callee = context.icfg.getMethodOf(pathEdgeStart);
			boolean maintainPathEdge = false;
			if (context.icfg.getStartPointsOf(callee).contains(pathEdgeStart)) {
				for (Unit callSite : context.icfg.getCallersOf(callee)) {
					maintainPathEdge |= !context.getContextRequester().continueAtCallSite(callSite, callee);
					Set<? extends IPathEdge<Unit, AccessGraph>> pathEdgesAtCallSite = context.getForwardIncomings(key);
					for (IPathEdge<Unit, AccessGraph> pathEdgeAtCallSite : pathEdgesAtCallSite) {
						Multimap<Pair<Unit, AccessGraph>, AccessGraph> aliasesAtCallSite = context.getForwardPathEdges()
								.getResultAtStmtContainingValue(callSite, pathEdgeAtCallSite.factAtTarget(), visited);
						for (Entry<Pair<Unit, AccessGraph>, AccessGraph> aliasEntry : aliasesAtCallSite.entries()) {
							Set<AccessGraph> withinCalleeFacts = context.getForwardTargetsFor(aliasEntry.getValue(),
									callSite, callee);
							for (AccessGraph wihinCalleeFact : withinCalleeFacts) {

								Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges
										.get(new Pair<Unit, AccessGraph>(pathEdgeStart, wihinCalleeFact));
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

	void groupByStartUnit() {
		for (Pair<Unit, AccessGraph> pe : forwardPathEdges.keySet()) {
			System.out.println(pe + " :: " + forwardPathEdges.get(pe).size());
			prettyPrint(forwardPathEdges.get(pe));
		}
	}

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
