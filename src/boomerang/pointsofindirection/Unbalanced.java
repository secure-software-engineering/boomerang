package boomerang.pointsofindirection;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.backward.BackwardSolver;
import boomerang.ifdssolver.PathEdge;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import soot.Unit;

public class Unbalanced implements BackwardBackwardHandler {

  private PathEdge<Unit, AccessGraph> pathEdge;


  public Unbalanced(PathEdge<Unit, AccessGraph> pathEdge) {
    this.pathEdge = pathEdge;
  }

  @Override
  public void execute(BackwardSolver pSolver, BoomerangContext context) {
	  context.debugger.onProcessUnbalancedReturnPOI(this);
    pSolver.propagate(pathEdge, PropagationType.BalancedReturn);
    pSolver.awaitExecution();
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
    Unbalanced other = (Unbalanced) obj;
    if (pathEdge == null) {
      if (other.pathEdge != null)
        return false;
    } else if (!pathEdge.equals(other.pathEdge))
      return false;
    return true;
  }


  @Override
  public Unit getStmt() {
    return pathEdge.getTarget();
  }
}
