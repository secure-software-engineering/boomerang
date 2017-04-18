/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden. Copyright (c) 2013 Tata Consultancy Services & Ecole
 * Polytechnique de Montreal All rights reserved. This program and the accompanying materials are
 * made available under the terms of the GNU Lesser Public License v2.1 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Eric Bodden - initial API and implementation Marc-Andre Laverdiere-Papineau - Fixed
 * race condition Steven Arzt - Created FastSolver implementation Johannes Spaeth - Updates
 * necessary for AliasFinder
 ******************************************************************************/
package boomerang.ifdssolver;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

/**
 * A solver for an {@link IFDSTabulationProblem}. This solver is not based on
 * the IDESolver implementation in Heros for performance reasons.
 * 
 * @param <N>
 *            The type of nodes in the interprocedural control-flow graph.
 *            Typically {@link Unit}.
 * @param <D>
 *            The type of data-flow facts to be computed by the tabulation
 *            problem.
 * @param <M>
 *            The type of objects used to represent methods. Typically
 *            {@link SootMethod}.
 * @param <I>
 *            The type of inter-procedural control-flow graph being used.
 * @see IFDSTabulationProblem
 */
public abstract class IFDSSolver<N, D, M, I extends BiDiInterproceduralCFG<N, M>> {

	private long propagationCount;
	protected LinkedList<PathEdgeProcessingTask> worklist = new LinkedList<PathEdgeProcessingTask>();
	protected IPathEdges<N, D, M, I> pathEdges;
	protected PathEdgeFunctions<N, D, M> pathEdgeFunctions;
	protected IIncomings<N, M, D> incomings;
	protected final I icfg;
	protected final IFDSDebugger<N, D, M, I> debugger;
	protected ISummaries<N, M, D> summaries;

	public enum PropagationType {
		Normal, Call2Return, CallEnter, BalancedReturn, UnbalancedReturn
	}

	private IFDSTabulationProblem<N, D, M, I> tabulationProblem;

	private Direction direction;

	/**
	 * Creates a solver for the given problem, which caches flow functions and
	 * edge functions. The solver must then be started by calling
	 * {@link #solve()}.
	 */
	public IFDSSolver(DefaultIFDSTabulationProblem<N, D, M, I> tabulationProblem, IFDSDebugger<N, D, M, I> debug) {
		this.pathEdgeFunctions = tabulationProblem.pathEdgeFunctions();
		this.tabulationProblem = tabulationProblem;
		this.direction = tabulationProblem.getDirection();
		this.icfg = tabulationProblem.interproceduralCFG();
		this.debugger = debug;
		propagationCount = 0;
	}

	/**
	 * Lines 13-20 of the algorithm; processing a call site in the caller's
	 * context.
	 * 
	 * For each possible callee, registers incoming call edges. Also propagates
	 * call-to-return flows and summarized callee flows within the caller.
	 * 
	 * @param incEdge
	 *            an edge whose target node resembles a method call
	 */
	protected <F extends IPathEdge<N, D>> void processCall(F incEdge) {
		final N n = incEdge.getTarget(); // a call node; line 14...
		final D d2 = incEdge.factAtTarget();
		assert d2 != null;
		Collection<N> returnSiteNs = icfg.getReturnSitesOfCallAt(n);
		// for each possible callee
		Collection<M> callees = icfg.getCalleesOfCallAt(n);

		for (M sCalledProcN : callees) { // still line 14

			Collection<N> startPointsOf = icfg.getStartPointsOf(sCalledProcN);
			// for each result node of the call-flow function
			for (N sP : startPointsOf) {
				// compute the call-flow function
				Collection<? extends IPathEdge<N, D>> nextCallEdges = pathEdgeFunctions.callFunction(incEdge,
						sCalledProcN, sP);
				if (!nextCallEdges.isEmpty()) {
					debugger.onEnterCall(n, nextCallEdges, incEdge);
				}
				for (IPathEdge<N, D> nextCallEdge : nextCallEdges) {
					// for each callee's start point(s)
					// create initial self-loop
					if (!addIncoming(sCalledProcN, nextCallEdge, incEdge)) {
						continue;
					}
					// line 15.2
					Collection<IPathEdge<N, D>> endSumm = endSummary(sCalledProcN, nextCallEdge.getStartNode());
					Collection<? extends IPathEdge<N, D>> edgesOnHold = pathEdgeFunctions.getEdgesOnHold(nextCallEdge,
							incEdge);
					for (IPathEdge<N, D> edgeOnHold : edgesOnHold) {
						scheduleEdgeProcessing(edgeOnHold); // line 15
					}
					// still line 15.2 of Naeem/Lhotak/Rodriguez
					// for each already-queried exit value <eP,d4> reachable
					// from <sP,d3>,
					// create new caller-side jump functions to the return sites
					// because we have observed a potentially new incoming edge
					// into <sP,d3>
					if (endSumm.isEmpty()) {
						propagate(nextCallEdge, PropagationType.CallEnter); // line
																			// 15
					} else {

						for (IPathEdge<N, D> summaryEdge : endSumm) {
							// for each return site
							for (N retSiteN : returnSiteNs) {
								// compute return-flow function
								Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions
										.balancedReturnFunction(summaryEdge, retSiteN, sCalledProcN, incEdge);

								// for each target value of the function
								for (IPathEdge<N, D> nextEdge : nextEdges) {
									// If we have not changed anything in the
									// callee, we do not need the facts
									// from there. Even if we change something:
									// If we don't need the concrete
									// path, we can skip the callee in the
									// predecessor chain
									propagate(nextEdge, PropagationType.BalancedReturn);
								}
							}
						}
					}
				}

			}
		}
		// line 17-19 of Naeem/Lhotak/Rodriguez
		// process intra-procedural flows along call-to-return flow functions
		for (N returnSiteN : returnSiteNs) {
			Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions.call2ReturnFunction(incEdge,
					returnSiteN, callees);
			for (IPathEdge<N, D> nextEdge : nextEdges) {
				propagate(nextEdge, PropagationType.Call2Return);
			}
		}
		// If there is no callee or the callee has no active body
		if (callees.isEmpty()) {
			// line 17-19 of Naeem/Lhotak/Rodriguez
			// process intra-procedural flows along call-to-return flow
			// functions
			for (N returnSiteN : returnSiteNs) {
				Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions.call2ReturnFunction(incEdge,
						returnSiteN, callees);

				for (IPathEdge<N, D> nextEdge : nextEdges) {
					propagate(nextEdge, PropagationType.Call2Return);
				}
			}
		}
	}

