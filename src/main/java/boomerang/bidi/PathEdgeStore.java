package boomerang.bidi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.IPathEdges;
import boomerang.pointsofindirection.AliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class PathEdgeStore implements
    IPathEdges<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {
  private Map<SootMethod, PerMethodPathEdges> stmtToPathEdges = new HashMap<>();
  private BoomerangContext context;
private Direction direction;



  public PathEdgeStore(BoomerangContext c, Direction dir) {
    this.context = c;
	this.direction = dir;
  }

  public void register(IPathEdge<Unit, AccessGraph> pe) {
    Unit target = pe.getTarget();
    SootMethod m = context.icfg.getMethodOf(target);
    PerMethodPathEdges perMethodPathEdges = getOrCreatePerStmt(m);
    perMethodPathEdges.register(pe);
  }

  public void registerPointOfIndirectionAt(Unit stmt, PointOfIndirection poi,AliasCallback cb){
	    SootMethod m = context.icfg.getMethodOf(stmt);
	    PerMethodPathEdges perMethodPathEdges = getOrCreatePerStmt(m);
	    perMethodPathEdges.registerPointOfIndirectionAt(stmt,poi,cb);
  }
  

  public boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
    Unit target = pe.getTarget();
    SootMethod m = context.icfg.getMethodOf(target);
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
    if (perMethodPathEdges == null)
      return false;
    return perMethodPathEdges.hasAlreadyProcessed(pe);
  }

  public Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultAtStmtContainingValue(Unit stmt,
      final AccessGraph fact, Set<Pair<Unit,AccessGraph>> visited) {
    SootMethod m = context.icfg.getMethodOf(stmt);
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
    if (perMethodPathEdges == null)
      return HashMultimap.create();

    return perMethodPathEdges.getResultsAtStmtContainingValue(stmt, fact,visited);

  }

  public void printStats() {
    for (SootMethod m : stmtToPathEdges.keySet()) {
      PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(m);
      System.out.println(m + " ::: " + perMethodPathEdges.size());
    }

  }


  public int size() {
    int c = 0;
    for (SootMethod u : this.stmtToPathEdges.keySet())
      c += this.stmtToPathEdges.get(u).size();
    return c;
  }

  public void clear() {
    stmtToPathEdges.clear();
  }


  private PerMethodPathEdges getOrCreatePerStmt(SootMethod method) {
    PerMethodPathEdges perMethodPathEdges = stmtToPathEdges.get(method);
    if (perMethodPathEdges == null) {
      perMethodPathEdges = new PerMethodPathEdges(context, direction);
      stmtToPathEdges.put(method, perMethodPathEdges);
    }
    return perMethodPathEdges;
  }
  @Override
  public void printTopMethods(int i) {
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
