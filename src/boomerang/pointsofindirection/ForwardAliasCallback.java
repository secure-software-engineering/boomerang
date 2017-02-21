package boomerang.pointsofindirection;

import com.google.common.base.Optional;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import soot.Unit;

public abstract class ForwardAliasCallback extends AliasCallback{
	public ForwardAliasCallback(BoomerangContext context) {
		super(context);
	}
	public void newAliasEncountered(AccessGraph alias){
		Optional<IPathEdge<Unit, AccessGraph>> edge = createInjectableEdge(alias);
		if(!edge.isPresent())
			return;
		context.getForwardSolver().inject(edge.get(), PropagationType.Normal);
	}
	public abstract Optional<IPathEdge<Unit, AccessGraph>> createInjectableEdge(AccessGraph alias);
}
