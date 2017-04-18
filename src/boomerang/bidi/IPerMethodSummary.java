package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;

interface IPerMethodSummary {
	void addEndSummary(IPathEdge<Unit, AccessGraph> edge);
	Collection<IPathEdge<Unit, AccessGraph>> endSummary(Pair<Unit, AccessGraph> d3);
}