	/**
	 * Lines 21-32 of the algorithm.
	 * 
	 * Stores callee-side summaries. Also, at the side of the caller, propagates
	 * intra-procedural flows to return sites using those newly computed
	 * summaries.
	 * 
	 * @param summaryEdge
	 *            an edge whose target node resembles a method exits
	 */
	protected <F extends IPathEdge<N, D>> void processExit(F summaryEdge) {
		final N n = summaryEdge.getTarget(); // an exit node; line 21...
		M methodThatNeedsSummary = icfg.getMethodOf(n);

		addEndSummary(methodThatNeedsSummary, summaryEdge);

		Set<? extends IPathEdge<N, D>> incomings = incoming(summaryEdge.getStartNode(), methodThatNeedsSummary);

		Collection<? extends IPathEdge<N, D>> appendToSummary = pathEdgeFunctions
				.summaryCallback(methodThatNeedsSummary, summaryEdge);

		for (IPathEdge<N, D> nextEdge : appendToSummary) {
			propagate(nextEdge, PropagationType.BalancedReturn);
		}
		// logger.trace("Processing exit of {} with {}", methodThatNeedsSummary,
		// edge);

		for (IPathEdge<N, D> inc : incomings) {
			N c = inc.getTarget();
			for (N retSiteC : icfg.getReturnSitesOfCallAt(c)) {
				Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions.balancedReturnFunction(summaryEdge,
						retSiteC, methodThatNeedsSummary, inc);

				for (IPathEdge<N, D> nextEdge : nextEdges) {
					propagate(nextEdge, PropagationType.BalancedReturn);
				}
			}
		}

		// handling for unbalanced problems where we return out of a method with
		// a fact for which we
		// have no incoming flow
		if (incomings.isEmpty()) {
			Collection<N> callers = icfg.getCallersOf(methodThatNeedsSummary);
			for (N c : callers) {
				for (N retSiteC : icfg.getReturnSitesOfCallAt(c)) {
					Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions
							.unbalancedReturnFunction(summaryEdge, c, retSiteC, methodThatNeedsSummary);
					for (IPathEdge<N, D> nextEdge : nextEdges) {
						propagate(nextEdge, PropagationType.UnbalancedReturn);
					}
				}
			}
			// in cases where there are no callers, the return statement would
			// normally not be processed
			// at all;
			// this might be undesirable if the flow function has a side effect
			// such as registering a
			// taint;
			// instead we thus call the return flow function will a null caller
			if (callers.isEmpty()) {
				pathEdgeFunctions.unbalancedReturnFunction(summaryEdge, null, null, methodThatNeedsSummary);
			}
		}
	}

