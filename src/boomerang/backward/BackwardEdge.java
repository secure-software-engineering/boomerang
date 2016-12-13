package boomerang.backward;

import soot.Unit;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.PathEdge;

public class BackwardEdge extends PathEdge<Unit,AccessGraph> {
	public BackwardEdge(Unit start, AccessGraph dSource, Unit target,
			AccessGraph dTarget) {
		super(start, dSource, target, dTarget);
	}

	@Override
	public String toString() {
		return "BW_EDGE " + super.toString();
	}
}
