package boomerang.forward;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.backward.BackwardFlowFunctions;
import boomerang.cache.Query;
import boomerang.cache.ResultCache.NoContextRequesterQueryCache;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import boomerang.pointsofindirection.Return;
import boomerang.pointsofindirection.Unbalanced;
import heros.FlowFunction;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NopStmt;

class ForwardPathEdgeFunctions extends AbstractPathEdgeFunctions {

  private HashMap<Pair<Unit, AccessGraph>, Set<Unit>> restrictedDtoPath = new HashMap<>();
  private Set<Pair<SootMethod, AccessGraph>> unrestrictedMethodsAndDp = new HashSet<>();
  private HashSet<IPathEdge<Unit, AccessGraph>> matchingAllocationIncomingEdge;
  private Multimap<Pair<Unit, AccessGraph>, IPathEdge<Unit, AccessGraph>> fwToBwEdge = HashMultimap
      .create();


  ForwardPathEdgeFunctions(FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions,
      BoomerangContext c) {
    super(flowFunctions, c, Direction.FORWARD);
  }

  private boolean isActivePath(Unit target, Pair<Unit, AccessGraph> fwedge) {
	 SootMethod m = context.icfg.getMethodOf(target);
    if (unrestrictedMethodsAndDp.contains(new Pair<SootMethod, AccessGraph>(context.icfg
        .getMethodOf(target), fwedge.getO2()))) {
      return true;
    }
    return context.getSubQuery().visitedBackwardMethod(m);
  }

  void addToFwBwEdge(Pair<Unit, AccessGraph> node, IPathEdge<Unit, AccessGraph> edge) {
    this.fwToBwEdge.put(node, edge);
  }
  /**
   * Whenever the forward analysis reaches the end of a path (that is, the backward analysis has not
   * visited a certain statement), the forward edge is stored as a "meetable" edge. The backward
   * analysis performs check, if a statement has such meetable forward edges. These forward edges
   * are then supplied in the forward propagation. If the successor statement of an edge was visited
   * by the backward solver in the meanwhile, the analysis will then automatically continue there.
   * 
   * @param pathEdge
   */
  private void onPathendReached(IPathEdge<Unit, AccessGraph> pathEdge) {
    context.getForwardPathEdges().addMeetableEdge(pathEdge);
    if (pathEdge.factAtSource().hasAllocationSite())
      return;

    context.getForwardPathEdges().pauseEdge(pathEdge.getStartNode(), pathEdge);
  }


  /**
   * 
   * @param startPoint
   * @return
   */
  private Collection<IPathEdge<Unit, AccessGraph>> getPausedEdges(Pair<Unit, AccessGraph> startPoint) {
    Collection<IPathEdge<Unit, AccessGraph>> pauseEdges =
        context.getForwardPathEdges().getAndRemovePauseEdge(startPoint);
    if (!pauseEdges.isEmpty()) {
      context.debugger.continuePausedEdges(pauseEdges);
    }
    HashSet<IPathEdge<Unit, AccessGraph>> copy = new HashSet<>(pauseEdges);
    return copy;
  }


  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunction(
      IPathEdge<Unit, AccessGraph> prevEdge, Unit succ) {


    return super.normalFunction(prevEdge, succ);
  };

  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> callFunction(
      IPathEdge<Unit, AccessGraph> prevEdge, SootMethod callee, Unit calleeSp) {
    Unit callSite = prevEdge.getTarget();
    if (!isActivePath(callSite, prevEdge.getStartNode())) {
      // The call is done in the appropriate call2Return furnction
      // onPathendReached(parentedEdge);
      return Collections.emptySet();
    }

    return super.callFunction(prevEdge, callee, calleeSp);
  };



  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunction(
      IPathEdge<Unit, AccessGraph> prevEdge, Unit returnSite, SootMethod callee,
      IPathEdge<Unit, AccessGraph> incomingEdge) {
    return super.balancedReturnFunction(prevEdge, returnSite, callee, incomingEdge);
  };

  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunction(
      IPathEdge<Unit, AccessGraph> prevEdge, Unit returnSite, Collection<SootMethod> callees) {
    return super.call2ReturnFunction(prevEdge, returnSite, callees);
  }



  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunction(
      IPathEdge<Unit, AccessGraph> currEdge, Unit callSite, Unit returnSite, SootMethod callee) {

    // Unbalanced return only occurs when the start statement of the path edge is not the first
    // statement of the method, i.e. a NopStmt
    if (currEdge.getStart() instanceof NopStmt) {
      return Collections.emptySet();
    }
    // Retrieve the backward edges associated with the forward edge
    Collection<IPathEdge<Unit, AccessGraph>> collection = fwToBwEdge.get(currEdge.getStartNode());
    if (collection == null)
      return Collections.emptySet();


    matchingAllocationIncomingEdge = new HashSet<>();
    for (IPathEdge<Unit, AccessGraph> currBwEdge : collection) {
      if (context.getSubQuery() == null)
        return Collections.emptySet();
      // Retrieve the incoming set for the backward edges, to know, where the forward analysis can
      // return to in an unbalanced manner (forward analysis follows backward analysis, hence it
      // also should do so at returns.)
      Collection<IPathEdge<Unit, AccessGraph>> incomingMap =
          context.getSubQuery().backwardIncoming(currBwEdge.getStartNode(), callee);

      if (incomingMap == null)
        return Collections.emptySet();
      // check that parentsolver has callSite in incomingMap, Otherwise we do not need to return;
      for (IPathEdge<Unit, AccessGraph> inc : incomingMap) {
        Unit callSiteOfAlloc = inc.getTarget();
        if (callSiteOfAlloc.equals(callSite)) {
          matchingAllocationIncomingEdge.add(inc);
        }
      }
    }
    // There was no incoming in the ASolver for the appropriate callSite.
    if (matchingAllocationIncomingEdge == null || matchingAllocationIncomingEdge.isEmpty()) {
      return Collections.emptySet();
    }
    return super.unbalancedReturnFunction(currEdge, callSite, returnSite, callee);

  }

