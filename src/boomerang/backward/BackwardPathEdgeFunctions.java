package boomerang.backward;

import java.util.Collection;
import java.util.Collections;

import boomerang.AliasResults;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.forward.AbstractPathEdgeFunctions;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.pointsofindirection.Alloc;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;

class BackwardPathEdgeFunctions extends AbstractPathEdgeFunctions {
	BackwardPathEdgeFunctions(FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions, BoomerangContext context) {
		super(flowFunctions, context, Direction.BACKWARD);
	}


	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, final IPathEdge<Unit, AccessGraph> initialSelfLoop,
			SootMethod callee) {

		AccessGraph targetFact = initialSelfLoop.factAtTarget();
		if(!targetFact.isStatic() && Scene.v().getPointsToAnalysis().reachingObjects(targetFact.getBase()).isEmpty()){
			return Collections.emptySet();
		}
		context.addAsVisitedBackwardMethod(callee);
		return Collections.singleton(new PathEdge<>(null, initialSelfLoop.factAtSource(), initialSelfLoop.getTarget(),
				initialSelfLoop.factAtTarget()));
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> calleeEdge, IPathEdge<Unit, AccessGraph> succEdge,
			IPathEdge<Unit, AccessGraph> incEdge) {

		AccessGraph targetFact = succEdge.factAtTarget();
		if(!targetFact.isStatic() && Scene.v().getPointsToAnalysis().reachingObjects(targetFact.getBase()).isEmpty()){
			return Collections.emptySet();
		}
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		context.addAsVisitedBackwardMethod(context.icfg.getMethodOf(succEdge.getTarget()));
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
			Unit returnSite) {
		if(callSite == null && returnSite == null){
			SootMethod callee = context.icfg.getMethodOf(prevEdge.getTarget());
			if(context.getContextRequester().isEntryPointMethod(callee)){
				Alloc alloc = new Alloc(prevEdge.factAtTarget(), prevEdge.getTarget(), callee,context, true);
				alloc.execute();
			}
			return Collections.emptySet();
		}
		succEdge = new PathEdge<Unit, AccessGraph>(null, succEdge.factAtTarget(), succEdge.getTarget(),
				succEdge.factAtTarget());
		AccessGraph targetFact = succEdge.factAtTarget();
		if(!targetFact.isStatic() && Scene.v().getPointsToAnalysis().reachingObjects(targetFact.getBase()).isEmpty()){
			return Collections.emptySet();
		}
		return Collections.singleton(succEdge);
	}

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> summaryCallback(SootMethod methodThatNeedsSummary,
			IPathEdge<Unit, AccessGraph> edge) {
		return Collections.emptySet();
	}

	@Override
	public void cleanup() {
	}

}
