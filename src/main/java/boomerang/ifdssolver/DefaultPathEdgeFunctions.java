package boomerang.ifdssolver;

import heros.FlowFunction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;

public abstract class DefaultPathEdgeFunctions<N, D, M> implements PathEdgeFunctions<N, D, M> {

  private FlowFunctions<N, D, M> flowFunctions;
  private IFDSDebugger<N, D, M, ?> debugger;
  private Direction direction;

  public DefaultPathEdgeFunctions(FlowFunctions<N, D, M> flowFunctions,
      IFDSDebugger<N, D, M, ?> debugger, Direction dir) {
    this.flowFunctions = flowFunctions;
    this.debugger = debugger;
    this.direction = dir;
  }

  @Override
  public Collection<? extends IPathEdge<N, D>> normalFunction(IPathEdge<N, D> prevEdge, N succ) {
    FlowFunction<D> flowFunction = flowFunctions.getNormalFlowFunction(prevEdge, succ);
    N sP = prevEdge.getStart();
    D d1 = prevEdge.factAtSource();
    D d2 = prevEdge.factAtTarget();
    Set<D> res = flowFunction.computeTargets(d2);
    Collection<IPathEdge<N, D>> out = new HashSet<>();
    for (D d3 : res) {
      if (!isValid(d3))
        continue;
      debugger.normalFlow(direction, prevEdge.getTarget(), d2, succ, d3);
      PathEdge<N, D> succEdge = new PathEdge<N, D>(sP, d1, succ, d3);
      out.addAll(normalFunctionExtendor(prevEdge, succEdge));
    }
    sanitize(out);
    return out;
  }

  protected abstract Collection<? extends IPathEdge<N, D>> normalFunctionExtendor(
      IPathEdge<N, D> prevEdge, IPathEdge<N, D> succEdge);

  protected abstract Collection<? extends IPathEdge<N, D>> callFunctionExtendor(
      IPathEdge<N, D> prevEdge, IPathEdge<N, D> succEdge, M callee);

  protected abstract Collection<? extends IPathEdge<N, D>> balancedReturnFunctionExtendor(
      IPathEdge<N, D> prevEdge, IPathEdge<N, D> succEdge, IPathEdge<N, D> incEdge);

  protected abstract Collection<? extends IPathEdge<N, D>> call2ReturnFunctionExtendor(
      IPathEdge<N, D> prevEdge, IPathEdge<N, D> succEdge);

  protected abstract Collection<? extends IPathEdge<N, D>> unbalancedReturnFunctionExtendor(
      IPathEdge<N, D> prevEdge, IPathEdge<N, D> succEdge, N callSite, N returnSite);

  @Override
  public Collection<? extends IPathEdge<N, D>> callFunction(IPathEdge<N, D> prevEdge, M callee,
      N calleeSp) {
    D d2 = prevEdge.factAtTarget();
    FlowFunction<D> function = flowFunctions.getCallFlowFunction(prevEdge, callee, calleeSp);
    Set<D> res = function.computeTargets(d2);
    Collection<IPathEdge<N, D>> out = new HashSet<>();
    for (D d3 : res) {
      if (!isValid(d3))
        continue;
      PathEdge<N, D> succEdge = new PathEdge<N, D>(calleeSp, d3, calleeSp, d3);
      debugger.callFlow(direction, prevEdge.getTarget(), d2, calleeSp, d3);
      out.addAll(callFunctionExtendor(prevEdge, succEdge, callee));
    }
    sanitize(out);
    return out;
  }

  @Override
  public Collection<? extends IPathEdge<N, D>> balancedReturnFunction(IPathEdge<N, D> prevEdge,
      N returnSite, M callee, IPathEdge<N, D> incEdge) {
    N sP = incEdge.getStart();
    N n = incEdge.getTarget();
    D d2 = prevEdge.factAtTarget();
    D newD1 = incEdge.factAtSource();
    FlowFunction<D> retFunction =
        flowFunctions.getReturnFlowFunction(prevEdge, n, callee, returnSite);
    Set<D> targets = retFunction.computeTargets(d2);

    Collection<IPathEdge<N, D>> out = new HashSet<>();
    for (D d3 : targets) {
      if (!isValid(d3))
        continue;
      PathEdge<N, D> succEdge = new PathEdge<N, D>(sP, newD1, returnSite, d3);
      debugger.returnFlow(direction, prevEdge.getTarget(), d2, returnSite, d3);
      out.addAll(balancedReturnFunctionExtendor(prevEdge, succEdge, incEdge));
    }
    sanitize(out);
    return out;
  }

  protected void sanitize(Collection<IPathEdge<N, D>> out) {
    for (IPathEdge<N, D> edge : out)
      sanitize(edge);
  }

  protected abstract void sanitize(IPathEdge<N, D> edge);

  @Override
  public Collection<? extends IPathEdge<N, D>> call2ReturnFunction(IPathEdge<N, D> prevEdge,
      N returnSite, Collection<M> callees) {
    FlowFunction<D> callToReturnFlowFunction =
        flowFunctions.getCallToReturnFlowFunction(prevEdge, returnSite, callees);
    // logger.trace("Call to return: CS:{} D1:{} D2: {} D3's{}", callSite,
    // d1,prevTargetVal,computeCallToReturnFlowFunction(callToReturnFlowFunction, d1,
    // prevTargetVal));
    D d1 = prevEdge.factAtSource();
    N sP = prevEdge.getStart();
    D d2 = prevEdge.factAtTarget();
    Set<D> targets = callToReturnFlowFunction.computeTargets(d2);
    Collection<IPathEdge<N, D>> out = new HashSet<>();
    for (D d3 : targets) {
      if (!isValid(d3))
        continue;
      debugger.callToReturn(direction, prevEdge.getTarget(), d2, returnSite, d3);
      PathEdge<N, D> succEdge = new PathEdge<N, D>(sP, d1, returnSite, d3);
      out.addAll(call2ReturnFunctionExtendor(prevEdge, succEdge));
    }
    sanitize(out);
    return out;
  }

  @Override
  public Collection<? extends IPathEdge<N, D>> unbalancedReturnFunction(IPathEdge<N, D> prevEdge,
      N callSite, N returnSite, M callee) {
    D d1 = prevEdge.factAtSource();
    D d2 = prevEdge.factAtTarget();
    FlowFunction<D> retFunction =
        flowFunctions.getReturnFlowFunction(prevEdge, callSite, callee, returnSite);
    final Set<D> targets = retFunction.computeTargets(d2);
    Collection<IPathEdge<N, D>> out = new HashSet<>();
    for (D d3 : targets) {
      if (!isValid(d3))
        continue;
      debugger.returnFlow(direction, prevEdge.getTarget(), d2, returnSite, d3);
      PathEdge<N, D> succEdge = new PathEdge<N, D>(callSite, d1, returnSite, d3);
      out.addAll(unbalancedReturnFunctionExtendor(prevEdge, succEdge, callSite, returnSite));
    }
    sanitize(out);
    return out;
  }

  public Collection<? extends IPathEdge<N, D>> getEdgesOnHold(IPathEdge<N, D> initialSelfLoop,
      IPathEdge<N, D> edgeEnteringCallee) {
    return Collections.emptySet();
  }

  public abstract boolean isValid(D d2);
}
