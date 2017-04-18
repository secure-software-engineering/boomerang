package boomerang.ifdssolver;

import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public interface IPathEdges<N, D, M, I extends BiDiInterproceduralCFG<N, M>> {
	public boolean hasAlreadyProcessed(IPathEdge<N,D> edge);
	public void register(IPathEdge<N,D> edge);
	public int size();
	public void clear();
	public void printStats();
	public void printTopMethods(int i);
}
