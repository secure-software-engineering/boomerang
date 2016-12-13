package boomerang.debug;

import java.util.Collection;
import java.util.HashSet;

import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Alloc;
import boomerang.pointsofindirection.BackwardParameterTurnHandler;
import boomerang.pointsofindirection.Call;
import boomerang.pointsofindirection.Meeting;
import boomerang.pointsofindirection.Read;
import boomerang.pointsofindirection.Return;
import boomerang.pointsofindirection.Unbalanced;
import boomerang.pointsofindirection.Write;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class BoomerangEfficiencyDebugger implements IBoomerangDebugger {
  private IInfoflowCFG icfg;
  private Collection<SootMethod> visitedMethods = new HashSet<>();
  private int propagationCount;
  private int loadedFromCache;

	public BoomerangEfficiencyDebugger(IInfoflowCFG icfg) {
    this.icfg = icfg;
	}
	
	@Override
	public void addIncoming(Direction direction, SootMethod callee, Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
		
	}

	@Override
	public void addSummary(Direction direction, SootMethod methodToSummary, IPathEdge<Unit, AccessGraph> summary) {
	}

	@Override
	public void normalFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
    visitedMethods.add(icfg.getMethodOf(target));
	}

	@Override
	public void callToReturn(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void returnFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void backwardStart(Direction backward, Unit stmt, AccessGraph d1, Unit s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishedQuery(Query q, AliasResults res) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startQuery(Query q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentlyProcessingRecursiveQuery(Query q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoadingQueryFromCache(Query q, AliasResults aliasResults) {
    loadedFromCache++;
	}

	@Override
	public void onAllocationSiteReached(AssignStmt as, IPathEdge<Unit, AccessGraph> pe) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessingMeetingPOI(Meeting meeting) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessingFieldReadPOI(Read read) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void continuePausedEdges(Collection<IPathEdge<Unit, AccessGraph>> pauseEdges) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessAllocationPOI(Alloc alloc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessCallPOI(Call call) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessReturnPOI(Return return1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessWritePOI(Write write) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessUnbalancedReturnPOI(Unbalanced unbalanced) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessingParamPOI(BackwardParameterTurnHandler backwardParameterTurnHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAliasQueryFinished(Query q, AliasResults res) {
    System.out
        .println(String.format("Visited methods: %s \nPropagationCount: %s\nLoaded from cache: %s",
            visitedMethods.size(), propagationCount, loadedFromCache));
	}

	@Override
	public void onAliasTimeout(Query q) {
    System.out.println("TIMEOUT!");
	}

	@Override
	public void onEnterCall(Unit n, Collection<? extends IPathEdge<Unit, AccessGraph>> nextCallEdges,
			IPathEdge<Unit, AccessGraph> incEdge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessCall(IPathEdge<Unit, AccessGraph> edge) {
    propagationCount++;
	}

	@Override
	public void onProcessExit(IPathEdge<Unit, AccessGraph> edge) {
    propagationCount++;
	}

	@Override
	public void onProcessNormal(IPathEdge<Unit, AccessGraph> edge) {
    propagationCount++;
	}

}
