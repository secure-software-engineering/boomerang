package boomerang.debug;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.IFDSDebugger;
import boomerang.ifdssolver.IPathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public interface IBoomerangDebugger extends
    IFDSDebugger<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> {
  public void finishedQuery(Query q, AliasResults res);

  public void startQuery(Query q);

public void onCurrentlyProcessingRecursiveQuery(Query q);

public void onLoadingQueryFromCache(Query q, AliasResults aliasResults);

public void onAllocationSiteReached(AssignStmt as, IPathEdge<Unit, AccessGraph> pe);

public void onAliasQueryFinished(Query q, AliasResults res);

public void onAliasTimeout(Query q);

public void indirectFlowEdgeAtRead(AccessGraph source, Unit curr, AccessGraph ap, Unit succ);

public void indirectFlowEdgeAtWrite(AccessGraph source, Unit target, AccessGraph ag, Unit curr);

public void indirectFlowEdgeAtReturn(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite);
public void indirectFlowEdgeAtCall(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite);

public void setContext(BoomerangContext boomerangContext);
}
