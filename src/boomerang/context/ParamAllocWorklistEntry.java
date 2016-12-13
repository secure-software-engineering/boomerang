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
import boomerang.cache.Query;
import boomerang.cache.ResultCache.NoContextRequesterQueryCache;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;


/**
 * An worklist entry which will be created at a start point of a method, say foo(a). And if there
 * was a query which has been started within foo for the parameter a. It will then request for the
 * calling contexts to proceed with the computatiom within the appropriate callers.
 * 
 * @author Johannes Spaeth
 *
 */
class ParamAllocWorklistEntry extends IWorklistEntry {
  private Set<Pair<Unit, AccessGraph>> startpoints;
  private Context stmt;

  ParamAllocWorklistEntry(Context stmt, Set<Pair<Unit, AccessGraph>> startpoints,
      IContextRequester requestor, BoomerangContext context) {
    super(context);
    this.stmt = stmt;
    this.startpoints = startpoints;
    this.requestor = requestor;
  }

  @Override
  public String toString() {
    return "ParamSearch [startpoints=" + startpoints + ", stmt=" + stmt + "]";
  }

  @Override
  public Set<IWorklistEntry> solve(ContextGraph contextGraph) {
    Collection<Context> callSiteContexts = contextGraph.getCaller(stmt);
    SootMethod callee = dartcontext.icfg.getMethodOf(stmt.getStmt());
    Set<IWorklistEntry> out = new HashSet<>();
    // takes all holding access graphs, maps them to the appropriate contexts, tries to find the
    // allocation sites there. The results are then stored in the context graph.
    for (Pair<Unit, AccessGraph> p : startpoints) {
      if (dartcontext.isOutOfBudget()) {
        throw new BoomerangTimeoutException();
      }
      if (p.getO2().hasAllocationSite()) {
        continue;
      }
      for (final Context callSiteContext : callSiteContexts) {
        Unit callSite = callSiteContext.getStmt();
        if (stmt.equals(callSiteContext))
          continue;
        AccessGraph ap = p.getO2();
        assert ap.isStatic() || dartcontext.isParameterOrThisValue(stmt.getStmt(), ap.getBase());

        // Map access graph to the callers.
        Set<AccessGraph> targets =
            ContextResolver.getAllocationSiteTargetsFor(ap, callSite, callee, dartcontext);
        for (AccessGraph t : targets) {
          Set<AccessGraph> toSearch = new HashSet<>();
          if ((!t.isStatic() && t.getFieldCount() > 0) || (t.isStatic() && t.getFieldCount() > 1)) {
            WrappedSootField lastField = t.getLastField();
            Set<AccessGraph> withoutLastfields = t.popLastField();
            for (AccessGraph withoutLastfield : withoutLastfields) {
              Set<AccessGraph> rec = getRecursiveResults(callSiteContext, withoutLastfield);
              toSearch = AliasResults.appendField(rec, lastField, dartcontext);
            }
          } else {
            Query q = new Query(t, callSite, dartcontext.icfg.getMethodOf(callSite));
            NoContextRequesterQueryCache cache = dartcontext.querycache.contextlessQueryCache();
            if (!cache.isProcessing(q) || cache.isDone(q)) {
              toSearch.add(t);
            }
          }
          // For all access graph within the callers we start a new search.
          for (AccessGraph search : toSearch) {
            if (!dartcontext.isValidQuery(search, callSite)) {
              continue;
            };
            AliasFinder dart = new AliasFinder(dartcontext);
            AliasResults indirectInNewContext = dart.findAliasAtStmt(search, callSite);

            // Store the new results, compute the worklist entries which ought to be computed.
            Set<IWorklistEntry> stillToCompute =
                contextGraph.storeResults(callSiteContext, indirectInNewContext);

            if (stillToCompute != null)
              out.addAll(stillToCompute);
          }
        }

      }
    }
    return out;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((startpoints == null) ? 0 : startpoints.hashCode());
    result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
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
    ParamAllocWorklistEntry other = (ParamAllocWorklistEntry) obj;
    if (startpoints == null) {
      if (other.startpoints != null)
        return false;
    } else if (!startpoints.equals(other.startpoints))
      return false;
    if (stmt == null) {
      if (other.stmt != null)
        return false;
    } else if (!stmt.equals(other.stmt))
      return false;
    return true;
  }
}
