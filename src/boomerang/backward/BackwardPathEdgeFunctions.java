package boomerang.backward;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.SubQueryContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.forward.AbstractPathEdgeFunctions;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import boomerang.pointsofindirection.BackwardParameterTurnHandler;
import boomerang.pointsofindirection.Call;
import boomerang.pointsofindirection.Meeting;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

class BackwardPathEdgeFunctions extends AbstractPathEdgeFunctions {
  BackwardPathEdgeFunctions(FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions,
      BoomerangContext context) {
    super(flowFunctions, context, Direction.BACKWARD);
  }


  /**
   * The backward analysis does not to unbalanced returns. But still, a check is performed if the
   * analysis should add an {@link BackwardParameterTurnHandler}.
   */
  public Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunction(
      IPathEdge<Unit, AccessGraph> prevEdge, Unit callSite, Unit returnSite, SootMethod callee) {
    // do not propagate further if the query was started inside this method.
    if (isQueryStartedInsideMethod(prevEdge, callee)) {
      reachesStartPointOfStartMethod(prevEdge);
    }
    return Collections.emptySet();
  };


  /**
   * Checks whether the query has been started inside this method, to maybe do the turn around at
   * the method start point
   * 
   * @param prevEdge The current backward edge being processed.
   * @param callee The callee
   * @return
   */
  private boolean isQueryStartedInsideMethod(IPathEdge<Unit, AccessGraph> prevEdge,
      SootMethod callee) {
    AccessGraph d1 = prevEdge.factAtSource();
    SubQueryContext query = context.getSubQuery();
    if (query == null) {
      return false;
    }
    if (context.bwicfg.getMethodOf(query.getStmt()).equals(callee)
        && d1.equals(query.getAccessPath())) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the edges reaches a start point of a method, if the target fact is a method parameter
   * (incl this) it adds the appropriate POI to let the analysis turn around.
   * 
   * @param edge
   */
  private void reachesStartPointOfStartMethod(IPathEdge<Unit, AccessGraph> edge) {
    boolean doTurnaround =
        context.isParameterOrThisValue(edge.getTarget(), edge.factAtTarget().getBase()) ||
        (edge.factAtTarget().isStatic() && context.trackStaticFields());
    if (doTurnaround) {
      if (context.getSubQuery() == null)
        return;
      context.getSubQuery().add(new BackwardParameterTurnHandler(edge));
    }
  }


  private void addMeetingPoints(Set<IPathEdge<Unit, AccessGraph>> fwEdges,
      IPathEdge<Unit, AccessGraph> bwEdge) {
    Meeting meetingPoint = new Meeting(fwEdges, bwEdge);
    if (context.getSubQuery() != null) {
      context.getSubQuery().add(meetingPoint);
    }
  }


  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
    return Collections.singleton(succEdge);
  }

  /**
   * Performs a meet check, thus checks whether the backward path edge at the provided statement
   * "meets" (i.e. exploded super graph nodes are equal) a forward edge. If so, a meeting point is
   * added.
   * 
   * @param statement The statement to be checked
   * @param bwPathEdge The path edge providing the data flow fact to be checked.
   * @return
   */
  boolean performMeetCheck(Unit statement, IPathEdge<Unit, AccessGraph> bwPathEdge) {
    boolean isMeetPoint = isMeetableStmt(statement);
    if (isMeetPoint) {
      Set<IPathEdge<Unit, AccessGraph>> meetingEdges =
          context.getForwardPathEdges().getAndRemoveMeetableEdges(new Pair<Unit, AccessGraph>(statement,
              bwPathEdge.factAtTarget()));
      if (meetingEdges.isEmpty())
        return false;
      addMeetingPoints(meetingEdges, bwPathEdge);
      return true;
    }
    return false;
  }


  private void performMeetCheckOnEnter(IPathEdge<Unit, AccessGraph> initialSelfLoopEdge,
      IPathEdge<Unit, AccessGraph> callerEdge) {
    Unit calleeExitStmt = initialSelfLoopEdge.getTarget();
    AccessGraph enteringFact = initialSelfLoopEdge.factAtSource();
    Multimap<Pair<Unit, AccessGraph>, AccessGraph> resultAtStmtContainingValue =
        context.getForwardPathEdges().getResultAtStmtContainingValue(calleeExitStmt, enteringFact);

    Set<IPathEdge<Unit, AccessGraph>> fwEdges = new HashSet<>();
    for (Pair<Unit, AccessGraph> startNode : resultAtStmtContainingValue.keys()) {
      if (!startNode.getO2().hasAllocationSite())
        continue;
      for (AccessGraph factToContinue : resultAtStmtContainingValue.get(startNode)) {
        fwEdges.add(new PathEdge<Unit, AccessGraph>(startNode.getO1(), startNode.getO2(),
            calleeExitStmt, factToContinue));
      }
    }
    if (fwEdges.isEmpty())
      return;
    context.getSubQuery().addBackwardIncoming(context.icfg.getMethodOf(calleeExitStmt),
        initialSelfLoopEdge.getStartNode(), callerEdge);

    Meeting meetingPoint = new Meeting(fwEdges, initialSelfLoopEdge);

    if (context.getSubQuery() != null) {
      context.getSubQuery().add(meetingPoint);
    }
  }


  private boolean isMeetableStmt(Unit target) {
    return context.getForwardPathEdges().hasMeetableEdges(target);
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> initialSelfLoop,
      SootMethod callee) {

    initialSelfLoop =
 new PathEdge<>(null, initialSelfLoop.factAtSource(),
        initialSelfLoop.getTarget(),
            initialSelfLoop.factAtTarget());
    performMeetCheckOnEnter(initialSelfLoop, prevEdge);
    Call handler = new Call(initialSelfLoop.factAtSource(), initialSelfLoop.getTarget(), prevEdge, callee);
    if (handler.isValid(context) && context.getSubQuery() != null)
      context.getSubQuery().add(handler);

    return Collections.singleton(initialSelfLoop);
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> calleeEdge, IPathEdge<Unit, AccessGraph> succEdge,
      IPathEdge<Unit, AccessGraph> incEdge) {
    SootMethod callee = context.icfg.getMethodOf(calleeEdge.getTarget());
    if (isQueryStartedInsideMethod(calleeEdge, callee)) {
      reachesStartPointOfStartMethod(calleeEdge);
    }
    return Collections.singleton(succEdge);
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
    performMeetCheck(prevEdge.getTarget(), succEdge);

    return Collections.singleton(succEdge);
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
      Unit returnSite) {
    return null;
  }


  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> summaryCallback(
      SootMethod methodThatNeedsSummary, IPathEdge<Unit, AccessGraph> edge) {
    return Collections.emptySet();
  }

  @Override
  public void cleanup() {}


}
