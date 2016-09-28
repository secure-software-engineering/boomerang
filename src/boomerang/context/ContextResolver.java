package boomerang.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.backward.BackwardFlowFunctions;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.forward.ForwardFlowFunctions;
import boomerang.ifdssolver.PathEdge;
import heros.FlowFunction;
import soot.SootMethod;
import soot.Unit;

public class ContextResolver {

  private IContextRequester requestor;
  private Query query;
  private BoomerangContext dartcontext;

  public ContextResolver(IContextRequester req, Query query, BoomerangContext context) {
    this.query = query;
    this.requestor = req;
    this.dartcontext = context;
  }


  /**
   * Resolves the {@link AliasResults} at the given statement. This is done by a worklist
   * computation using the requester supplied at the construction time.
   * 
   * @param res The results which have been computed context-independently
   * @param stmt The statement at which the results hold.
   * @return
   */
  public AliasResults resolve(AliasResults res, Unit stmt) {
    if (requestor instanceof NoContextRequester)
      return res;

    Context initialContext = requestor.initialContext(stmt);
    ContextGraph contextGraph = new ContextGraph(requestor, dartcontext, query, initialContext);

    // store initial result and compute what has to be computed next.
    Set<IWorklistEntry> entry = contextGraph.storeResults(requestor.initialContext(stmt), res);
    LinkedList<IWorklistEntry> worklist = new LinkedList<>();
    worklist.addAll(entry);
    Set<IWorklistEntry> solved = new HashSet<>();
    while (!worklist.isEmpty()) {
      if (dartcontext.isOutOfBudget()) {
        throw new BoomerangTimeoutException();
      }
      IWorklistEntry we = worklist.pollFirst();

      if (solved.contains(we)) {
        continue;
      }

      // solve each worklist entry independently.
      Set<IWorklistEntry> tosolve = we.solve(contextGraph);
      solved.add(we);
      for (IWorklistEntry s : tosolve) {
        worklist.add(s);
      }
    }
    return contextGraph.extractResults(initialContext);

  }

  static Set<AccessGraph> getAllocationSiteTargetsFor(AccessGraph d1, Unit callSite,
      SootMethod callee, BoomerangContext dartcontext) {
    BackwardFlowFunctions allocAnalysisFlowFunctions = new BackwardFlowFunctions(dartcontext);
    FlowFunction<AccessGraph> returnFlowFunction =
        allocAnalysisFlowFunctions.getReturnFlowFunction(null, callSite, callee, null);
    Set<AccessGraph> targets = returnFlowFunction.computeTargets(d1);
    return targets;
  }

  static Set<AccessGraph> getForwardTargetsFor(AccessGraph d2, Unit callSite,
      SootMethod callee, BoomerangContext dartcontext) {
    Collection<Unit> calleeSps = dartcontext.icfg.getStartPointsOf(callee);
    Set<AccessGraph> factsInCallee = new HashSet<>();
    ForwardFlowFunctions ptsFunction = new ForwardFlowFunctions(dartcontext);
    for (Unit calleeSp : calleeSps) {
      FlowFunction<AccessGraph> callFlowFunction =
          ptsFunction.getCallFlowFunction(new PathEdge<Unit, AccessGraph>(callSite, null, callSite,
              null), callee, calleeSp);
      Set<AccessGraph> targets = callFlowFunction.computeTargets(d2);
      factsInCallee.addAll(targets);
    }
    return factsInCallee;
  }
}
