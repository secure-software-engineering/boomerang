package boomerang.pointsofindirection;

import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Unit;

public class StrongUpdateCallback extends AliasCallback {

	private final IPathEdge<Unit, AccessGraph> pausedEdge;
	private Set<Pair<Unit,AccessGraph>> origins = new HashSet<>();

	public StrongUpdateCallback(IPathEdge<Unit, AccessGraph> pausedEdge, BoomerangContext context) {
		super(context);
		this.pausedEdge = pausedEdge;
	}

	@Override
	public void newAliasEncountered(PointOfIndirection poi, AccessGraph alias, Pair<Unit,AccessGraph> origin) {
		if(origins.add(origin) && origins.size() > 1){
			context.getForwardSolver().inject(pausedEdge, PropagationType.Normal);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pausedEdge == null) ? 0 : pausedEdge.hashCode());
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
		StrongUpdateCallback other = (StrongUpdateCallback) obj;
		if (pausedEdge == null) {
			if (other.pausedEdge != null)
				return false;
		} else if (!pausedEdge.equals(other.pausedEdge))
			return false;
		return true;
	}
	
}
