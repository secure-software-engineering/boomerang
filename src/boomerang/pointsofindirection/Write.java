package boomerang.pointsofindirection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Local;
import soot.SootField;
import soot.Unit;

public class Write implements ForwardPointOfIndirection {
	private Unit curr;
	private AccessGraph source;
	private SootField field;
	private Local base;
	private IPathEdge<Unit, AccessGraph> edge;
	private Set<Pair<Unit, AccessGraph>> origins = new HashSet<>();
	private BoomerangContext context;
	private Unit succ;

	public Write(Unit curr, Unit succ, Local leftLocal, SootField field, Local rightLocal, AccessGraph origin,
			IPathEdge<Unit, AccessGraph> edge, BoomerangContext context) {
		this.succ = succ;
		this.field = field;
		this.base = leftLocal;
		this.source = origin;
		this.curr = curr;
		this.edge = edge;
		this.context = context;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((curr == null) ? 0 : curr.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		Write other = (Write) obj;
		if (curr == null) {
			if (other.curr != null)
				return false;
		} else if (!curr.equals(other.curr))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	public String toString() {
		return "WRITE: " + source + "@" + curr;
	}

	@Override
	public void newEdgeRegistered(IPathEdge<Unit, AccessGraph> pe) {
		AccessGraph baseAccessGraph = new AccessGraph(base, base.getType());
		if (pe.factAtTarget().equals(baseAccessGraph)) {
			if (origins.add(pe.getStartNode())) {
				Multimap<Pair<Unit, AccessGraph>, AccessGraph> resultAtStmtContainingValue = context
						.getForwardPathEdges().getResultAtStmtContainingValue(curr, baseAccessGraph);
				for (Entry<Pair<Unit, AccessGraph>, AccessGraph> e : resultAtStmtContainingValue.entries()) {
					newEdgeRegistered(new PathEdge<Unit, AccessGraph>(e.getKey().getO1(), e.getKey().getO2(), curr,
							e.getValue()));
				}
			}
		}
		if (origins.contains(pe.getStartNode())) {
			AccessGraph factAtTarget = pe.factAtTarget();

			factAtTarget = factAtTarget.appendFields(
					new WrappedSootField[] { new WrappedSootField(field, source.getBaseType(), edge.getTarget()) });
			if (source != null) {
				factAtTarget = factAtTarget.appendGraph(source.getFieldGraph());
			}
			IPathEdge<Unit, AccessGraph> newEdge = new PathEdge<>(edge.getStart(), edge.factAtSource(), succ,
					factAtTarget);
			context.debugger.indirectFlowEdgeAtWrite(edge.factAtSource(), curr, factAtTarget, succ);
			context.getForwardSolver().inject(newEdge, PropagationType.Normal);
		}
	}

	@Override
	public void registered() {
		AccessGraph baseAccessGraph = new AccessGraph(base, base.getType());
		PathEdge<Unit, AccessGraph> pe = new PathEdge<>(null, baseAccessGraph, curr, baseAccessGraph);
		context.getBackwardSolver().inject(pe, PropagationType.Normal);
	}
}
