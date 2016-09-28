package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;

/**
 * A meeting point (not described in paper) is a point where a backward edge discovered forward path
 * edges and the target nodes of backward and forward edges are the same. Then the forward path
 * edges are propagated forward and might flow to new parts of the program, as the backward analysis
 * previously has unveiled new statement to be visited by the forward analysis.
 * 
 * @author spaeth
 *
 */
public class Meeting implements BackwardForwardHandler {


  private IPathEdge<Unit, AccessGraph> bwEdge;
  private Collection<IPathEdge<Unit, AccessGraph>> fwEdges;
  private ReversalType type;

  private enum ReversalType {
    Normal, BalancedCall, UnbalancedCall, ProcessCall, Call2Return
  }

  public Meeting(Set<IPathEdge<Unit, AccessGraph>> fwEdges, IPathEdge<Unit, AccessGraph> bwEdge) {
    this.fwEdges = new HashSet<>(fwEdges);
    this.bwEdge = bwEdge;
  }


  @Override
  public void execute(ForwardSolver pSolver, BoomerangContext context) {
	 context.debugger.onProcessingMeetingPOI(this);
    for (IPathEdge<Unit, AccessGraph> fwEdge : fwEdges) {
      // each forward edge is inserted with the backward edge which generated this Meeting
      pSolver.onMeet(fwEdge, bwEdge);
    }
  }


  @Override
  public String toString() {
    return "MeetingPPOI [pathedge=" + bwEdge + " currentFacts=" + fwEdges.size() + "]";
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fwEdges == null) ? 0 : fwEdges.hashCode());
    result = prime * result + ((bwEdge == null) ? 0 : bwEdge.hashCode());
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
    Meeting other = (Meeting) obj;
    if (fwEdges == null) {
      if (other.fwEdges != null)
        return false;
    } else if (!fwEdges.equals(other.fwEdges))
      return false;
    if (bwEdge == null) {
      if (other.bwEdge != null)
        return false;
    } else if (!bwEdge.equals(other.bwEdge))
      return false;
    return true;
  }

  public ReversalType getType() {
    return type;
  }


  @Override
  public Unit getStmt() {
    return bwEdge.getTarget();
  }
}
