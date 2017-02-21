package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Unit;

public class Return implements ForwardPointOfIndirection {

	private Unit callSite;
	private AccessGraph outcoming;
	private Collection<WrappedSootField> lastFields;
	private Set<AccessGraph> sendBackward;
	private Set<Pair<Unit,AccessGraph>> origins = new HashSet<>();
	private BoomerangContext context;
	private Unit sourceStmt;
	private AccessGraph sourceFact;

	public Return(Unit origin, AccessGraph sourceFact, Unit callSite, AccessGraph outcomings, BoomerangContext context) {
		this.sourceStmt = origin;
		this.sourceFact = sourceFact;
		assert callSite != null;
		this.callSite = callSite;
		this.outcoming = outcomings;
		this.context = context;
	}

	@Override
	public String toString() {
		return "ForwardExitQuery(" + callSite + "," + outcoming + ")";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((outcoming == null) ? 0 : outcoming.hashCode());
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
		Return other = (Return) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (outcoming == null) {
			if (other.outcoming != null)
				return false;
		} else if (!outcoming.equals(other.outcoming))
			return false;
		return true;
	}

	@Override
	public void newEdgeRegistered(IPathEdge<Unit, AccessGraph> pe) {
		if(sendBackward != null && sendBackward.contains(pe.factAtTarget())){
			if(origins.add(pe.getStartNode())){
				Multimap<Pair<Unit, AccessGraph>, AccessGraph> resultAtStmtContainingValue = context.getForwardPathEdges().getResultAtStmtContainingValue(callSite, pe.factAtTarget());
				for(Entry<Pair<Unit, AccessGraph>, AccessGraph> e: resultAtStmtContainingValue.entries()){
					newEdgeRegistered(new PathEdge<Unit, AccessGraph>(e.getKey().getO1(), e.getKey().getO2(), callSite, e.getValue()));
				}
			}
		}
		if(origins.contains(pe.getStartNode())){
			AccessGraph factAtTarget = pe.factAtTarget();

			for(WrappedSootField f : lastFields){
				factAtTarget = factAtTarget.appendFields(new WrappedSootField[]{f});
				IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<>(sourceStmt, sourceFact, callSite, factAtTarget);
				context.debugger.indirectFlowEdgeAtReturn(sourceFact, sourceStmt, factAtTarget, callSite);
				if(!context.getForwardSolver().getPathEdges().hasAlreadyProcessed(newEdge))
					context.getForwardSolver().propagate(newEdge, PropagationType.Normal);
			}
		}
	}

	@Override
	public void registered() {
		if(outcoming.hasSetBasedFieldGraph() || outcoming.isStatic())
			return;
		lastFields = outcoming.getLastField();
		if (lastFields == null)
			return;
		sendBackward = outcoming.popLastField();
		for (AccessGraph prefix : sendBackward) {
			IPathEdge<Unit, AccessGraph> edge = new PathEdge<Unit, AccessGraph>(null, prefix, callSite, prefix);
			context.getBackwardSolver().inject(edge, PropagationType.Normal);
		}
	}

}
