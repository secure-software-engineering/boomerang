package boomerang.pointsofindirection;

import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;
import soot.jimple.AssignStmt;

public class Alloc implements BackwardForwardHandler {


  private IPathEdge<Unit, AccessGraph> pathEdge;

  /**
   * Creates an allocation POI with the backward path edge reaching it.
   * 
   * @param pathEdge
   */
  public Alloc(IPathEdge<Unit, AccessGraph> pathEdge) {
    this.pathEdge = pathEdge;
    assert pathEdge.getTarget() instanceof AssignStmt;
  }


  @Override
  public void execute(ForwardSolver ptsSolver, BoomerangContext context) {
	  context.debugger.onProcessAllocationPOI(this);
    AccessGraph factAtTarget = pathEdge.factAtTarget();
    Unit target = pathEdge.getTarget();
    AccessGraph alloc = factAtTarget.deriveWithAllocationSite(target);
    assert alloc.hasAllocationSite() == true;
    // start forward propagation from the path edge target with the allocation site.
    ptsSolver.startPropagationAlongPath(target, alloc, factAtTarget, pathEdge);
    
    // Case in which the allocation site is also a field write statement (a.f = new)
    if (factAtTarget.getFieldCount() > 0) {
      Set<AccessGraph> bases = factAtTarget.popFirstField();
      for (AccessGraph base : bases) {
        WrappedSootField field = factAtTarget.getFirstField();
        AliasFinder dart = new AliasFinder(context);
        AliasResults res = dart.findAliasAtStmt(base, target);
        Set<AccessGraph> withField = AliasResults.appendField(res.mayAliasSet(), field, context);
        for (AccessGraph alias : withField) {
          if (alias.equals(alloc))
            continue;
          ptsSolver.startPropagationAlongPath(target, alloc, alias, pathEdge);
        }
      }
    }
  }

  @Override
  public String toString() {
    return "AllocationSiteAPOI [pathEdge=" + pathEdge + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pathEdge == null) ? 0 : pathEdge.hashCode());
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
    Alloc other = (Alloc) obj;
    if (pathEdge == null) {
      if (other.pathEdge != null)
        return false;
    } else if (!pathEdge.equals(other.pathEdge))
      return false;
    return true;
  }

  public IPathEdge<Unit, AccessGraph> getEdge() {
    return this.pathEdge;
  }

  @Override
  public Unit getStmt() {
    return pathEdge.getTarget();
  }
}
