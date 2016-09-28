package boomerang.context;

import java.util.Set;

import soot.Unit;

public interface ContextRequester {
	public Set<Context> getContextFor(Context  c);
	public Context initialContexts(Unit stmt);
}
