package boomerang.context;

import java.util.Collection;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * In contrast to the {@link NoContextRequestor}, upon reaching the method start point of the
 * triggered query this requester yields the analysis to continue at ALL callsites.
 * 
 * @author Johannes Spaeth
 *
 */
public class AllCallersRequester<I extends BiDiInterproceduralCFG<Unit, SootMethod>> implements
    IContextRequester {
  private I icfg;

  public AllCallersRequester(I icfg) {
    this.icfg = icfg;
  }


  private class AllCallersContext implements Context {
    private Unit stmt;

    private AllCallersContext(Unit stmt) {
      this.stmt = stmt;
    }

    @Override
    public Unit getStmt() {
      return stmt;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
      return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      AllCallersContext other = (AllCallersContext) obj;
      if (stmt == null) {
        if (other.stmt != null)
          return false;
      } else if (!stmt.equals(other.stmt))
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "AllCallersContext [stmt=" + stmt + "]";
    }
  }

  @Override
  public Collection<Context> getCallSiteOf(Context child) {
    SootMethod method = icfg.getMethodOf(child.getStmt());
    Collection<Unit> callersOf = icfg.getCallersOf(method);

    return Collections2.transform(callersOf, new Function<Unit, Context>() {
      @Override
      public Context apply(Unit arg0) {
        return new AllCallersContext(arg0);
      }
    });
  }

  @Override
  public Context initialContext(Unit stmt) {
    return new AllCallersContext(stmt);
  }
}
