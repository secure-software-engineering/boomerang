package boomerang.mock;

import java.util.Collections;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;

public class DefaultForwardDataFlowMocker implements MockedDataFlow {
	public DefaultForwardDataFlowMocker(BoomerangContext dartContext) {
	}

	@Override
	public boolean flowInto(Unit callSite, AccessGraph source, InvokeExpr invokeExpr,Value[] params) {
		return true;
	}

	@Override
	public boolean handles(Unit callSite, InvokeExpr invokeExpr,AccessGraph source, Value[] params) {
		return false;
	}

	@Override
	public Set<AccessGraph> computeTargetsOverCall(Unit callSite,InvokeExpr invokeExpr,
			AccessGraph source, Value[] params, IPathEdge<Unit,AccessGraph> edge) {
		return Collections.singleton(source);
	}

}
