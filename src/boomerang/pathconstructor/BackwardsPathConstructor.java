package boomerang.pathconstructor;

import heros.solver.Pair;

import java.util.Collection;

import boomerang.backward.IBackwardPathEdge;
import boomerang.ifdssolver.IPathEdge;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class BackwardsPathConstructor<N,D,M, I   extends BiDiInterproceduralCFG<N, M>> extends AbstractPathConstructor<N, D, M, I > {


	public BackwardsPathConstructor(IBackwardPathEdge<N,D,M,I> jumpFn) {
		super(jumpFn);
	}

	@Override
	protected Collection<IPathEdge<N, D>> lookUp(Pair<N,D> sPAndD1, IPathEdge<N,D> edge) {
		Collection<IPathEdge<N, D>> backwardsAdjacent = jumpFn.getBackwardsAdjacent(sPAndD1,edge);
		return backwardsAdjacent;
	}

}
