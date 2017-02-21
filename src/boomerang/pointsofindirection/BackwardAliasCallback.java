package boomerang.pointsofindirection;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import soot.Unit;

public abstract class BackwardAliasCallback extends AliasCallback{
	public BackwardAliasCallback(BoomerangContext context) {
		super(context);
	}
	public void newAliasEncountered(AccessGraph alias){
		context.getBackwardSolver().inject(createInjectableEdge(alias), PropagationType.Normal);
	}
	public abstract IPathEdge<Unit, AccessGraph> createInjectableEdge(AccessGraph alias);
}
