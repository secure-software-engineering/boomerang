package boomerang.debug;

import java.util.Collection;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;

public class BoomerangDebugger implements IBoomerangDebugger {

  private BoomerangContext context;

  public BoomerangDebugger() {
  }

  @Override
  public void addIncoming(Direction direction, SootMethod callee, Pair<Unit, AccessGraph> pair,
      IPathEdge<Unit, AccessGraph> pe) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addSummary(Direction direction, SootMethod methodToSummary,
      IPathEdge<Unit, AccessGraph> summary) {
    // TODO Auto-generated method stub

  }

  @Override
  public void normalFlow(Direction dir, Unit start, AccessGraph startFact, Unit target,
      AccessGraph targetFact) {
    // TODO Auto-generated method stub

  }

  @Override
  public void callToReturn(Direction dir, Unit start, AccessGraph startFact, Unit target,
      AccessGraph targetFact) {
    // TODO Auto-generated method stub

  }

  @Override
  public void returnFlow(Direction dir, Unit start, AccessGraph startFact, Unit target,
      AccessGraph targetFact) {
    // TODO Auto-generated method stub

  }

  @Override
  public void backwardStart(Direction backward, Unit stmt, AccessGraph d1, Unit s) {
    // TODO Auto-generated method stub

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


  @Override
  public void finishedQuery(Query q, AliasResults res) {
    log("Finished query " + res);
  }

  @Override
  public void startQuery(Query q) {
    log("Start query " + q);
  }

  private void log(Object s) {
    System.out.println(s.toString());
  }

  @Override
  public void onCurrentlyProcessingRecursiveQuery(Query q) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onLoadingQueryFromCache(Query q, AliasResults aliasResults) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAllocationSiteReached(AssignStmt as, IPathEdge<Unit, AccessGraph> pe) {
    log("Allocation site reached " + as);
  }

  @Override
  public void onAliasQueryFinished(Query q, AliasResults res) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAliasTimeout(Query q) {
    // TODO Auto-generated method stub

  }

@Override
public void callFlow(Direction direction, Unit target, AccessGraph startFact, Unit calleeSp, AccessGraph targetFact) {
	// TODO Auto-generated method stub
	
}

@Override
public void indirectFlowEdgeAtRead(AccessGraph source, Unit curr, AccessGraph ap, Unit succ) {
	// TODO Auto-generated method stub
	
}

@Override
public void indirectFlowEdgeAtWrite(AccessGraph source, Unit target, AccessGraph ag, Unit curr) {
	// TODO Auto-generated method stub
	
}

@Override
public void indirectFlowEdgeAtReturn(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite) {
	// TODO Auto-generated method stub
	
}

@Override
public void indirectFlowEdgeAtCall(AccessGraph source, Unit callSite, AccessGraph alias, Unit returnSite) {
	// TODO Auto-generated method stub
	
}

@Override
public void setContext(BoomerangContext boomerangContext) {
	this.context = boomerangContext;
	
}

}
