package soot.jimple.stmtselector;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;


/**
 * A class with the purpose to select specific statements in a method. E.g. one can retrieve the nth
 * statement of the method, or the nthMergePoint, last Stmt etc.
 * 
 * @author "Johannes Spaeth"
 *
 */
public class StmtFinder implements BeforeAfterSwitcher, StmtSelector {

  private SootMethod method;
  protected Set<Unit> stmt;
  private int COUNT_START = 0;
  private static int BEFORE = 0;
  private static int AFTER = 1;

  public StmtFinder(String m) {
    this.setSootMethod(m);
  }

  public StmtFinder(SootMethod m) {
    this.method = m;
  }

  @Override
  public Set<Unit> nthStmt(int n) {
    stmt = getNthUnit(n);
    return stmt;
  }

  @Override
  public Set<Unit> nthMergePoint(int n) {
    stmt = getNthMergePoint(n);
    return stmt;
  }

  @Override
  public Set<Unit> anyCallTo(String method) {
    stmt = getCallsTo(method);
    return stmt;
  }

  public static BeforeAfterSwitcher inMethod(String m) {
    return new StmtFinder(m);
  }

  public static BeforeAfterSwitcher inMethod(SootMethod m) {
    return new StmtFinder(m);
  }

  @Override
  public StmtSelector before() {
    COUNT_START = BEFORE;
    return this;
  }

  @Override
  public StmtSelector after() {
    COUNT_START = AFTER;
    return this;
  }


  private void setSootMethod(String m) {
    SootMethod method = Scene.v().getMethod(m);
    if (method == null) {
      throw new RuntimeException("The specified method " + m.toString() + " does not exist");
    }
    if (!method.hasActiveBody()) {
      throw new RuntimeException("The specified method " + m.toString() + " has no active body");
    }
    this.method = method;
  }

  private Set<Unit> getNthUnit(int n) {
    PatchingChain<Unit> units = method.getActiveBody().getUnits();
    int count = COUNT_START;
    if (n > units.size())
      throw new RuntimeException(
          "The method " + method.toString() + " has only " + units.size() + " units");
    for (Unit u : units) {
      if (count == n) {
        return Collections.singleton(u);
      }
      count++;
    }

    throw new RuntimeException(
        "The specified unit " + n + " was not found in " + method.toString());
  }

  private Set<Unit> getNthMergePoint(int n) {
    ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(method.getActiveBody());
    PatchingChain<Unit> units = method.getActiveBody().getUnits();

    if (n <= 0) {
      throw new RuntimeException("The parameter n has to be greater than 0!");
    }
    int mergePointCount = 0;
    for (Unit u : units) {
      List<Unit> predsOf = exceptionalUnitGraph.getPredsOf(u);
      if (predsOf.size() > 1) {
        mergePointCount++;
      }
      if (mergePointCount == n) {
        if (COUNT_START == BEFORE) {
          return new HashSet<Unit>(predsOf);
        }
        return Collections.singleton(u);
      }
    }
    throw new RuntimeException(
        "There does not exist a mergepoint with number " + n + " in Method " + method.toString());
  }


  private Set<Unit> getCallsTo(String m) {
    PatchingChain<Unit> units = method.getActiveBody().getUnits();
    Set<Unit> resultSet = new HashSet<Unit>();
    for (Unit u : units) {
      if (u instanceof Stmt) {
        SootMethod method = ((Stmt) u).getInvokeExpr().getMethod();
        if (method.toString().equals(m)) {
          resultSet.add(u);
        }
      }
    }
    if (resultSet.isEmpty()) {
      throw new RuntimeException(
          "No call to " + m + " was found in the method " + method.toString());
    }
    if (COUNT_START == BEFORE) {
      return resultSet;
    }
    Set<Unit> predSet = new HashSet<Unit>();
    for (Unit u : resultSet) {
      Unit succOf = units.getSuccOf(u);
      predSet.add(succOf);
    }
    return predSet;
  }

  @Override
  public Set<Unit> lastStmt() {
    Unit last = method.getActiveBody().getUnits().getLast();
    if (COUNT_START == AFTER)
      return Collections.singleton(last);

    ExceptionalUnitGraph exceptionalUnitGraph = new ExceptionalUnitGraph(method.getActiveBody());
    List<Unit> predsOf = exceptionalUnitGraph.getPredsOf(last);
    return new HashSet<Unit>(predsOf);
  }

  @Override
  public Set<Unit> firstStmt() {
    if (COUNT_START == BEFORE)
      throw new RuntimeException("There is no Statement before the first Statement!");
    return Collections.singleton(method.getActiveBody().getUnits().getFirst());
  }

}
