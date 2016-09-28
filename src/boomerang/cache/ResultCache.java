package boomerang.cache;

import heros.solver.Pair;

import java.util.Set;

import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.context.IContextRequester;
import boomerang.context.NoContextRequester;


public class ResultCache {
	private WithContextRequesterQueryCache RQcache;
	private RecursiveQueryCache RECQcache;
	private NoContextRequesterQueryCache NQcache;

  /**
   * Cache of queries for {@link boomerang.AliasFinder#findAliasAtStmtRec(AccessGraph, soot.Unit)}.
   * 
   * @return map of query to a set of {@link AccessGraph}
   */
	public RecursiveQueryCache recursiveQueryCache(){
		if(RECQcache == null)
			RECQcache = new RecursiveQueryCache();
		return RECQcache;
	}

  /**
   * Cache of queries for which have been computed with {@link NoContextRequester}.
   * 
   * @return map of query to {@link AliasResults} with {@link NoContextRequester}
   */
	public NoContextRequesterQueryCache contextlessQueryCache(){
		if(NQcache == null)
			NQcache = new NoContextRequesterQueryCache();
		return NQcache;
	}
	
  /**
   * Cache of queries for which have been computed with certain {@link IContextRequester}.
   * 
   * @return map of query to {@link AliasResults} with with certain {@link IContextRequester}
   */
	public WithContextRequesterQueryCache resolvedCache(){
		if(RQcache == null)
			RQcache= new WithContextRequesterQueryCache();
		return RQcache;
	}
	

	public class WithContextRequesterQueryCache extends CachedWorkinglist<Pair<Query,AliasResults>,AliasResults>{}
	public class NoContextRequesterQueryCache extends CachedWorkinglist<Query,AliasResults>{}
	public class RecursiveQueryCache extends CachedWorkinglist<Query,Set<AccessGraph>>{}
	
  /**
   * Completely clears the cache.
   */
	public void clear() {
		NQcache.clear();
		RECQcache.clear();
		RQcache.clear();
	}

	/**
	 * Cleans the cache. That means, all started but not yet finished queries are removed.
	 * Should be called when a {@link BoomerangTimeoutException} has been thrown. 
	 */
	public void clean() {
		recursiveQueryCache().removeStarted();
		contextlessQueryCache().removeStarted();
		resolvedCache().removeStarted();
	}
}
