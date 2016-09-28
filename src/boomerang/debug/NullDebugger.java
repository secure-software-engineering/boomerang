package boomerang.debug;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
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
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class NullDebugger implements
		IFDSDebugger<Unit, AccessGraph, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>>, IBoomerangDebugger {

	@Override
	public void addIncoming(Direction dir, SootMethod callee, Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
	}

	@Override
	public void normalFlow(Direction dir, Unit start, AccessGraph startFact, Unit unit, AccessGraph targetFact) {

	}

	@Override
	public void addSummary(Direction direction, SootMethod methodToSummary, IPathEdge<Unit, AccessGraph> summary) {
	}

	@Override
	public void callToReturn(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
	}

	@Override
	public void returnFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
	}

	@Override
	public void backwardStart(Direction backward, Unit stmt, AccessGraph d1, Unit s) {
	}

	@Override
	public void finishedQuery(Query q, AliasResults res) {
	}

	@Override
	public void startQuery(Query q) {
	}

	@Override
	public void onCurrentlyProcessingRecursiveQuery(Query q) {
	}

	@Override
	public void onLoadingQueryFromCache(Query q, AliasResults aliasResults) {
	}

	@Override
	public void onAllocationSiteReached(AssignStmt as, IPathEdge<Unit, AccessGraph> pe) {
	}

	@Override
	public void onProcessingMeetingPOI(Meeting meeting) {
	}

	@Override
	public void onProcessingFieldReadPOI(Read read) {
	}

	@Override
	public void continuePausedEdges(Collection<IPathEdge<Unit, AccessGraph>> pauseEdges) {
	}

	@Override
	public void onProcessAllocationPOI(Alloc alloc) {
	}

	@Override
	public void onProcessCallPOI(Call call) {

	}

	@Override
	public void onProcessReturnPOI(Return return1) {

	}

	@Override
	public void onProcessWritePOI(Write write) {

	}

	@Override
	public void onProcessUnbalancedReturnPOI(Unbalanced unbalanced) {
	}

	@Override
	public void onProcessingParamPOI(BackwardParameterTurnHandler backwardParameterTurnHandler) {
	}

	@Override
	public void onAliasQueryFinished(Query q, AliasResults res) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAliasTimeout(Query q) {
		
	}

	@Override
	public void onEnterCall(Unit n, Collection<? extends IPathEdge<Unit, AccessGraph>> nextCallEdges,
			IPathEdge<Unit, AccessGraph> incEdge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessCall(IPathEdge<Unit, AccessGraph> edge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessExit(IPathEdge<Unit, AccessGraph> edge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessNormal(IPathEdge<Unit, AccessGraph> edge) {
		// TODO Auto-generated method stub
		
	}
}
