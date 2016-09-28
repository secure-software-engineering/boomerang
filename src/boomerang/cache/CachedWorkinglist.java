package boomerang.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class CachedWorkinglist<A, B> {
	private ConcurrentHashMap<A, B> values = new ConcurrentHashMap<>();
	private Set<A> started = new HashSet<A>();
	
	public B getResults(A a){
		return values.get(a);
	}
	public void setResults(A a, B b){
		started.remove(a);
		values.put(a,b);
	}

	public boolean isDone(A a){
    return values.containsKey(a);
	}
	public boolean isProcessing(A a){
		return started.contains(a);
	}
	public void start(A a){
		started.add(a);
	}

  public void removeStarted() {
		started.clear();
	}
	public void clear(){
		started.clear();
		values.clear();
	}
}