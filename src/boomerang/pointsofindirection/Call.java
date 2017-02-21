package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardSolver;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

public class Call implements BackwardBackwardHandler, PointOfIndirection {

	private final AccessGraph factInsideCall;
	private final Unit returnSiteOfCall;
	private final SootMethod method;
	private Collection<WrappedSootField> lastFields;
	private Set<Pair<Unit, AccessGraph>> origins = new HashSet<>();
	private Set<AccessGraph> sendBackward;
	private BoomerangContext context;

	public Call(AccessGraph factInsideCall, Unit returnSiteOfCall, SootMethod method, BoomerangContext context) {
		this.factInsideCall = factInsideCall;
		this.returnSiteOfCall = returnSiteOfCall;
		this.method = method;
		this.context = context;
	}

	@Override
	public void execute(BackwardSolver backwardsSolver, BoomerangContext context) {
	}

	@Override
	public String toString() {
		return "Call [factInsideCall=" + factInsideCall + ", returnSiteOfCall=" + returnSiteOfCall + " € " + method
				+ "]";
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
	public void newEdgeRegistered(IPathEdge<Unit, AccessGraph> pe) {
		if (sendBackward != null && sendBackward.contains(pe.factAtTarget())) {
			if (origins.add(pe.getStartNode())) {
				Multimap<Pair<Unit, AccessGraph>, AccessGraph> resultAtStmtContainingValue = context
						.getForwardPathEdges().getResultAtStmtContainingValue(returnSiteOfCall, pe.factAtTarget());
				for (Entry<Pair<Unit, AccessGraph>, AccessGraph> e : resultAtStmtContainingValue.entries()) {
					newEdgeRegistered(new PathEdge<Unit, AccessGraph>(e.getKey().getO1(), e.getKey().getO2(),
							returnSiteOfCall, e.getValue()));
				}
			}
		}
		if (origins.contains(pe.getStartNode())) {
			AccessGraph factAtTarget = pe.factAtTarget();

			for (WrappedSootField f : lastFields) {
				factAtTarget = factAtTarget.appendFields(new WrappedSootField[] { f });
				IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<>(null, factInsideCall, returnSiteOfCall,
						factAtTarget);
				context.debugger.indirectFlowEdgeAtCall(factInsideCall, returnSiteOfCall, factAtTarget,
						returnSiteOfCall);
				context.getBackwardSolver().inject(newEdge, PropagationType.Normal);
			}
		}
	}

	@Override
	public void registered() {
		if (factInsideCall.hasSetBasedFieldGraph() || factInsideCall.isStatic())
			return;
		lastFields = factInsideCall.getLastField();
		if (lastFields == null)
			return;
		sendBackward = factInsideCall.popLastField();
		for (AccessGraph prefix : sendBackward) {
			IPathEdge<Unit, AccessGraph> edge = new PathEdge<Unit, AccessGraph>(null, prefix, returnSiteOfCall, prefix);
			context.getBackwardSolver().inject(edge, PropagationType.Normal);
		}
	}
}
