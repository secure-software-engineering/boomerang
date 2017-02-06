package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardEdge;
import boomerang.backward.BackwardSolver;
import boomerang.cache.AliasResults;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;

public class Call implements BackwardBackwardHandler, PointOfIndirection {

  private AccessGraph factInsideCall;
  private Unit returnSiteOfCall;
  private IPathEdge<Unit, AccessGraph> prevEdge;

  public Call(AccessGraph factInsideCall, Unit returnSiteOfCall, IPathEdge<Unit, AccessGraph> prevEdge) {
    this.factInsideCall = factInsideCall;
    this.returnSiteOfCall = returnSiteOfCall;
    this.prevEdge = prevEdge;
  }



  @Override
  public void execute(BackwardSolver backwardsSolver, BoomerangContext context) {
	  context.debugger.onProcessCallPOI(this);
    Collection<WrappedSootField> lastFields = factInsideCall.getLastField();
    for(WrappedSootField lastField : lastFields){
	    Set<AccessGraph> prefixes = factInsideCall.popLastField();
	    for (AccessGraph prefix : prefixes) {
	      AliasFinder dart = new AliasFinder(context);
	      Set<AccessGraph> aliases = dart.findAliasAtStmtRec(prefix, returnSiteOfCall);
	      aliases = AliasResults.appendField(aliases, lastField, context);
	      for (AccessGraph ap : aliases) {
	
	        if (ap.equals(factInsideCall)) {
	          continue;
	        }
	        IPathEdge<Unit, AccessGraph> newEdge =
	            new BackwardEdge(returnSiteOfCall, factInsideCall, returnSiteOfCall, ap);
	        context.debugger.indirectFlowEdgeAtCall(factInsideCall, returnSiteOfCall, ap, returnSiteOfCall);
	        backwardsSolver.propagate(newEdge, prevEdge, PropagationType.Normal, null);
	      }
	      backwardsSolver.awaitExecution();
	    }
    }
  }

  @Override
  public String toString() {
    return "AliasOnBackwardsCallHandler [factInsideCall=" + factInsideCall + ", returnSiteOfCall="
        + returnSiteOfCall + ", caller=TODO" + returnSiteOfCall + "]";
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((factInsideCall == null) ? 0 : factInsideCall.hashCode());
    result = prime * result + ((returnSiteOfCall == null) ? 0 : returnSiteOfCall.hashCode());
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
    Call other = (Call) obj;
    if (factInsideCall == null) {
      if (other.factInsideCall != null)
        return false;
    } else if (!factInsideCall.equals(other.factInsideCall))
      return false;
    if (returnSiteOfCall == null) {
      if (other.returnSiteOfCall != null)
        return false;
    } else if (!returnSiteOfCall.equals(other.returnSiteOfCall))
      return false;
    return true;
  }


  @Override
  public Unit getStmt() {
    return returnSiteOfCall;
  }



  public boolean isValid(BoomerangContext context) {
    if (factInsideCall.getFieldCount() < 1)
      return false;

//    if (!shouldExecute(context))
//      return false;
    return true;
  }

}
