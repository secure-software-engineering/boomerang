package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import soot.Unit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.PointOfIndirection;

class PerStatementPathEdges {
  private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> forwardPathEdges = HashMultimap
      .create();
  private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> reversePathEdges = HashMultimap
      .create();
  private Set<IPathEdge<Unit, AccessGraph>> meetableEdges = new HashSet<>();
  private Multimap<Pair<Unit, AccessGraph>, Pair<Unit, AccessGraph>> reverseMeetableEdges =
      HashMultimap.create();
  private Set<IPathEdge<Unit, AccessGraph>> processedPathEdges = new HashSet<>();
  private Set<PointOfIndirection> pois = new HashSet<>();

  void register(IPathEdge<Unit, AccessGraph> pe) {
	  
    forwardPathEdges.put(pe.getStartNode(), pe.getTargetNode());
    reversePathEdges.put(new Pair<Unit,AccessGraph>(pe.getTarget(),pe.factAtTarget().noType()), pe.getStartNode());
    processedPathEdges.add(pe);
    for(PointOfIndirection p : pois){
    	p.newEdgeRegistered(pe);
    }
  }

	public void registerPointOfIndirectionAt(PointOfIndirection poi) {
		if(pois.add(poi)){
			poi.registered();
			for(IPathEdge<Unit, AccessGraph> edge : processedPathEdges)
				poi.newEdgeRegistered(edge);
		}
	}
  Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultsAtStmtContainingValue(Unit stmt,
      AccessGraph fact) {
    Multimap<Pair<Unit, AccessGraph>, AccessGraph> out = HashMultimap.create();
    Pair<Unit, AccessGraph> o = new Pair<>(stmt, fact.noType());
    if (!reversePathEdges.containsKey(o))
      return HashMultimap.create();

    Collection<Pair<Unit, AccessGraph>> matchingStarts = new HashSet<>();
    matchingStarts = reversePathEdges.get(o);
    for (Pair<Unit, AccessGraph> start : matchingStarts) {
      Collection<Pair<Unit, AccessGraph>> fwPair = forwardPathEdges.get(start);
      for (Pair<Unit, AccessGraph> target : fwPair) {
        out.put(start, target.getO2());
      }
    }
    return out;
  }

  boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
    if (processedPathEdges.contains(pe))
      return true;
    return false;
  }


  void addMeetableEdge(IPathEdge<Unit, AccessGraph> pathEdge) {
    meetableEdges.add(pathEdge);
    reverseMeetableEdges.put(pathEdge.getTargetNode(), pathEdge.getStartNode());
  }

  boolean hasMeetableEdges() {
    return !meetableEdges.isEmpty();
  }

  Set<IPathEdge<Unit, AccessGraph>> getAndRemoveMeetableEdges(Pair<Unit, AccessGraph> targetNode) {
    HashSet<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
    Collection<Pair<Unit, AccessGraph>> startNodes = reverseMeetableEdges.get(targetNode);
    for (IPathEdge<Unit, AccessGraph> edge : meetableEdges) {
      if (startNodes.contains(edge.getStartNode())) {
        out.add(edge);
      }
    }

    meetableEdges.removeAll(out);
    return out;
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
    reverseMeetableEdges.clear();
    reversePathEdges.clear();
    meetableEdges.clear();
    processedPathEdges.clear();

    forwardPathEdges = null;
    reversePathEdges = null;
    meetableEdges = null;
    reverseMeetableEdges = null;
    processedPathEdges = null;
  }


}
