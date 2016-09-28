package boomerang.context;

import java.util.Collection;

import soot.Unit;

public interface IContextRequester {
	public Collection<Context> getCallSiteOf(Context child);
	public Context initialContext(Unit stmt);
}
