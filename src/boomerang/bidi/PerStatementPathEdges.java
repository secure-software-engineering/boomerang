package boomerang.bidi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.AliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.NopStmt;

class PerStatementPathEdges {
	private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> forwardPathEdges = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> reversePathEdges = HashMultimap.create();
	private Set<IPathEdge<Unit, AccessGraph>> processedPathEdges = new HashSet<>();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> targetToPOI = HashMultimap.create();
	private Multimap<Pair<Unit, AccessGraph>, PointOfIndirection> originToPOI = HashMultimap.create();
	private Multimap<PointOfIndirection, AliasCallback> poisToCallback = HashMultimap.create();
	private Set<PointOfIndirection> pois = new HashSet<>();
	private final BoomerangContext context;

	public PerStatementPathEdges(BoomerangContext context) {
		this.context = context;
	}

	void register(IPathEdge<Unit, AccessGraph> pe) {
		Pair<Unit,AccessGraph> typeLessBackwardNode = new Pair<Unit,AccessGraph>(pe.getTarget(),pe.factAtTarget().noType());
		forwardPathEdges.put(pe.getStartNode(), pe.getTargetNode());
		reversePathEdges.put(typeLessBackwardNode, pe.getStartNode());
		if(!processedPathEdges.add(pe))
			return;
		for (PointOfIndirection p : targetToPOI.get(typeLessBackwardNode)) {
			registerPOIWithTarget(typeLessBackwardNode,p);
		}
		for(PointOfIndirection p : originToPOI.get(pe.getStartNode())){
			for(AliasCallback cb : poisToCallback.get(p)){
					cb.newAliasEncountered(p,pe.factAtTarget(),pe.getStartNode());
			}
		}
	}

	public void registerPointOfIndirectionAt(PointOfIndirection poi, AliasCallback callback) {
		Pair<Unit, AccessGraph> aliasTarget = poi.getTarget();
		if(poisToCallback.put(poi, callback))
			executeCallback(aliasTarget, poi,callback);
		if(pois.add(poi)){
			if(targetToPOI.put(aliasTarget,poi)){
				poi.sendBackward();
			}

		}
	}
	private void executeCallback(Pair<Unit, AccessGraph> aliasTarget, PointOfIndirection poi, AliasCallback cb) {
		for(Pair<Unit, AccessGraph> origin : reversePathEdges.get(aliasTarget)){
			for(Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)){
					cb.newAliasEncountered(poi,aliases.getO2(),origin);	
			}
		}
	}
	private void registerPOIWithTarget(Pair<Unit, AccessGraph> aliasTarget, PointOfIndirection poi) {
		for(Pair<Unit, AccessGraph> origin : reversePathEdges.get(aliasTarget)){
			if(originToPOI.put(origin,poi)){
				for(Pair<Unit, AccessGraph> aliases : forwardPathEdges.get(origin)){
					for(AliasCallback cb : poisToCallback.get(poi)){
							cb.newAliasEncountered(poi,aliases.getO2(),origin);
					}
				}
			}
		}
	}

	Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultsAtStmtContainingValue(Unit stmt, AccessGraph fact, Set<Pair<Unit,AccessGraph>> visited) {
		Pair<Unit, AccessGraph> visit = new Pair<Unit,AccessGraph>(stmt,fact);
		if(visited.contains(visit)){
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
		for(Pair<Unit, AccessGraph> key: pathEdges.keySet()){
			Unit pathEdgeStart = key.getO1();
			SootMethod callee = context.icfg.getMethodOf(pathEdgeStart);
			boolean maintainPathEdge = false;
			if(context.icfg.getStartPointsOf(callee).contains(pathEdgeStart)){
				for(Unit callSite : context.icfg.getCallersOf(callee)){
					maintainPathEdge |= !context.getContextRequester().continueAtCallSite(callSite, callee);
					Set<AccessGraph> factsAtCallSite = context.getBackwardTargetsFor(key.getO2(), callSite, callee);
					for(AccessGraph factAtCallSite: factsAtCallSite){
						Multimap<Pair<Unit, AccessGraph>, AccessGraph> aliasesAtCallSite = context.getForwardPathEdges().getResultAtStmtContainingValue(callSite, factAtCallSite, visited);
						for(Entry<Pair<Unit, AccessGraph>, AccessGraph> aliasEntry : aliasesAtCallSite.entries()){
							Set<AccessGraph> withinCalleeFacts = context.getForwardTargetsFor(aliasEntry.getValue(), callSite, callee);
							for(AccessGraph wihinCalleeFact : withinCalleeFacts){
								
								Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges.get(new Pair<Unit,AccessGraph>(pathEdgeStart,wihinCalleeFact));
								for (Pair<Unit, AccessGraph> target : fwPair) {
									out.put(aliasEntry.getKey(),target.getO2());
								}
							}
						}
					}
				}
			} else{
				maintainPathEdge = true;
			}
			if(maintainPathEdge){
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
