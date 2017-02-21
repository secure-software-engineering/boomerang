package boomerang.forward;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import boomerang.pointsofindirection.ForwardAliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;

class ForwardPathEdgeFunctions extends AbstractPathEdgeFunctions {

	ForwardPathEdgeFunctions(FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions, BoomerangContext c) {
		super(flowFunctions, c, Direction.FORWARD);
	}

	private boolean isActivePath(Unit target) {
		SootMethod m = context.icfg.getMethodOf(target);
		return context.visitedBackwardMethod(m);
	}


	/**
	 * Whenever the forward analysis reaches the end of a path (that is, the
	 * backward analysis has not visited a certain statement), the forward edge
	 * is stored as a "meetable" edge. The backward analysis performs check, if
	 * a statement has such meetable forward edges. These forward edges are then
	 * supplied in the forward propagation. If the successor statement of an
	 * edge was visited by the backward solver in the meanwhile, the analysis
	 * will then automatically continue there.
	 * 
	 * @param pathEdge
	 */
	private void onPathendReached(IPathEdge<Unit, AccessGraph> pathEdge) {
		context.getForwardPathEdges().addMeetableEdge(pathEdge);
	}


	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunction(IPathEdge<Unit, AccessGraph> prevEdge,
			Unit succ) {
		return super.normalFunction(prevEdge, succ);
	};

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> callFunction(IPathEdge<Unit, AccessGraph> prevEdge,
			SootMethod callee, Unit calleeSp) {
		Unit callSite = prevEdge.getTarget();
		if (!isActivePath(callSite)) {
			// The call is done in the appropriate call2Return furnction
			// onPathendReached(parentedEdge);
			return Collections.emptySet();
		}

		return super.callFunction(prevEdge, callee, calleeSp);
	};

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunction(
			IPathEdge<Unit, AccessGraph> prevEdge, Unit returnSite, SootMethod callee,
			IPathEdge<Unit, AccessGraph> incomingEdge) {
		return super.balancedReturnFunction(prevEdge, returnSite, callee, incomingEdge);
	};

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunction(IPathEdge<Unit, AccessGraph> prevEdge,
			Unit returnSite, Collection<SootMethod> callees) {
		return super.call2ReturnFunction(prevEdge, returnSite, callees);
	}

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> unbalancedReturnFunction(
			IPathEdge<Unit, AccessGraph> currEdge, Unit callSite, Unit returnSite, SootMethod callee) {

		// Unbalanced return only occurs when the start statement of the path
		// edge is not the first
		// statement of the method, i.e. a NopStmt
		if (!currEdge.factAtSource().hasAllocationSite()) {
			return Collections.emptySet();
		}
		context.addAsVisitedBackwardMethod(callee);
		return super.unbalancedReturnFunction(currEdge, callSite, returnSite, callee);

	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> normalFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		assert prevEdge.getStartNode().equals(succEdge.getStartNode());
		if (!isActivePath(succEdge.getTarget())) {
			onPathendReached(succEdge);
			return Collections.emptySet();
		}
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> call2ReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge) {
		assert prevEdge.getStartNode().equals(succEdge.getStartNode());
		if (!isActivePath(succEdge.getTarget())) {
			onPathendReached(succEdge);
			
			return Collections.emptySet();
		}
		sanitize(Collections.singleton(succEdge));
		return Collections.singleton(succEdge);
	}