	/**
	 * Lines 33-37 of the algorithm. Simply propagate normal, intra-procedural
	 * flows.
	 * 
	 * @param edge
	 */
	protected <F extends IPathEdge<N, D>> void processNormalFlow(F edge) {
		final N n = edge.getTarget();
		List<N> succsOf = icfg.getSuccsOf(n);
		for (N m : succsOf) {
			Collection<? extends IPathEdge<N, D>> nextEdges = pathEdgeFunctions.normalFunction(edge, m);
			for (IPathEdge<N, D> nextEdge : nextEdges) {
				propagate(nextEdge, PropagationType.Normal);
			}
		}
	}

	public boolean propagate(IPathEdge<N, D> edge, PropagationType t) {
		boolean hasAlreadyProcessed = pathEdges.hasAlreadyProcessed(edge);
		registerEdge(edge);
		assert pathEdges.hasAlreadyProcessed(edge);
		if (!hasAlreadyProcessed) {
			propagationCount++;
			scheduleEdgeProcessing(edge);
		}

		return hasAlreadyProcessed;
	}

	public void registerEdge(IPathEdge<N, D> edge) {
		onRegister(edge);
		pathEdges.register(edge);
	}

	public abstract void onRegister(IPathEdge<N, D> edge);

	/**
	 * Dispatch the processing of a given edge. It may be executed in a
	 * different thread.
	 * 
	 * @param edge
	 *            the edge to process
	 */
	protected void scheduleEdgeProcessing(IPathEdge<N, D> edge) {
		if (worklist != null)
			worklist.add(new PathEdgeProcessingTask(edge));
//		System.out.println(direction + " " + edge);
	}

	protected class PathEdgeProcessingTask implements Runnable {
		private final IPathEdge<N, D> edge;

		public PathEdgeProcessingTask(IPathEdge<N, D> edge) {
			this.edge = edge;
		}

		public void run() {
			if (icfg.isCallStmt(edge.getTarget())) {
				debugger.onProcessCall(edge);
				processCall(edge);
			} else {
				// note that some statements, such as "throw" may be
				// both an exit statement and a "normal" statement
				if (icfg.isExitStmt(edge.getTarget())) {
					debugger.onProcessExit(edge);
					processExit(edge);
				}
				if (!icfg.getSuccsOf(edge.getTarget()).isEmpty()) {
					debugger.onProcessNormal(edge);
					processNormalFlow(edge);
				}
			}

		}

		public String toString() {
			return edge.toString();
		}
	}

	public BiDiInterproceduralCFG<N, M> getICFG() {
		return icfg;
	}

	public void awaitExecution() {
		while (worklist != null && !worklist.isEmpty()) {
			PathEdgeProcessingTask task = worklist.poll();
			task.run();
		}
	}

	protected Collection<IPathEdge<N, D>> endSummary(M m, Pair<N, D> d3) {
		Collection<IPathEdge<N, D>> endSummary = summaries.endSummary(m, d3);
		if (endSummary == null)
			return new HashSet<>();
		return new HashSet<>(endSummary);
	}

	protected void addEndSummary(M m, IPathEdge<N, D> edge) {
		debugger.addSummary(direction, m, edge);
		summaries.addEndSummary(m, edge);
	}

	public Set<? extends IPathEdge<N, D>> incoming(Pair<N, D> pair, M m) {
		if (incomings == null) {
			return new HashSet<>();
		}
		return new HashSet<>(incomings.incoming(pair, m));
	};

	public boolean addIncoming(M callee, IPathEdge<N, D> nextCallEdge, IPathEdge<N, D> incEdge) {
		debugger.addIncoming(direction, callee, nextCallEdge.getTargetNode(), incEdge);
		onRegister(incEdge);
		tabulationProblem.onSolverAddIncoming(callee, nextCallEdge.getStartNode(), incEdge);
		return incomings.addIncoming(callee, nextCallEdge.getStartNode(), incEdge);
	}

	public void cleanup() {
		if (incomings != null)
			this.incomings.clear();
		if (worklist != null)
			this.worklist.clear();
		if (tabulationProblem != null)
			tabulationProblem.cleanup();
		this.incomings = null;
		this.summaries = null;
		this.pathEdges = null;
		this.tabulationProblem = null;
		this.pathEdgeFunctions = null;
		this.worklist = null;
	}

	public boolean isDone() {
		return worklist.isEmpty();
	}

	public IPathEdges<N, D, M, I> getPathEdges() {
		return this.pathEdges;
	}

	public void inject(IPathEdge<N, D> edge, PropagationType normal) {
		if (!pathEdges.hasAlreadyProcessed(edge)){
			propagate(edge, normal);
		}
	}
}
