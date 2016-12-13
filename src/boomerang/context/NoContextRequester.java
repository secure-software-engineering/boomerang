package boomerang.context;

import java.util.Collections;
import java.util.Set;

import soot.Unit;

/**
 * The NoContextRequester is for analysis, where one wants to have aliases under the current context
 * only. The analysis will NOT continue at callsites of the method m where
 * the query statement s resides in. Note: If one uses NoContextRequestor, evaluating a query will
 * analyze the (transitive) callees of m but (if necessary) will turn around at the start statement
 * of m. Thus it constructs conditionally aliases.
 * 
 * @author Johannes Späth
 *
 */
public class NoContextRequester implements IContextRequester {

	@Override
	public Set<Context> getCallSiteOf(Context child) {
		return Collections.emptySet();
	}

	@Override
	public Context initialContext(Unit stmt) {
		return null;
	}

}
