package boomerang.preanalysis;

import heros.solver.Pair;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class ReadsLocalAnalysis {
  private SootMethod method;
  private IInfoflowCFG icfg;
  private final LoadingCache<Pair<Local, SootField>, Boolean> methodToUsedLocals = CacheBuilder
      .newBuilder().build(new CacheLoader<Pair<Local, SootField>, Boolean>() {
        @Override
        public Boolean load(Pair<Local, SootField> arg0) throws Exception {
          Set<Unit> visited = new HashSet<>();
          LinkedList<Unit> worklist = new LinkedList<>();
          worklist.addAll(icfg.getStartPointsOf(method));
          Local v = arg0.getO1();
          SootField f = arg0.getO2();
          while (!worklist.isEmpty()) {
            Unit next = worklist.removeFirst();
            if (stmtReadsFromLocal(next, f, v)) {
              return true;
            }
            if (!visited.contains(next)) {
              worklist.addAll(icfg.getSuccsOf(next));
              visited.add(next);
            }
          }
          return false;
        }
      });

  public ReadsLocalAnalysis(SootMethod m, IInfoflowCFG icfg) {
    this.method = m;
    assert m.hasActiveBody();
    this.icfg = icfg;
  }


  public boolean readsFromLocal(Local v, SootField f) {
    return methodToUsedLocals.getUnchecked(new Pair<Local, SootField>(v, f));
  }


  private boolean stmtReadsFromLocal(Unit next, SootField f, Local v) {
    if (!(next instanceof AssignStmt)) {
      return false;
    }
    AssignStmt as = (AssignStmt) next;
    Value rOp = as.getRightOp();
    if (opUses(rOp, v, f))
      return true;

    Value lOp = as.getLeftOp();
    if (opUses(lOp, v, f))
      return true;
    return false;
  }


  private boolean opUses(Value op, Local v, SootField f) {
    if (op.equals(v))
      return true;
    if (op instanceof InstanceFieldRef) {
      InstanceFieldRef ifr = (InstanceFieldRef) op;
      if (ifr.getBase().equals(v) && ifr.getField().equals(f))
        return true;
    }
    return false;
  }
}
