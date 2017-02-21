package boomerang.pointsofindirection;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.IFieldGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardSolver;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Local;
import soot.SootField;
import soot.Unit;

public class Read implements BackwardBackwardHandler {
	private IPathEdge<Unit, AccessGraph> edge;
	private Unit succ;
	private Local ifrBase;
	private AccessGraph source;
	private SootField ifrField;
	private Unit curr;
	private IFieldGraph sourceFieldGraph;
	private BoomerangContext context;
	private Set<Pair<Unit,AccessGraph>> origins = new HashSet<>();
	private AccessGraph baseAccessGraph;

	public Read(IPathEdge<Unit, AccessGraph> edge, Local base, SootField field, Unit succ, AccessGraph source,
			BoomerangContext context) {
		this.ifrField = field;
		this.edge = edge;
		this.context = context;
		this.curr = edge.getTarget();
		this.ifrBase = base;
		this.succ = succ;
		this.source = source;
		this.sourceFieldGraph = source.getFieldGraph();
	}

	@Override
	public void execute(BackwardSolver backwardsSolver, BoomerangContext context) {
		context.debugger.onProcessingFieldReadPOI(this);
		AccessGraph original = edge.factAtTarget();
		AliasFinder dart = new AliasFinder(context);
//
//		Set<AccessGraph> iterate = AliasResults.appendField(res.mayAliasSet(),
//				new WrappedSootField(ifrField, source.getBaseType(), edge.getTarget()), context);
//		if (source != null) {
//			iterate = AliasResults.appendFields(iterate, source, context);
//		}
//
//		for (AccessGraph ap : iterate) {
//			if (ap.baseAndFirstFieldMatches(ifrBase, ifrField)) {
//				continue;
//			}
//			if (ap.equals(original)) {
//				continue;
//			}
//			IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<>(edge.getStart(), edge.factAtSource(), succ, ap);
//			context.debugger.indirectFlowEdgeAtRead(source, curr, ap, succ);
//			backwardsSolver.propagate(newEdge, PropagationType.Normal);
//		}
//		backwardsSolver.awaitExecution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((curr == null) ? 0 : curr.hashCode());
		result = prime * result + ((sourceFieldGraph == null) ? 0 : sourceFieldGraph.hashCode());
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
		if (sourceFieldGraph == null) {
			if (other.sourceFieldGraph != null)
				return false;
		} else if (!sourceFieldGraph.equals(other.sourceFieldGraph))
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
	public void newEdgeRegistered(IPathEdge<Unit, AccessGraph> pe) {
		if(pe.factAtTarget().equals(baseAccessGraph)){
			if(origins.add(pe.getStartNode())){
				Multimap<Pair<Unit, AccessGraph>, AccessGraph> resultAtStmtContainingValue = context.getForwardPathEdges().getResultAtStmtContainingValue(curr, baseAccessGraph);
				for(Entry<Pair<Unit, AccessGraph>, AccessGraph> e: resultAtStmtContainingValue.entries()){
					newEdgeRegistered(new PathEdge<Unit, AccessGraph>(e.getKey().getO1(), e.getKey().getO2(), curr, e.getValue()));
				}
			}
		}
		if(origins.contains(pe.getStartNode())){
			AccessGraph factAtTarget = pe.factAtTarget();

			factAtTarget = factAtTarget.appendFields(new WrappedSootField[]{new WrappedSootField(ifrField, source.getBaseType(), edge.getTarget())});
			if (source != null) {
				factAtTarget = factAtTarget.appendGraph(sourceFieldGraph);
			}
			IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<>(edge.getStart(), edge.factAtSource(), succ, factAtTarget);
			context.debugger.indirectFlowEdgeAtRead(source, curr, factAtTarget, succ);
			context.getBackwardSolver().inject(newEdge, PropagationType.Normal);
		}
	}

	@Override
	public void registered() {
		baseAccessGraph = new AccessGraph(ifrBase, ifrBase.getType());
		PathEdge<Unit, AccessGraph> pe = new PathEdge<>(null, baseAccessGraph, curr, baseAccessGraph);
		context.getBackwardSolver().inject(pe, PropagationType.Normal);
	}

}
