package boomerang.pointsofindirection;

import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;

public class Alloc implements BackwardForwardHandler {

  private Unit target;
private SootMethod method;
private AccessGraph factAtTarget;

  /**
   * Creates an allocation POI with the backward path edge reaching it.
   * 
   * @param pathEdge
   */
  public Alloc(AccessGraph factAtTarget, Unit target, SootMethod method) {
	this.factAtTarget = factAtTarget;
	this.method = method;
	this.target = target;
  }


  @Override
  public void execute(ForwardSolver ptsSolver, BoomerangContext context) {
	  context.debugger.onProcessAllocationPOI(this);
    AccessGraph alloc = factAtTarget.deriveWithAllocationSite(target);
//    if(!alloc.firstFirstFieldMayMatch(AliasFinder.ARRAY_FIELD))
//    	alloc = alloc.dropTail();
    if(target instanceof AssignStmt && ((AssignStmt) target).getRightOp() instanceof NewExpr)
    	alloc = alloc.deriveWithNewLocal(alloc.getBase(), ((NewExpr)((AssignStmt) target).getRightOp()).getBaseType());
    assert alloc.hasAllocationSite() == true;
    // start forward propagation from the path edge target with the allocation site.
    ptsSolver.startPropagationAlongPath(target, alloc, alloc.deriveWithoutAllocationSite(), null);
    
    // Case in which the allocation site is also a field write statement (a.f = new)
    //TODO Is this necessary?
//    if (factAtTarget.getFieldCount() > 0) {
//      Set<AccessGraph> bases = factAtTarget.popFirstField();
//      for (AccessGraph base : bases) {
//        for(WrappedSootField field : factAtTarget.getFirstField()){
//	        AliasFinder dart = new AliasFinder(context);
//	        AliasResults res = dart.findAliasAtStmt(base, target);
//	        Set<AccessGraph> withField = AliasResults.appendField(res.mayAliasSet(), field, context);
//	        for (AccessGraph alias : withField) {
//	          if (alias.equals(alloc))
//	            continue;
//	          ptsSolver.startPropagationAlongPath(target, alloc, alias, pathEdge);
//	        }
//        }
//      }
//    }
  }

  @Override
  public String toString() {
    return "Alloc ["+target +" €" + method+"]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((target == null) ? 0 : target.hashCode());
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
    if (target == null) {
      if (other.target != null)
        return false;
    } else if (!target.equals(other.target))
      return false;
    return true;
  }


  @Override
  public Unit getStmt() {
    return target;
  }
}
