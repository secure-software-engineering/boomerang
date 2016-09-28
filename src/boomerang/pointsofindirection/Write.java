package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.context.Context;
import boomerang.context.IContextRequester;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import soot.Local;
import soot.SootField;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class Write implements ForwardPointOfIndirection {
  private Unit curr;
  private AccessGraph source;
  private SootField field;
  private Local base;
  private Local rightLocal;
  private IPathEdge<Unit, AccessGraph> edge;
  private IInfoflowCFG icfg;

  public Write(Unit curr, Local leftLocal, SootField field, Local rightLocal, AccessGraph origin,
      IPathEdge<Unit, AccessGraph> edge) {
    this.field = field;
    this.base = leftLocal;
    this.rightLocal = rightLocal;
    this.source = origin;
    this.curr = curr;
    this.edge = edge;
  }

  @Override
  public Set<AccessGraph> process(BoomerangContext context) {
    if (source.baseMatches(rightLocal)) {
      return queryAliases(context);
    }
    return Collections.emptySet();
  }

  private Set<AccessGraph> queryAliases(BoomerangContext context) {
	  context.debugger.onProcessWritePOI(this);
    if (context.isOutOfBudget())
      throw new BoomerangTimeoutException();
    this.icfg = context.icfg;
    AliasFinder dart = new AliasFinder(context);
    AliasResults res = dart.findAliasAtStmt(new AccessGraph(base, base.getType()), curr,
        new WriteContextRequester(context.getCurrentForwardSolver()));
    Set<AccessGraph> set =
        AliasResults.appendField(res.mayAliasSet(), new WrappedSootField(field, source.getBaseType(),
            curr), context);
    set = AliasResults.appendFields(set, source, context);
    return set;
  }


  @Override
  public Unit getStmt() {
    return curr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((curr == null) ? 0 : curr.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
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
    Write other = (Write) obj;
    if (curr == null) {
      if (other.curr != null)
        return false;
    } else if (!curr.equals(other.curr))
      return false;
    if (source == null) {
      if (other.source != null)
        return false;
    } else if (!source.equals(other.source))
      return false;
    return true;
  }

  public String toString() {
    return "WRITE: " + source + "@" + curr;
  }



  public class WriteContext implements Context {

    private IPathEdge<Unit, AccessGraph> contextEdge;

    public WriteContext(IPathEdge<Unit, AccessGraph> edge) {
      contextEdge = edge;
    }

    @Override
    public Unit getStmt() {
      return contextEdge.getTarget();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((contextEdge == null) ? 0 : contextEdge.hashCode());
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
      WriteContext other = (WriteContext) obj;
      if (contextEdge == null) {
        if (other.contextEdge != null)
          return false;
      } else if (!contextEdge.equals(other.contextEdge))
        return false;
      return true;
    }

    public String toString() {
      return "edge" + contextEdge.toString();
    }


  }

  public class WriteContextRequester implements IContextRequester {

    private ForwardSolver forwardSolver;

    public WriteContextRequester(ForwardSolver forwardSolver) {
      this.forwardSolver = forwardSolver;
    }

    @Override
    public Collection<Context> getCallSiteOf(Context child) {
      if (!(child instanceof WriteContext)) {
        throw new RuntimeException("Wrong type!");
      }
      WriteContext rc = (WriteContext) child;
      Set<? extends IPathEdge<Unit, AccessGraph>> incoming = forwardSolver
          .incoming(rc.contextEdge.getStartNode(), icfg.getMethodOf(rc.contextEdge.getTarget()));
      Set<Context> res = new HashSet<>();
      for (IPathEdge<Unit, AccessGraph> inc : incoming) {
        res.add(new WriteContext(inc));
      }
      return res;
    }

    @Override
    public Context initialContext(Unit stmt) {
      WriteContext c = new WriteContext(edge);
      return c;
    }

  }
}
