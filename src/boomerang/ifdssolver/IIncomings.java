package boomerang.ifdssolver;

import heros.solver.Pair;

import java.util.Collection;

public interface IIncomings<N,M,D>{
	public boolean addIncoming(M callee, D d, IPathEdge<N,D> pe);
	public Collection<IPathEdge<N,D>> incoming(D factAtSource, M m);
	public void clear();
}
