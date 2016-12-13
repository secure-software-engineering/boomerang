package boomerang.backward;

import java.util.Map;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdgeFunctions;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class BackwardProblem
    extends
    DefaultIFDSTabulationProblem<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {
  private BackwardFlowFunctions flowFunctions;
  private BackwardPathEdgeFunctions edgeFunction;
  private BoomerangContext context;

  public BackwardProblem(BoomerangContext context) {
    super(context.bwicfg);
    this.context = context;
  }

  public final BackwardFlowFunctions flowFunctions() {
    if (flowFunctions == null) {
      flowFunctions = new BackwardFlowFunctions(context);
    }
    return flowFunctions;
  }

  protected AccessGraph createZeroValue() {
    return null;
  }

  @Override
  public Map<Unit, Set<AccessGraph>> initialSeeds() {
    return null;
  }



  @Override
  public PathEdgeFunctions<Unit, AccessGraph, SootMethod> pathEdgeFunctions() {
    if (edgeFunction == null) {
        edgeFunction = new BackwardPathEdgeFunctions(flowFunctions(), context);
    }
    return edgeFunction;
  }

  @Override
  public void cleanup() {
    this.pathEdgeFunctions().cleanup();
    this.edgeFunction = null;
    this.flowFunctions = null;
  }

  @Override
  public void onSolverAddIncoming(SootMethod callee, Pair<Unit, AccessGraph> d3,
      IPathEdge<Unit, AccessGraph> pe) {
    if (context.getSubQuery() != null)
      context.getSubQuery().addBackwardIncoming(callee, d3, pe);
  }

  @Override
  public Direction getDirection() {
    return Direction.BACKWARD;
  }

  @Override
  public boolean recordEdges() {
    return false;
  }


}