	@Override
	protected Collection<IPathEdge<Unit, AccessGraph>> unbalancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, final IPathEdge<Unit, AccessGraph> succEdge, Unit callSite,
			Unit returnSite) {
		context.sanityCheckEdge(succEdge);
		context.sanityCheckEdge(prevEdge);

		AccessGraph d1 = prevEdge.factAtSource();
		Unit exitStmt = prevEdge.getTarget();
		HashSet<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
		if (d1.hasAllocationSite()) {
			out.add(succEdge);
			if (succEdge.factAtTarget().getFieldCount() > 0) {
				AccessGraph d2 = succEdge.factAtTarget();
				if(d2.getLastField() != null){
					for(final WrappedSootField field : d2.getLastField()){
						Set<AccessGraph> withoutLast = d2.popLastField();
						if(withoutLast == null)
							continue;
						for(AccessGraph subgraph : withoutLast){
							context.registerPOI(callSite, new PointOfIndirection(subgraph, callSite, context), new ForwardAliasCallback(context) {
								
								@Override
								public IPathEdge<Unit, AccessGraph> createInjectableEdge(AccessGraph alias) {
									alias = alias.appendFields(new WrappedSootField[]{field});
									return new PathEdge<Unit,AccessGraph>(succEdge.getStart(),succEdge.factAtSource(),succEdge.getTarget(), alias);
								}
							});
						}
					}
				}
			}
		}
		assert d1.isStatic() || context.isParameterOrThisValue(exitStmt, d1.getBase());
		sanitize(out);
		return out;
	}

	private boolean isOverridenByCall(AccessGraph ap, Unit callSite) {
		if (ap.isStatic())
			return false;
		if (!(callSite instanceof AssignStmt))
			return false;
		if (callSite instanceof AssignStmt) {
			AssignStmt as = (AssignStmt) callSite;
			if (as.getLeftOp().equals(ap.getBase()))
				return true;
		}
		return false;
	}


	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> balancedReturnFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> succEdge,
			IPathEdge<Unit, AccessGraph> incEdge) {

		if (!isActivePath(succEdge.getTarget())) {
			onPathendReached(succEdge);
			return Collections.emptySet();
		}

		// For balanced problems we continue with the path edge which actually
		// was incoming!
		assert incEdge.getStartNode().equals(succEdge.getStartNode());
		if (succEdge.factAtTarget().getFieldCount() > 0 && !isIdentityEdge(prevEdge)) {
			sanitize(succEdge);
			createAliasEdgesOnBalanced(incEdge.getTarget(), succEdge);
		}
		return Collections.singleton(succEdge);
	}

	private void createAliasEdgesOnBalanced(Unit callSite,
			final IPathEdge<Unit, AccessGraph> succEdge) {

		Set<IPathEdge<Unit, AccessGraph>> out = new HashSet<>();
		out.add(succEdge);
		AccessGraph d2 = succEdge.factAtTarget();
		if (isOverridenByCall(d2, callSite))
			return;
		if(d2.getLastField() == null)
			return;
		for(final WrappedSootField field : d2.getLastField()){
			Set<AccessGraph> withoutLast = d2.popLastField();
			if(withoutLast == null)
				continue;
			for(AccessGraph subgraph : withoutLast){
				context.registerPOI(callSite, new PointOfIndirection(subgraph, callSite, context), new ForwardAliasCallback(context) {
					
					@Override
					public IPathEdge<Unit, AccessGraph> createInjectableEdge(AccessGraph alias) {
						alias = alias.appendFields(new WrappedSootField[]{field});
						return new PathEdge<Unit,AccessGraph>(succEdge.getStart(),succEdge.factAtSource(),succEdge.getTarget(), alias);
					}
				});
			}
		}
	}

	private boolean isIdentityEdge(IPathEdge<Unit, AccessGraph> edge) {
		AccessGraph source = edge.factAtSource();
		AccessGraph target = edge.factAtTarget();
		if (source == null || target == null)
			return false;
		if (source.isStatic() || target.isStatic())
			return false;
		return source.getBase().equals(target.getBase()) && target.getFirstField().equals(source.getFirstField());
	}

	@Override
	protected Collection<? extends IPathEdge<Unit, AccessGraph>> callFunctionExtendor(
			IPathEdge<Unit, AccessGraph> prevEdge, IPathEdge<Unit, AccessGraph> initialSelfLoopEdge,
			SootMethod callee) {

		sanitize(Collections.singleton(initialSelfLoopEdge));
		context.addAsVisitedBackwardMethod(callee);
		return Collections.singleton(initialSelfLoopEdge);
	}

	@Override
	public Collection<? extends IPathEdge<Unit, AccessGraph>> summaryCallback(SootMethod methodThatNeedsSummary,
			IPathEdge<Unit, AccessGraph> edge) {
		return Collections.emptySet();
	}
}
