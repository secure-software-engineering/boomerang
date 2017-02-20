package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardSolver;
import boomerang.cache.AliasResults;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import soot.SootMethod;
import soot.Unit;

public class Call implements BackwardBackwardHandler, PointOfIndirection {

	private final AccessGraph factInsideCall;
	private final Unit returnSiteOfCall;
	private final SootMethod method;

	public Call(AccessGraph factInsideCall, Unit returnSiteOfCall,
			SootMethod method) {
		this.factInsideCall = factInsideCall;
		this.returnSiteOfCall = returnSiteOfCall;
		this.method = method;
	}

	@Override
	public void execute(BackwardSolver backwardsSolver, BoomerangContext context) {
		context.debugger.onProcessCallPOI(this);
		Collection<WrappedSootField> lastFields = factInsideCall.getLastField();
		if(lastFields == null)
			return;
		for (WrappedSootField lastField : lastFields) {
			Set<AccessGraph> prefixes = factInsideCall.popLastField();
			for (AccessGraph prefix : prefixes) {
				AliasFinder dart = new AliasFinder(context);
				Collection<AccessGraph> aliases = dart.findAliasAtStmt(prefix, returnSiteOfCall).mayAliasSet();
				aliases = AliasResults.appendField(aliases, lastField, context);
				
				for (AccessGraph ap : aliases) {

					if (ap.equals(factInsideCall)) {
						continue;
					}
					IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<Unit,AccessGraph>(null, factInsideCall, returnSiteOfCall, ap);
					context.debugger.indirectFlowEdgeAtCall(factInsideCall, returnSiteOfCall, ap, returnSiteOfCall);
					backwardsSolver.propagate(newEdge, PropagationType.Normal);
//					context.addProcessedPOI(new Call(ap,returnSiteOfCall,method));
				}
			}
		}
		backwardsSolver.awaitExecution();
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
	public Unit getStmt() {
		return returnSiteOfCall;
	}

	public boolean isValid(BoomerangContext context) {
		if (factInsideCall.getFieldCount() < 1)
			return false;

		// if (!shouldExecute(context))
		// return false;
		return true;
	}

}
