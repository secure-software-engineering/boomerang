package boomerang.backward;

import heros.solver.Pair;

import java.util.Collection;

import boomerang.ifdssolver.IPathEdge;

public interface IBackwardPathEdge<N, D, M, I> {
	public Collection<IPathEdge<N,D>> getBackwardsAdjacent(Pair<N,D> spAndD1, IPathEdge<N,D> edge);
}
