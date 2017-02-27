package boomerang.backward;

import java.util.Collection;
import java.util.Collections;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.forward.AbstractPathEdgeFunctions;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
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
	// public Collection<? extends IPathEdge<Unit, AccessGraph>>
	// unbalancedReturnFunction(
	// IPathEdge<Unit, AccessGraph> prevEdge, Unit callSite, Unit returnSite,
	// SootMethod callee) {
	// // do not propagate further if the query was started inside this method.
	//// if (isQueryStartedInsideMethod(prevEdge, callee)) {
	// // System.out.println("UnBALNACEd");
	//
	//// }
	// return Collections.emptySet();
	// };


	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, final IPathEdge<Unit, AccessGraph> initialSelfLoop,
			SootMethod callee) {

		return Collections.singleton(new PathEdge<>(null, initialSelfLoop.factAtSource(), initialSelfLoop.getTarget(),
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
		succEdge = new PathEdge<Unit, AccessGraph>(null, succEdge.factAtTarget(), succEdge.getTarget(),
				succEdge.factAtTarget());
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
