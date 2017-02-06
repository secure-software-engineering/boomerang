package boomerang.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

class AliasWorklistEntry extends IWorklistEntry {

  private AliasResults accessPath;
  private Unit stmt;
  private ContextGraph contextGraph;
  private Context context;

  AliasWorklistEntry(Context context, AliasResults accessPath, IContextRequester requestor,
      BoomerangContext c) {
    super(c);
    this.context = context;
    this.requestor = requestor;
    this.stmt = context.getStmt();
    this.accessPath = accessPath;
  }

  public String toString() {
    return "context" + stmt + "" + stmt.hashCode() + " ->" + accessPath + "" + stmt.hashCode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
    result = prime * result + ((context == null) ? 0 : context.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AliasWorklistEntry other = (AliasWorklistEntry) obj;
    if (accessPath == null) {
      if (other.accessPath != null)
        return false;
    } else if (!accessPath.equals(other.accessPath))
      return false;
    if (context == null) {
      if (other.context != null)
        return false;
    } else if (!context.equals(other.context))
      return false;
    return true;
  }

  @Override
  public Set<IWorklistEntry> solve(ContextGraph contextGraph) {
    this.contextGraph = contextGraph;
    Set<IWorklistEntry> out = new HashSet<>();
    // for all access graph which have a parameter as a base value, and more than 1 field access.
    // There might be aliases due to aliases ahead of the callsite. This is computed here.
    out.addAll(solveCalleeToCaller());
    out.addAll(solveCallerToCallee());
    this.contextGraph = null;
    return out;
  }

  private Set<IWorklistEntry> solveLocal() {
    Set<IWorklistEntry> out = new HashSet<>();

    for (Pair<Unit, AccessGraph> key : new HashSet<>(accessPath.keySet())) {
      for (AccessGraph ap : new HashSet<>(accessPath.get(key))) {
        if (!dartcontext.isParameterOrThisValue(stmt, ap.getBase()))
          continue;
        if (ap.getFieldCount() < 1)
          continue;

        if (!dartcontext.isValidQuery(ap, stmt)) {
          continue;
        };
        if (dartcontext.isOutOfBudget())
          throw new BoomerangTimeoutException();
        AliasFinder dart = new AliasFinder(dartcontext);

        AliasResults res = dart.findAliasAtStmt(ap, stmt);
        AliasResults combined = new AliasResults();
        for (Pair<Unit, AccessGraph> newAllocSites : res.keySet()) {
          Collection<AccessGraph> newly = new HashSet<>(res.get(newAllocSites));

          newly.addAll(new HashSet<>(accessPath.get(key)));
          combined.putAll(newAllocSites, newly);
        }
        Set<IWorklistEntry> entry = contextGraph.storeResults(context, combined);
        if (entry != null)
          out.addAll(entry);
      }
    }
    return out;
  }

  private Set<IWorklistEntry> solveCallerToCallee() {
    if (stmt instanceof Stmt) {
      if (!contextGraph.isInitialContext(context)) {
        return solveDownwardsToCallee();
      } else {
        return solveLocal();
      }
    }

    throw new RuntimeException("");
  }

  private Set<IWorklistEntry> solveDownwardsToCallee() {
    Collection<Context> calleeContexts = contextGraph.getCallee(context);

    Set<IWorklistEntry> out = new HashSet<>();
    for (Pair<Unit, AccessGraph> key : new HashSet<>(accessPath.keySet())) {
      for (AccessGraph ap : new HashSet<>(accessPath.get(key))) {
        for (Context calleeContext : calleeContexts) {
          if (calleeContext.equals(context))
            continue;
          Unit stmtOfCallee = calleeContext.getStmt();

          SootMethod callee = dartcontext.icfg.getMethodOf(stmtOfCallee);
          Set<AccessGraph> targets =
              ContextResolver.getForwardTargetsFor(ap, stmt, callee, dartcontext);
          for (AccessGraph t : targets) {
            if (dartcontext.isOutOfBudget()) {
              throw new BoomerangTimeoutException();
            }
            if (!dartcontext.isValidQuery(t, stmtOfCallee)) {
              continue;
            };
            AliasFinder dart = new AliasFinder(dartcontext);

            AliasResults indirectInNewContext = dart.findAliasAtStmt(t, stmtOfCallee);
            Set<IWorklistEntry> entry =
                contextGraph.storeResults(calleeContext, indirectInNewContext);
            if (entry != null)
              out.addAll(entry);
          }
        }
      }
    }
    return out;
  }

  /**
   * Solve upwards pushes the results from callee to caller to see what still has to be computed.
   * Once the result are computed, the results are then pushed downwards by
   * {@link #solveCallerToCallee()}.
   * 
   * @return the set of worklist entries still to be computed.
   */
  private Set<IWorklistEntry> solveCalleeToCaller() {
    // get callers of that context.
    Collection<Context> callerContexts = contextGraph.getCaller(context);
    SootMethod callee = dartcontext.icfg.getMethodOf(stmt);

    Set<IWorklistEntry> out = new HashSet<>();
    for (Pair<Unit, AccessGraph> key : new HashSet<>(accessPath.keySet())) {
      for (AccessGraph apValue : new HashSet<>(accessPath.get(key))) {
        // for each key-> value (alloc -> alias) in the set
        if (!dartcontext.isParameterOrThisValue(stmt, apValue.getBase()))
          continue;

        // if the access graph does not have more than one field access, there can not be any
        // aliases outsite this method.
        if (apValue.getFieldCount() < 1)
          continue;

        for (final Context callerContext : callerContexts) {
          if (callerContext.equals(context))
            continue;
          Unit callerCallSite = callerContext.getStmt();

		for(WrappedSootField lastField:apValue.getLastField()){
          // we have to remove the last field, as all aliases of this access access graph without
          // the last field reflect back to the access graph.
          Set<AccessGraph> withoutLastFields = apValue.popLastField();
          for (AccessGraph withoutLastField : withoutLastFields) {
            Set<AccessGraph> targets =
                ContextResolver.getAllocationSiteTargetsFor(withoutLastField, callerCallSite,
                    callee, dartcontext);
            Set<AccessGraph> possibleNewAlias = new HashSet<>();

            for (final AccessGraph inCallerContext : targets) {
              if (dartcontext.isOutOfBudget()) {
                throw new BoomerangTimeoutException();
              }
              Set<AccessGraph> preAliases = getRecursiveResults(callerContext, inCallerContext);

              Set<AccessGraph> aliasWithField =
                  AliasResults.appendField(preAliases, lastField, dartcontext);
              for (AccessGraph aliasInCallerContext : aliasWithField) {
                Set<AccessGraph> forwardTargetsFor =
                    ContextResolver.getForwardTargetsFor(aliasInCallerContext, callerCallSite,
                        callee, dartcontext);
                possibleNewAlias.addAll(forwardTargetsFor);
              }
            }
            AliasResults res = new AliasResults();
            res.putAll(key, possibleNewAlias);
            Set<IWorklistEntry> entry = contextGraph.storeResults(context, res);
            if (entry != null)
              out.addAll(entry);
          }
          }
        }

      }
    }
    return out;
  }
}
