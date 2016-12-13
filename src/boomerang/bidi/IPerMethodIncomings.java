package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;

interface IPerMethodIncomings {
	public boolean addIncoming(Pair<Unit, AccessGraph> pair,IPathEdge<Unit, AccessGraph> pe);
	public Collection<IPathEdge<Unit, AccessGraph>> getIncomings(Pair<Unit, AccessGraph> startNode);
}
