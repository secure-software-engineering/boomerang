package boomerang.context;

import java.util.Collection;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * In contrast to the {@link NoContextRequestor}, upon reaching the method start point of the
 * triggered query this requester yields the analysis to continue at ALL callsites.
 * 
 * @author Johannes Spaeth
 *
 */
public class AllCallersRequester implements
    IContextRequester {

	@Override
	public boolean continueAtCallSite(Unit callSite, SootMethod callee) {
		return true;
	}

	@Override
	public boolean isEntryPointMethod(SootMethod method) {
		return true;
	}
}
