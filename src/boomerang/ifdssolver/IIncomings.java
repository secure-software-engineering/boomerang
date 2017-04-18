package boomerang.ifdssolver;

import heros.solver.Pair;

import java.util.Collection;

public interface IIncomings<N,M,D>{
	public boolean addIncoming(M callee, Pair<N, D> pair, IPathEdge<N,D> pe);
	public Collection<IPathEdge<N,D>> incoming(Pair<N, D> pair, M m);
	public void clear();
}
