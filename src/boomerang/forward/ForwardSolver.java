package boomerang.forward;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.bidi.Incomings;
import boomerang.ifdssolver.IFDSSolver;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class ForwardSolver extends
    IFDSSolver<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {


  private ForwardPathEdgeFunctions edgeFunctions;
  private BoomerangContext context;
  private String type;
  private long propagationCount;



  public ForwardSolver(ForwardProblem tabulationProblem, BoomerangContext context) {
    super(tabulationProblem, context.debugger);
    this.edgeFunctions = tabulationProblem.pathEdgeFunctions();
    this.pathEdges = context.getForwardPathEdges();
    this.summaries = context.FW_SUMMARIES;
    this.context = context;
    this.incomings = new Incomings();
  }


  /**
   * Starts the propagation at the successor of the provided statement. Creates the path edge
   * <stmt,d1>-><succ(stmt),d2> and adds it to the solvers worklist. The backward path edge is used
   * to track the path along which the forward propagation is allowed.
   * 
   * @param stmt The stmt where the path edge will begin (for each succ(stmt) there will be a path
   *        edge created).
   * @param d1 The start fact of the propagated path edge.
   * @param d2 The target fact of the propagated path edge.
   * @param bwedge Provides the path along which the analysis is allowed to propagate
   */
  public void startPropagationAlongPath(final Unit stmt, final AccessGraph d1, final AccessGraph d2,
      final IPathEdge<Unit, AccessGraph> bwedge) {
    for (Unit succStmt : icfg.getSuccsOf(stmt)) {
      PathEdge<Unit, AccessGraph> pathEdge = new PathEdge<Unit, AccessGraph>(stmt, d1, succStmt, d2);
      edgeFunctions.addToFwBwEdge(pathEdge.getStartNode(), bwedge);
      propagate(pathEdge, pathEdge, PropagationType.Normal, null);
    }

    type = "alloc" + stmt + " " + d2 + " € " + context.icfg.getMethodOf(stmt);

    awaitExecution();
  }


  /**
   * The provided forward path edge pathEdge will be propagated, while the propagation is limited
   * along the propagation path of the backward path edge supplied as the second argument.
   * 
   * @param pathEdge The forward path edge to be scheduled in the worklist.
   * @param bwedge The backward path edge along which the forward propagation will be done.
   */
  public void onMeet(final IPathEdge<Unit, AccessGraph> pathEdge,
      final IPathEdge<Unit, AccessGraph> bwedge) {
    edgeFunctions.addToFwBwEdge(pathEdge.getStartNode(), bwedge);
    pathEdges.register(pathEdge, pathEdge);
    scheduleEdgeProcessing(pathEdge);
    type = "meet@" + pathEdge;
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

  @Override
  public void onRegister(IPathEdge<Unit, AccessGraph> edge) {
    context.sanityCheckEdge(edge);
  }

  public String toString() {
    return "FW Solver" + type;
  }

  @Override
  public void cleanup() {
    super.cleanup();
  }
}
