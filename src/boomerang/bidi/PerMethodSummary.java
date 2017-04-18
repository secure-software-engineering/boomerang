package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;

import soot.Unit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;

class PerMethodSummary implements IPerMethodSummary{
	private Multimap<Pair<Unit,AccessGraph>, IPathEdge<Unit,AccessGraph>> startNodeToSummary = HashMultimap.create();
	public void addEndSummary(IPathEdge<Unit, AccessGraph> edge) {
		startNodeToSummary.put(edge.getStartNode(), edge);
	}
	public Collection<IPathEdge<Unit, AccessGraph>> endSummary(Pair<Unit, AccessGraph> startNode) {
		return startNodeToSummary.get(startNode);
	}
}
