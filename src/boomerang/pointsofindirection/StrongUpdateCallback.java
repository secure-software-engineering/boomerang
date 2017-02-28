package boomerang.pointsofindirection;

import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Unit;

public class StrongUpdateCallback extends AliasCallback {

	private final PathEdge<Unit, AccessGraph> pausedEdge;
	private Set<Pair<Unit,AccessGraph>> origins = new HashSet<>();

	public StrongUpdateCallback(PathEdge<Unit, AccessGraph> pausedEdge, BoomerangContext context) {
		super(context);
		this.pausedEdge = pausedEdge;
	}

	@Override
	public void newAliasEncountered(PointOfIndirection poi, AccessGraph alias, Pair<Unit,AccessGraph> origin) {
		System.out.println(origin);
		if(origins.add(origin)){
			if(origins.size() > 1)
				context.getForwardSolver().inject(pausedEdge, PropagationType.Normal);
		}

	}

}
