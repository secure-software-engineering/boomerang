package boomerang.ifdssolver;

import java.util.Collection;

/**
 * Path edge functions allow to modifying the set of path edges which flow out of statements. Thus
 * one can directly interfere the creation process and modify the outflowing edges to be generated.
 * With earlier version of heros/IFDS one can only modify the data-flow fact at the target of the
 * path edge. Instead for this approach we also need to modify the complete path edge.
 * 
 * @author Johannes Spaeth
 *
 * @param <N> The type of Statements to be used (for Jimple Unit)
 * @param <D> The data flow fact
 * @param <M> The generic for a method.
 */
public interface PathEdgeFunctions<N, D, M> {
	public Collection<? extends IPathEdge<N,D>> normalFunction(IPathEdge<N,D> prevEdge, N succ);
	public Collection<? extends IPathEdge<N,D>> callFunction(IPathEdge<N,D> prevEdge, M callee, N calleeSp);
	public Collection<? extends IPathEdge<N,D>> balancedReturnFunction(IPathEdge<N,D> prevEdge, N returnSite, M callee, IPathEdge<N,D> incomingEdge);
	public Collection<? extends IPathEdge<N,D>> unbalancedReturnFunction(IPathEdge<N,D> prevEdge, N callSite, N returnSite, M callee);
	public Collection<? extends IPathEdge<N,D>> call2ReturnFunction(IPathEdge<N,D> prevEdge, N returnSite, Collection<M> callees);
	public Collection<? extends IPathEdge<N, D>> getEdgesOnHold(IPathEdge<N, D> initialSelfLoop,IPathEdge<N, D> edgeEnteringCallee);
	public void cleanup();
	public Collection<? extends IPathEdge<N, D>> summaryCallback(M methodThatNeedsSummary,IPathEdge<N, D> edge);
}
