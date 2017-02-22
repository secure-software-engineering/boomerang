package boomerang.pointsofindirection;

import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;

public abstract class AliasCallback {
	protected final BoomerangContext context;
	protected Set<AccessGraph> executed = new HashSet<>();
	public AliasCallback(BoomerangContext context){
		this.context = context;
	}
	public abstract void newAliasEncountered(PointOfIndirection poi, AccessGraph alias);
}
