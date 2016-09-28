package boomerang.ifdssolver;

import heros.solver.Pair;

import java.util.Collection;

public interface ISummaries<N,M,D>{
	public void addEndSummary(M m, IPathEdge<N,D> edge);
	public Collection<IPathEdge<N, D>> endSummary(M m, Pair<N, D> d3);
	public void clear();
}
