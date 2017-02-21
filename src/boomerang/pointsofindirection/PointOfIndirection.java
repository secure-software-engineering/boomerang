package boomerang.pointsofindirection;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;

public interface PointOfIndirection {

	public void newEdgeRegistered(IPathEdge<Unit, AccessGraph> pe);

	public void registered();
}
