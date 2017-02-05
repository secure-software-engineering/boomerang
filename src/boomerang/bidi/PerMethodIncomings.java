package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;
import java.util.Collections;

import soot.Unit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;

class PerMethodIncomings implements IPerMethodIncomings {
	private Multimap<Pair<Unit,AccessGraph>,IPathEdge<Unit, AccessGraph>> startNodeToIncEdges = HashMultimap.create();
	public boolean addIncoming(Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
		return startNodeToIncEdges.put(pair, pe);
	}
	public Collection<IPathEdge<Unit, AccessGraph>> getIncomings(
			Pair<Unit, AccessGraph> startNode) {
		Collection<IPathEdge<Unit, AccessGraph>> collection = startNodeToIncEdges.get(startNode);
		if(collection == null)
			return Collections.emptySet();
		return collection;
	}
	@Override
	public String toString() {
		return startNodeToIncEdges.toString();
	}
}
