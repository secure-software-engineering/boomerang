package boomerang.pointsofindirection;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;

public abstract class AliasCallback {
	protected final BoomerangContext context;
	public AliasCallback(BoomerangContext context){
		this.context = context;
	}
	public abstract void newAliasEncountered(AccessGraph alias);
}
