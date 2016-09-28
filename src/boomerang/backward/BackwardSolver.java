package boomerang.backward;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.bidi.Incomings;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IFDSSolver;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class BackwardSolver extends
    IFDSSolver<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {

  private BoomerangContext context;
  private long propagationCount;

  public BackwardSolver(BackwardProblem tabulationProblem, BoomerangContext context) {
    super(tabulationProblem, context.debugger);
    this.context = context;
    this.pathEdges = context.getBackwardsPathEdges();
    this.summaries = context.BW_SUMMARIES;
    this.incomings = new Incomings();
  }


  public void startPropagation(AccessGraph d1, Unit stmt) {
    PathEdge<Unit, AccessGraph> prevEdge = new PathEdge<Unit, AccessGraph>(stmt, d1, stmt, d1);
    for (Unit s : icfg.getSuccsOf(stmt)) {
      PathEdge<Unit, AccessGraph> edge = new PathEdge<Unit, AccessGraph>(stmt, d1, s, d1);
      debugger.backwardStart(Direction.BACKWARD, stmt, d1, s);
      checkForMeetingPoints(edge, stmt);
      propagate(edge, prevEdge, PropagationType.Normal, null);
    }
    awaitExecution();
  }

  @Override
  public void awaitExecution() {
    while (worklist != null && !worklist.isEmpty()) {
      PathEdgeProcessingTask task = worklist.poll();
      propagationCount++;
      if (propagationCount % 1000 == 0) {
        if (context.isOutOfBudget()) {
          throw new BoomerangTimeoutException();
        }
      }
      task.run();
    }
  }

  private void checkForMeetingPoints(IPathEdge<Unit, AccessGraph> edge, Unit prevStmt) {
    boomerang.backward.BackwardPathEdgeFunctions func =
        (boomerang.backward.BackwardPathEdgeFunctions) this.pathEdgeFunctions;
    func.performMeetCheck(prevStmt, edge);
  }

  @Override
  public void onRegister(IPathEdge<Unit, AccessGraph> edge) {
    context.sanityCheckEdge(edge);
  }

  public String toString() {
    return "BWSOLVER: " + context.getSubQuery();
  }
}
