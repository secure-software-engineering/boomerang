package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

/**
 * A meeting point (not described in paper) is a point where a backward edge
 * discovered forward path edges and the target nodes of backward and forward
 * edges are the same. Then the forward path edges are propagated forward and
 * might flow to new parts of the program, as the backward analysis previously
 * has unveiled new statement to be visited by the forward analysis.
 * 
 * @author spaeth
 *
 */
public class Meeting implements BackwardForwardHandler {

	private Collection<IPathEdge<Unit, AccessGraph>> fwEdges;
	private ReversalType type;
	private SootMethod methodOf;
	private Pair<Unit, AccessGraph> startNode;

	private enum ReversalType {
		Normal, BalancedCall, UnbalancedCall, ProcessCall, Call2Return
	}

	public Meeting(Pair<Unit, AccessGraph> startNode, SootMethod methodOf, Set<IPathEdge<Unit, AccessGraph>> fwEdges) {
		this.startNode = startNode;
		this.methodOf = methodOf;
		this.fwEdges = new HashSet<>(fwEdges);
	}

	@Override
	public void execute(ForwardSolver pSolver, BoomerangContext context) {
		context.debugger.onProcessingMeetingPOI(this);
		for (IPathEdge<Unit, AccessGraph> fwEdge : fwEdges) {
			// each forward edge is inserted with the backward edge which
			// generated this Meeting
			pSolver.onMeet(fwEdge);
		}
	}

	@Override
	public String toString() {
		return "MeetingPOI [pathedge=" + fwEdges + " currentFacts=" + fwEdges.size() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodOf == null) ? 0 : methodOf.hashCode());
		result = prime * result + ((startNode == null) ? 0 : startNode.hashCode());
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
		if (methodOf == null) {
			if (other.methodOf != null)
				return false;
		} else if (!methodOf.equals(other.methodOf))
			return false;
		if (startNode == null) {
			if (other.startNode != null)
				return false;
		} else if (!startNode.equals(other.startNode))
			return false;
		return true;
	}

	@Override
	public Unit getStmt() {
		return null;
	}
}
