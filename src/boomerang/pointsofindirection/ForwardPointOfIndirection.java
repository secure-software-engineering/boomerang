package boomerang.pointsofindirection;

import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;

public interface ForwardPointOfIndirection extends PointOfIndirection {
	public Set<AccessGraph> process(BoomerangContext context);
}
