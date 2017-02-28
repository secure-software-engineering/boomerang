package boomerang.context;

import java.util.Collection;

import soot.SootMethod;
import soot.Unit;

public interface IContextRequester {
	public boolean continueAtCallSite(Unit callSite, SootMethod callee);
	public boolean isEntryPointMethod(SootMethod method);
}
