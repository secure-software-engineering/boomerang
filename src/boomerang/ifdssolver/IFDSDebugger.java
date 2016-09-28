package boomerang.ifdssolver;

import heros.solver.Pair;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;

public interface IFDSDebugger<N, D, M, I extends BiDiInterproceduralCFG<N, M>> {
  public void addIncoming(Direction direction, M callee, Pair<N, D> pair, IPathEdge<N, D> pe);

  public void addSummary(Direction direction, M methodToSummary, IPathEdge<N, D> summary);

  public void normalFlow(Direction dir, N start, D startFact, N target, D targetFact);

  public void callToReturn(Direction dir, N start, D startFact, N target, D targetFact);

  public void returnFlow(Direction dir, N start, D startFact, N target, D targetFact);

  public void backwardStart(Direction backward, Unit stmt, AccessGraph d1, Unit s);

public void onEnterCall(N n, Collection<? extends IPathEdge<N, D>> nextCallEdges, IPathEdge<N,D> incEdge);

public void onProcessCall(IPathEdge<N, D> edge);

public void onProcessExit(IPathEdge<N, D> edge);

public void onProcessNormal(IPathEdge<N, D> edge);
}
