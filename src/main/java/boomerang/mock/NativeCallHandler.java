package boomerang.mock;

import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import soot.Value;
import soot.jimple.Stmt;

public abstract class NativeCallHandler {

	public abstract Set<AccessGraph> getForwardValues(Stmt call, AccessGraph source, Value[] params);

	public abstract Set<AccessGraph> getBackwardValues(Stmt call, AccessGraph source, Value[] params);

}
