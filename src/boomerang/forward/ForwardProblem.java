package boomerang.forward;

import java.util.Map;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class ForwardProblem
    extends
    DefaultIFDSTabulationProblem<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {

  private ForwardFlowFunctions flowFunctions;
  private ForwardPathEdgeFunctions edgeFunctions;
  private BoomerangContext context;

  public ForwardProblem(BoomerangContext context) {
    super(context.icfg);
    this.context = context;
  }

  @Override
  public boolean computeValues() {
    return false;
  }

  @Override
  public Map<Unit, Set<AccessGraph>> initialSeeds() {
    return null;
  }


  protected AccessGraph createZeroValue() {
    return null;
  }

  @Override
  public final ForwardFlowFunctions flowFunctions() {
    if (flowFunctions == null) {
      flowFunctions = new ForwardFlowFunctions(context);
    }
    return flowFunctions;
  }

  @Override
  public ForwardPathEdgeFunctions pathEdgeFunctions() {
    if (edgeFunctions == null) {
        edgeFunctions = new ForwardPathEdgeFunctions(flowFunctions(), context);
    }
    return edgeFunctions;
  }

  @Override
  public void cleanup() {}

  @Override
  public void onSolverAddIncoming(SootMethod callee, AccessGraph d3,
      IPathEdge<Unit, AccessGraph> pe) {}


  @Override
  public Direction getDirection() {
    return Direction.FORWARD;
  }

  @Override
  public boolean recordEdges() {
    return false;
  }
}
