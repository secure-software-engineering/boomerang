package boomerang.context;

import java.util.Collection;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import soot.Unit;

abstract class IWorklistEntry {
  protected IContextRequester requestor;
  protected final BoomerangContext dartcontext;

  IWorklistEntry(BoomerangContext context) {
    this.dartcontext = context;
  }

  protected Collection<AccessGraph> getRecursiveResults(final Context callerContext,
      AccessGraph inCallerContext) {

    Unit callerCallSite = callerContext.getStmt();
    AliasFinder dart = new AliasFinder(dartcontext);
    if (dartcontext.isOutOfBudget()) {
      throw new BoomerangTimeoutException();
    }
    Collection<AccessGraph> preAliases =
        dart.findAliasAtStmtRec(inCallerContext, callerCallSite, new IContextRequester() {
          @Override
          public Context initialContext(Unit stmt) {
            return callerContext;
          }

          @Override
          public Collection<Context> getCallSiteOf(Context child) {
            return requestor.getCallSiteOf(child);
          }
        });

    return preAliases;
  }



  public abstract Set<IWorklistEntry> solve(ContextGraph contextGraph);
}