  private Collection<IPathEdge<Unit, AccessGraph>> onUnbalancedReturnWithNoSourceStmt(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
      Unit returnSite) {
    AccessGraph d1 = succEdge.factAtSource();


    Unit exitStmt = prevEdge.getTarget();

    SootMethod callee = context.bwicfg.getMethodOf(exitStmt);
    BackwardFlowFunctions allocAnalysisFlowFunctions = new BackwardFlowFunctions(context);
    assert context.bwicfg.isCallStmt(callSite);
    NoContextRequesterQueryCache cache = context.querycache.contextlessQueryCache();
    List<Unit> succsOf = context.bwicfg.getSuccsOf(callSite);
    for (Unit allocRetSiteC : succsOf) {
      FlowFunction<AccessGraph> returnFlowFunction =
          allocAnalysisFlowFunctions.getReturnFlowFunction(null, callSite, callee, allocRetSiteC);
      Set<AccessGraph> computeTargets = returnFlowFunction.computeTargets(d1);

      assert computeTargets.size() <= 1;
      for (AccessGraph ap : computeTargets) {
        for (IPathEdge<Unit, AccessGraph> p : this.matchingAllocationIncomingEdge) {
          PathEdge<Unit, AccessGraph> toContinue =
              new PathEdge<Unit, AccessGraph>(p.getStart(), p.factAtSource(), allocRetSiteC, ap);
          Query query = new Query(ap, callSite);
          if (!cache.isProcessing(query)) {
            context.getSubQuery().add(new Unbalanced(toContinue));
          }
        }
      }
    }

    return Collections.emptySet();
  };



  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
    assert prevEdge.getStartNode().equals(succEdge.getStartNode());
    if (!isActivePath(succEdge.getTarget(), succEdge.getStartNode())) {
      onPathendReached(succEdge);
      return Collections.emptySet();
    }
    return Collections.singleton(succEdge);
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
    assert prevEdge.getStartNode().equals(succEdge.getStartNode());
    if (!isActivePath(succEdge.getTarget(), succEdge.getStartNode())) {
      onPathendReached(succEdge);
      return Collections.emptySet();
    }
    sanitize(Collections.singleton(succEdge));
    return Collections.singleton(succEdge);
  }

  @Override
  protected Collection<IPathEdge<Unit, AccessGraph>> unbalancedReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
      Unit returnSite) {
    context.sanityCheckEdge(succEdge);
    context.sanityCheckEdge(prevEdge);

    AccessGraph d1 = prevEdge.factAtSource();
    Unit exitStmt = prevEdge.getTarget();
    SootMethod callee = context.bwicfg.getMethodOf(exitStmt);
    if (d1.hasAllocationSite()) {
      HashSet<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
      out.add(succEdge);
      if (succEdge.factAtTarget().getFieldCount() > 0) {
        out.addAll(createAliasEdges(callSite, succEdge, callee));
      }
      wrappWithMatchingIncoming(out);
      return out;
    }
    assert d1.isStatic() || context.isParameterOrThisValue(exitStmt, d1.getBase());
    Collection<IPathEdge<Unit, AccessGraph>> out =
        onUnbalancedReturnWithNoSourceStmt(prevEdge, succEdge, callSite, returnSite);
    wrappWithMatchingIncoming(out);
    sanitize(out);
    return out;
  }

  private Collection<? extends IPathEdge<Unit, AccessGraph>> createAliasEdges(Unit callSite,
      IPathEdge<Unit, AccessGraph> succEdge, SootMethod callee) {
    PathEdge<Unit, AccessGraph> succAliasEdge;
    Set<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
    out.add(succEdge);
    AccessGraph d = succEdge.factAtTarget();
    if (isOverridenByCall(d, callSite))
      return out;
    Return resHandler = new Return(callSite, d);

    if (resHandler.isValid(context) && context.addToDirectlyProcessed(resHandler)) {
      Set<AccessGraph> aliases = resHandler.process(context);
      for (AccessGraph alias : aliases) {
        if (isOverridenByCall(alias, callSite)) {
          continue;
        }
        context.validateInput(alias, callSite);
        succAliasEdge =
            new PathEdge<Unit, AccessGraph>(succEdge.getStart(), succEdge.factAtSource(),
                succEdge.getTarget(), alias);
        out.add(succAliasEdge);
        context.debugger.indirectFlowEdgeAtReturn(d, callSite, alias, succEdge.getTarget());
        context.addToDirectlyProcessed(new Return(callSite,alias));
      }
    }

    sanitize(out);
    return out;
  }

  private boolean isOverridenByCall(AccessGraph ap, Unit callSite) {
    if (ap.isStatic())
      return false;
    if (!(callSite instanceof AssignStmt))
      return false;
    if (callSite instanceof AssignStmt) {
      AssignStmt as = (AssignStmt) callSite;
      if (as.getLeftOp().equals(ap.getBase()))
        return true;
    }
    return false;
  }

  private void wrappWithMatchingIncoming(Collection<IPathEdge<Unit, AccessGraph>> succEdges) {
    for (IPathEdge<Unit, AccessGraph> succEdge : succEdges) {
      this.fwToBwEdge.putAll(succEdge.getStartNode(), this.matchingAllocationIncomingEdge);
    }
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge,
      IPathEdge<Unit, AccessGraph> incEdge) {

    if (!isActivePath(succEdge.getTarget(), succEdge.getStartNode())) {
      onPathendReached(succEdge);
      return Collections.emptySet();
    }


    // For balanced problems we continue with the path edge which actually was incoming!
    assert incEdge.getStartNode().equals(succEdge.getStartNode());
    if (succEdge.factAtTarget().getFieldCount() > 0 && !isIdentityEdge(prevEdge)) {
      sanitize(succEdge);
      return createAliasEdgesOnBalanced(incEdge.getTarget(), succEdge);
    }
    return Collections.singleton(succEdge);
  }

  private Collection<? extends IPathEdge<Unit, AccessGraph>> createAliasEdgesOnBalanced(
      Unit callSite, IPathEdge<Unit, AccessGraph> succEdge) {

    Set<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
    out.add(succEdge);
    AccessGraph d2 = succEdge.factAtTarget();
    PathEdge<Unit, AccessGraph> succAliasEdge;
    if (isOverridenByCall(d2, callSite))
      return out;
    Return resHandler = new Return(callSite, d2);

    if (resHandler.isValid(context) && context.addToDirectlyProcessed(resHandler)) {
      Set<AccessGraph> aliases = resHandler.process(context);
      for (AccessGraph alias : aliases) {
        if (isOverridenByCall(alias, callSite))
          continue;

        context.validateInput(alias, callSite);
        succAliasEdge =
            new PathEdge<Unit, AccessGraph>(succEdge.getStart(), succEdge.factAtSource(),
                succEdge.getTarget(), alias);
        out.add(succAliasEdge);
        context.debugger.indirectFlowEdgeAtReturn(d2, callSite, alias, succEdge.getTarget());
        context.addToDirectlyProcessed(new Return(callSite,alias));
      }
    }

    sanitize(out);
    return out;
  }


  private boolean isIdentityEdge(IPathEdge<Unit, AccessGraph> edge) {
    AccessGraph source = edge.factAtSource();
    AccessGraph target = edge.factAtTarget();
    if (source == null || target == null)
      return false;
    if (source.isStatic() || target.isStatic())
      return false;
    return source.getBase().equals(target.getBase())
        && target.getFirstField().equals(source.getFirstField());
  }

  @Override
  protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
      IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> initialSelfLoopEdge,
      SootMethod callee) {
    this.unrestrictedMethodsAndDp.add(new Pair<SootMethod, AccessGraph>(callee, initialSelfLoopEdge
        .factAtSource()));

    sanitize(Collections.singleton(initialSelfLoopEdge));
    return Collections.singleton(initialSelfLoopEdge);
  }

  @Override
  public Collection<IPathEdge<Unit, AccessGraph>> getEdgesOnHold(
      IPathEdge<Unit, AccessGraph> initialSelfLoop, IPathEdge<Unit, AccessGraph> edgeEnteringCallee) {
    SootMethod callee = context.icfg.getMethodOf(initialSelfLoop.getStart());
    this.unrestrictedMethodsAndDp.add(new Pair<SootMethod, AccessGraph>(callee, initialSelfLoop
        .factAtSource()));
    return getPausedEdges(initialSelfLoop.getStartNode());
  }

  public void cleanup() {
    super.cleanup();
    if (this.matchingAllocationIncomingEdge != null) {
      this.matchingAllocationIncomingEdge.clear();
      this.matchingAllocationIncomingEdge = null;
    }
    this.unrestrictedMethodsAndDp.clear();
    this.unrestrictedMethodsAndDp = null;
    this.fwToBwEdge.clear();
    this.fwToBwEdge = null;
    this.restrictedDtoPath.clear();
    this.restrictedDtoPath = null;

  }

  @Override
  public Collection<? extends IPathEdge<Unit, AccessGraph>> summaryCallback(
      SootMethod methodThatNeedsSummary, IPathEdge<Unit, AccessGraph> edge) {
    return Collections.emptySet();

  }
}
