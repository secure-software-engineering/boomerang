package boomerang.bidi;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.IPathEdges;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class PathEdgeStore implements
    IPathEdges<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {
  private Map<SootMethod, PerMethodPathEdges> stmtToPathEdges = new HashMap<>();
  private Multimap<Pair<Unit, AccessGraph>, IPathEdge<Unit, AccessGraph>> pausedEdges = HashMultimap
      .create();
  private Map<Pair<Unit, AccessGraph>, Multimap<IPathEdge<Unit, AccessGraph>, IPathEdge<Unit, AccessGraph>>> pathFor =
      new HashMap<>();
  private Multimap<Pair<Unit, AccessGraph>, Unit> simplePath = HashMultimap.create();
  private Set<Pair<Unit, AccessGraph>> visitedNodes = new HashSet<>();
  private BoomerangContext context;
  private Direction direction;

  public enum Direction {
    Forward, Backward
  }

  private static Logger logger = LoggerFactory.getLogger(PathEdgeStore.class);
  private static boolean DEBUG = false;//logger.isDebugEnabled();

  public PathEdgeStore(BoomerangContext c, Direction dir) {
    this.context = c;
    this.direction = dir;
  }

  public void register(IPathEdge<Unit, AccessGraph> pe) {
    precheck();
    Unit target = pe.getTarget();
    SootMethod m = context.icfg.getMethodOf(target);
    PerMethodPathEdges perMethodPathEdges = getOrCreatePerStmt(m);
    perMethodPathEdges.register(pe);
    visitedNodes.add(pe.getTargetNode());
  }


  private void precheck() {
    if (context.isOutOfBudget())
      throw new BoomerangTimeoutException();
  }

  public boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
    precheck();
    Unit target = pe.getTarget();
    SootMethod m = context.icfg.getMethodOf(target);
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
    if (perMethodPathEdges == null)
      return false;
    return perMethodPathEdges.hasAlreadyProcessed(pe);
  }

  public Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultAtStmtContainingValue(Unit stmt,
      final AccessGraph fact) {
    precheck();
    SootMethod m = context.icfg.getMethodOf(stmt);
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
    if (perMethodPathEdges == null)
      return HashMultimap.create();

    return perMethodPathEdges.getResultsAtStmtContainingValue(stmt, fact);

  }

  public void printStats() {
    precheck();
    for (SootMethod m : stmtToPathEdges.keySet()) {
      PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
      System.out.println(m + " ::: " + perMethodPathEdges.size());
    }

  }


  public void pauseEdge(Pair<Unit, AccessGraph> pair, IPathEdge<Unit, AccessGraph> edge) {
    precheck();
    if (direction == Direction.Backward)
      return;
    pausedEdges.put(pair, edge);
  }


  public int size() {
    precheck();
    int c = 0;
    for (SootMethod u : this.stmtToPathEdges.keySet())
      c += this.stmtToPathEdges.get(u).size();
    return c;
  }

  public void clear() {
    pausedEdges.clear();
    stmtToPathEdges.clear();
    visitedNodes.clear();
    pathFor.clear();
    simplePath.clear();
  }

  public void addMeetableEdge(IPathEdge<Unit, AccessGraph> pathEdge) {
    precheck();
    if (DEBUG)
      logger.debug("Pausing edge {} at {}", pathEdge, pathEdge.getTarget());
    SootMethod target = context.icfg.getMethodOf(pathEdge.getTarget());
    PerMethodPathEdges perStmtPathEdges = getOrCreatePerStmt(target);
    perStmtPathEdges.addMeetableEdge(pathEdge);
  }

  public boolean hasMeetableEdges(Unit stmt) {
    precheck();
    SootMethod m = context.icfg.getMethodOf(stmt);
    PerMethodPathEdges perStmtPathEdges = getOrCreatePerStmt(m);
    return perStmtPathEdges.hasMeetableEdges(stmt);
  }

  public Set<IPathEdge<Unit, AccessGraph>> getAndRemoveMeetableEdges(
      Pair<Unit, AccessGraph> targetNode) {
    precheck();
    SootMethod m = context.icfg.getMethodOf(targetNode.getO1());
    PerMethodPathEdges perStmtPathEdges = getOrCreatePerStmt(m);
    Set<IPathEdge<Unit, AccessGraph>> pausedEdges =
        perStmtPathEdges.getAndRemoveMeetableEdges(targetNode);

    if (DEBUG)
      logger.debug("Fetching paused edges {} for {}", pausedEdges, targetNode);
    return pausedEdges;
  }

  private PerMethodPathEdges getOrCreatePerStmt(SootMethod method) {
    precheck();
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(method);
    if (perMethodPathEdges == null) {
      perMethodPathEdges = new PerMethodPathEdges();
      stmtToPathEdges.put(method, perMethodPathEdges);
    }
    return perMethodPathEdges;
  }

  public Collection<IPathEdge<Unit, AccessGraph>> getAndRemovePauseEdge(
      Pair<Unit, AccessGraph> startPoint) {
    precheck();
    SootMethod m = context.icfg.getMethodOf(startPoint.getO1());
    PerMethodPathEdges perStmtPathEdges = getOrCreatePerStmt(m);
    Set<IPathEdge<Unit, AccessGraph>> pausedEdges =
        perStmtPathEdges.getAndRemoveMeetableEdgesByStartNode(startPoint);

    if (DEBUG)
      logger.debug("Fetching paused edges {} for {}", pausedEdges, startPoint);
    return pausedEdges;
  }

  @Override
  public void printTopMethods(int i) {
    precheck();
    TreeSet<SootMethod> set = new TreeSet<>(new Comparator<SootMethod>() {
      @Override
      public int compare(SootMethod o1, SootMethod o2) {
        PerMethodPathEdges m1 = stmtToPathEdges.get(o1);
        int size1 = m1.size();
        PerMethodPathEdges m2 = stmtToPathEdges.get(o2);
        int size2 = m2.size();
        return size1 > size2 ? -1 : 1;
      }
    });
    for (SootMethod m : stmtToPathEdges.keySet())
      set.add(m);
    int j = 0;
    for (SootMethod m : set) {
      stmtToPathEdges.get(m).printTopStmts();
      j++;
      if (j > i)
        break;
    }

  }
}
