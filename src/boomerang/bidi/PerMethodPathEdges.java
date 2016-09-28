package boomerang.bidi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

class PerMethodPathEdges {
  private Map<Unit, PerStatementPathEdges> stmtToPathEdges = new HashMap<>();
  private Set<IPathEdge<Unit, AccessGraph>> meetableEdges = new HashSet<>();

  boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
    PerStatementPathEdges pathEdges = getOrCreate(pe.getTarget());
    return pathEdges.hasAlreadyProcessed(pe);
  }

  private PerStatementPathEdges getOrCreate(Unit stmt) {
    PerStatementPathEdges perStatementPathEdges = stmtToPathEdges.get(stmt);
    if (perStatementPathEdges == null)
      perStatementPathEdges = new PerStatementPathEdges();
    stmtToPathEdges.put(stmt, perStatementPathEdges);
    return perStatementPathEdges;
  }

  void register(IPathEdge<Unit, AccessGraph> pe) {
    Unit target = pe.getTarget();
    PerStatementPathEdges pathedges = getOrCreate(target);
    pathedges.register(pe);
  }

  public int size() {
    int c = 0;
    for (Unit u : this.stmtToPathEdges.keySet())
      c += this.stmtToPathEdges.get(u).size();
    return c;
  }

  void addMeetableEdge(IPathEdge<Unit, AccessGraph> pathEdge) {
    Unit target = pathEdge.getTarget();
    PerStatementPathEdges pathedges = getOrCreate(target);
    meetableEdges.add(pathEdge);
    pathedges.addMeetableEdge(pathEdge);
  }

  boolean hasMeetableEdges(Unit stmt) {
    PerStatementPathEdges pathedges = getOrCreate(stmt);
    return pathedges.hasMeetableEdges();
  }

  Set<IPathEdge<Unit, AccessGraph>> getAndRemoveMeetableEdges(
      Pair<Unit, AccessGraph> targetNode) {
    PerStatementPathEdges pathedges = getOrCreate(targetNode.getO1());
    return pathedges.getAndRemoveMeetableEdges(targetNode);
  }

  Set<IPathEdge<Unit, AccessGraph>> getAndRemoveMeetableEdgesByStartNode(
      Pair<Unit, AccessGraph> startPoint) {
    Set<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
    for (IPathEdge<Unit, AccessGraph> edge : meetableEdges) {
      if (edge.getStartNode().equals(startPoint))
        out.add(edge);
    }
    meetableEdges.removeAll(out);
    return out;
  }

  Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultsAtStmtContainingValue(Unit stmt,
      AccessGraph fact) {
    PerStatementPathEdges pathedges = getOrCreate(stmt);
    return pathedges.getResultsAtStmtContainingValue(stmt, fact);
  }

  public SootMethod reportStats() {
    return null;
  }

  public void printTopStmts() {
    TreeSet<Unit> set = new TreeSet<>(new Comparator<Unit>() {
      @Override
      public int compare(Unit o1, Unit o2) {
        PerStatementPathEdges m1 = stmtToPathEdges.get(o1);
        int size1 = m1.size();
        PerStatementPathEdges m2 = stmtToPathEdges.get(o2);
        int size2 = m2.size();
        return size1 > size2 ? -1 : 1;
      }

    });
    for (Unit unit : stmtToPathEdges.keySet())
      set.add(unit);
    for (Unit m : set) {
      System.out.println("\t " + m + " " + stmtToPathEdges.get(m).size());
      stmtToPathEdges.get(m).groupByStartUnit();
    }
  }
}
