package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardEdge;
import boomerang.backward.BackwardSolver;
import boomerang.cache.AliasResults;
import boomerang.context.Context;
import boomerang.context.IContextRequester;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import soot.Local;
import soot.SootField;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class Read implements BackwardBackwardHandler {

  public class ReadContext implements Context {

    private IPathEdge<Unit, AccessGraph> contextEdge;

    public ReadContext(IPathEdge<Unit, AccessGraph> edge) {
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
      ReadContext other = (ReadContext) obj;
      if (contextEdge == null) {
        if (other.contextEdge != null)
          return false;
      } else if (!contextEdge.equals(other.contextEdge))
        return false;
      return true;
    }


  }

  public class ReadContextRequester implements IContextRequester {

    private BackwardSolver backwardsSolver;

    public ReadContextRequester(BackwardSolver backwardsSolver) {
      this.backwardsSolver = backwardsSolver;
    }

    @Override
    public Collection<Context> getCallSiteOf(Context child) {
      if(!(child instanceof ReadContext)){
        throw new RuntimeException("Wrong type!");
      }
      ReadContext rc = (ReadContext) child;
      Set<? extends IPathEdge<Unit, AccessGraph>> incoming = backwardsSolver.incoming(rc.contextEdge.getStartNode(), icfg.getMethodOf(rc.contextEdge.getTarget()));
      Set<Context> res = new HashSet<>();
      for(IPathEdge<Unit, AccessGraph> inc : incoming){
        res.add(new ReadContext(inc));
      }
      return res;
    }

    @Override
    public Context initialContext(Unit stmt) {
      ReadContext c = new ReadContext(edge);
      return c;
    }

  }

  private IPathEdge<Unit, AccessGraph> edge;
  private Unit succ;
  private Local ifrBase;
  private AccessGraph source;
  private SootField ifrField;
  private Unit curr;
  private IInfoflowCFG icfg;

  public Read(IPathEdge<Unit, AccessGraph> edge, Local base, SootField field, Unit succ,
      AccessGraph source) {
    this.ifrField = field;
    this.edge = edge;
    this.curr = edge.getTarget();
    this.ifrBase = base;
    this.succ = succ;
    this.source = source;
  }

  @Override
  public void execute(BackwardSolver backwardsSolver, BoomerangContext context) {
	  context.debugger.onProcessingFieldReadPOI(this);
    this.icfg = context.icfg;
    AccessGraph original = edge.factAtTarget();
    AliasFinder dart = new AliasFinder(context);
    AliasResults res = dart.findAliasAtStmt(new AccessGraph(ifrBase, ifrBase.getType()), curr);

    Set<AccessGraph> iterate =
        AliasResults.appendField(res.mayAliasSet(),
            new WrappedSootField(ifrField, source.getBaseType(), edge.getTarget()), context);
    if (source != null) {
      iterate = AliasResults.appendFields(iterate, source, context);
    }

    for (AccessGraph ap : iterate) {
      if (ap.baseAndFirstFieldMatches(ifrBase, ifrField)) {
        continue;
      }
      if (ap.equals(original)) {
        continue;
      }
      IPathEdge<Unit, AccessGraph> newEdge =
          new BackwardEdge(edge.getStart(), edge.factAtSource(), succ, ap);
      context.debugger.indirectFlowEdgeAtRead(source,curr,ap, succ);
      backwardsSolver.propagate(newEdge, edge, PropagationType.Normal, null);
    }
    backwardsSolver.awaitExecution();
  }



  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((curr == null) ? 0 : curr.hashCode());
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    result = prime * result + ((succ == null) ? 0 : succ.hashCode());
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
    Read other = (Read) obj;
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
    if (succ == null) {
      if (other.succ != null)
        return false;
    } else if (!succ.equals(other.succ))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "InstanceRead [edge=" + edge + ", succ=" + succ + "]";
  }

  @Override
  public Unit getStmt() {
    return edge.getTarget();
  }
}
