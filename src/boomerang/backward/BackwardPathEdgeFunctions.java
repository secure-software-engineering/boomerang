package boomerang.backward;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Optional;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.forward.AbstractPathEdgeFunctions;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import boomerang.pointsofindirection.BackwardAliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import soot.SootMethod;
import soot.Unit;

class BackwardPathEdgeFunctions extends AbstractPathEdgeFunctions {
	BackwardPathEdgeFunctions(FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions, BoomerangContext context) {
		super(flowFunctions, context, Direction.BACKWARD);
	}

	/**
	 * The backward analysis does not to unbalanced returns. But still, a check
	 * is performed if the analysis should add an
	 * {@link BackwardParameterTurnHandler}.
	 */
//	public Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunction(
//			IPathEdge<Unit, AccessGraph> prevEdge, Unit callSite, Unit returnSite, SootMethod callee) {
//		// do not propagate further if the query was started inside this method.
////		if (isQueryStartedInsideMethod(prevEdge, callee)) {
//			// System.out.println("UnBALNACEd");
//			
////		}
//		return Collections.emptySet();
//	};


	/**
	 * Checks if the edges reaches a start point of a method, if the target fact
	 * is a method parameter (incl this) it adds the appropriate POI to let the
	 * analysis turn around.
	 * 
	 * @param edge
	 * @param callee
	 */
	private void reachesStartPointOfStartMethod(IPathEdge<Unit, AccessGraph> edge, SootMethod callee) {
		boolean doTurnaround = context.isParameterOrThisValue(edge.getTarget(), edge.factAtTarget().getBase())
				|| (edge.factAtTarget().isStatic() && context.trackStaticFields());
		if (doTurnaround) {
//			context.getSubQuery().add(new BackwardParameterTurnHandler(edge, callee));
		}
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		return Collections.singleton(succEdge);
	}

	

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, final IPathEdge<Unit, AccessGraph> initialSelfLoop, SootMethod callee) {

		AccessGraph d2 = initialSelfLoop.factAtTarget();
		final Unit returnSiteOfCall = initialSelfLoop.getTarget();
		if(d2.getLastField() != null && !d2.hasSetBasedFieldGraph() && !d2.isStatic()){
			for(final WrappedSootField field : d2.getLastField()){
				Set<AccessGraph> withoutLast = d2.popLastField();
				if(withoutLast == null)
					continue;
				for(AccessGraph subgraph : withoutLast){
					context.registerPOI(returnSiteOfCall, new PointOfIndirection(subgraph, returnSiteOfCall, context), new BackwardAliasCallback(context) {
						@Override
						public Optional<IPathEdge<Unit, AccessGraph>> createInjectableEdge(AccessGraph alias) {
							alias = alias.appendFields(new WrappedSootField[]{field});
							PathEdge<Unit, AccessGraph> edge = new PathEdge<Unit,AccessGraph>(null,initialSelfLoop.factAtSource(),returnSiteOfCall, alias);
							return Optional.<IPathEdge<Unit, AccessGraph>>of(edge);
						}
					});
				}
			}
		}
		return Collections.singleton( new PathEdge<>(null, initialSelfLoop.factAtSource(), initialSelfLoop.getTarget(),
				initialSelfLoop.factAtTarget()));
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> calleeEdge, IPathEdge<Unit, AccessGraph> succEdge,
			IPathEdge<Unit, AccessGraph> incEdge) {
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {

		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
			Unit returnSite) {
		if(!context.visitedBackwardMethod(context.icfg.getMethodOf(callSite))){
			reachesStartPointOfStartMethod(prevEdge, context.icfg.getMethodOf(prevEdge.getTarget()));
			return Collections.emptySet();
		}
		succEdge = new PathEdge<Unit,AccessGraph>(null,succEdge.factAtTarget(),succEdge.getTarget(),succEdge.factAtTarget());
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
