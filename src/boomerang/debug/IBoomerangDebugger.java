package boomerang.debug;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.IFDSDebugger;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Alloc;
import boomerang.pointsofindirection.BackwardParameterTurnHandler;
import boomerang.pointsofindirection.Call;
import boomerang.pointsofindirection.Meeting;
import boomerang.pointsofindirection.Read;
import boomerang.pointsofindirection.Return;
import boomerang.pointsofindirection.Unbalanced;
import boomerang.pointsofindirection.Write;
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

public void onProcessingMeetingPOI(Meeting meeting);

public void onProcessingFieldReadPOI(Read read);

public void continuePausedEdges(Collection<IPathEdge<Unit, AccessGraph>> pauseEdges);

public void onProcessAllocationPOI(Alloc alloc);

public void onProcessCallPOI(Call call);

public void onProcessReturnPOI(Return return1);

public void onProcessWritePOI(Write write);

public void onProcessUnbalancedReturnPOI(Unbalanced unbalanced);

public void onProcessingParamPOI(BackwardParameterTurnHandler backwardParameterTurnHandler);

public void onAliasQueryFinished(Query q, AliasResults res);

public void onAliasTimeout(Query q);

public void indirectFlowEdgeAtRead(AccessGraph source, Unit curr, AccessGraph ap, Unit succ);

public void indirectFlowEdgeAtWrite(AccessGraph source, Unit target, AccessGraph ag, Unit curr);

public void indirectFlowEdgeAtReturn(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite);
public void indirectFlowEdgeAtCall(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite);
}
