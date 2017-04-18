package boomerang.context;

import soot.SootMethod;
import soot.Unit;

/**
 * The NoContextRequester is for analysis, where one wants to have aliases under the current context
 * only. By that, we mean, that the analysis will NOT continue at callsites of the method m where
 * the query statement s resides in. Note: If one uses NoContextRequestor, evaluating a query will
 * analyze the (transitive) callees of m but (if necessary) will turn around at the start statement
 * of m. Thus it constructs conditionally aliases.
 * 
 * @author Johannes Spaeth
 *
 */
public class NoContextRequester implements IContextRequester {
	@Override
	public boolean continueAtCallSite(Unit callSite, SootMethod callee) {
		return false;
	}

	@Override
	public boolean isEntryPointMethod(SootMethod method) {
		return false;
	}

}