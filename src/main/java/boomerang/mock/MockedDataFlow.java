package boomerang.mock;

import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;

public interface MockedDataFlow {
	public boolean flowInto(Unit callSite, AccessGraph source, InvokeExpr ie, Value[] params);
	public boolean handles(Unit callSite, InvokeExpr invokeExpr, AccessGraph source, Value[] params);
	public Set<AccessGraph> computeTargetsOverCall(Unit callSite, InvokeExpr invokeExpr,AccessGraph source, Value[] params, IPathEdge<Unit, AccessGraph> edge);
}
