/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE. All rights reserved. This
 * program and the accompanying materials are made available under the terms of the GNU Lesser
 * Public License v2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.solver.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import boomerang.preanalysis.FieldPreanalysis;
import heros.solver.IDESolver;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.util.queue.QueueReader;

/**
 * Interprocedural control-flow graph for the infoflow solver
 * 
 * @author Steven Arzt
 * @author Eric Bodden
 */
public class InfoflowCFG implements IInfoflowCFG {

	private static enum StaticFieldUse {
		Unknown, Unused, Read, Write, ReadWrite
	}

	protected final Map<SootMethod, Map<SootField, StaticFieldUse>> staticFieldUses = new ConcurrentHashMap<SootMethod, Map<SootField, StaticFieldUse>>();
	protected final Map<SootMethod, Boolean> methodSideEffects = new ConcurrentHashMap<SootMethod, Boolean>();

	public static final Set<SootMethod> METHODS_TO_STRING = new HashSet<>();
	public static final Set<SootMethod> METHODS_EQUALS = new HashSet<>();
	public static final Set<SootMethod> IGNORED_METHODS = new HashSet<>();

	protected final BiDiInterproceduralCFG<Unit, SootMethod> delegate;

	protected final LoadingCache<Unit, UnitContainer> unitToPostdominator = IDESolver.DEFAULT_CACHE_BUILDER
			.build(new CacheLoader<Unit, UnitContainer>() {
				@Override
				public UnitContainer load(Unit unit) throws Exception {
					SootMethod method = getMethodOf(unit);
					DirectedGraph<Unit> graph = delegate.getOrCreateUnitGraph(method);
					MHGPostDominatorsFinder<Unit> postdominatorFinder = new MHGPostDominatorsFinder<Unit>(graph);
					Unit postdom = postdominatorFinder.getImmediateDominator(unit);
					if (postdom == null)
						return new UnitContainer(method);
					else
						return new UnitContainer(postdom);
				}
			});

	protected final LoadingCache<SootMethod, Local[]> methodToUsedLocals = IDESolver.DEFAULT_CACHE_BUILDER
			.build(new CacheLoader<SootMethod, Local[]>() {
				@Override
				public Local[] load(SootMethod method) throws Exception {
					if (!method.isConcrete() || !method.hasActiveBody())
						return new Local[0];

					List<Local> lcs = new ArrayList<Local>(method.getParameterCount() + (method.isStatic() ? 0 : 1));

					for (Unit u : method.getActiveBody().getUnits())
						useBox: for (ValueBox vb : u.getUseBoxes()) {
							// Check for parameters
							for (int i = 0; i < method.getParameterCount(); i++) {
								if (method.getActiveBody().getParameterLocal(i) == vb.getValue()) {
									lcs.add((Local) vb.getValue());
									continue useBox;
								}
							}
						}

					// Add the "this" local
					if (!method.isStatic())
						lcs.add(method.getActiveBody().getThisLocal());

					return lcs.toArray(new Local[lcs.size()]);
				}
			});
	private FieldPreanalysis preanalysis;

	public InfoflowCFG() {
		this(true);
	}
	public InfoflowCFG(boolean exceptionAnalysis) {
		this(new JimpleBasedInterproceduralCFG(exceptionAnalysis));
	}

	public InfoflowCFG(BiDiInterproceduralCFG<Unit, SootMethod> delegate) {
		this.delegate = delegate;
		preanalysis();
	}

	private void preanalysis() {
//		preanalysis = new FieldPreanalysis(this);
		selectSpecialMethods();
	}

	private void selectSpecialMethods() {
		ReachableMethods reachableMethods = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> listener = reachableMethods.listener();
		while (listener.hasNext()) {
			SootMethod method = listener.next().method();
			if (method.getSubSignature().equals("java.lang.String toString()")) {
//				if(method.hasActiveBody())
//					method.setActiveBody(Jimple.v().newBody(method));
				METHODS_TO_STRING.add(method);
				IGNORED_METHODS.add(method);
			}
			if (method.getSubSignature().equals("boolean equals(java.lang.Object)")) {
//				if(method.hasActiveBody())
//					method.setActiveBody(Jimple.v().newBody(method));
				METHODS_EQUALS.add(method);
				IGNORED_METHODS.add(method);
			}
			if (method.getName().equals("hashCode")) {
//				if(method.hasActiveBody())
//					method.setActiveBody(Jimple.v().newBody(method));
				IGNORED_METHODS.add(method);
			}
		}
	}

	@Override
	public boolean isEqualsMethod(SootMethod method) {
		return METHODS_EQUALS.contains(method);
	}

	@Override
	public boolean isToStringMethod(SootMethod method) {
		return METHODS_TO_STRING.contains(method);
	}

	@Override
	public boolean isIgnoredMethod(SootMethod method) {
		return IGNORED_METHODS.contains(method);
	}

	@Override
	public UnitContainer getPostdominatorOf(Unit u) {
		return unitToPostdominator.getUnchecked(u);
	}

	// delegate methods follow

	@Override
	public SootMethod getMethodOf(Unit u) {
		return delegate.getMethodOf(u);
	}

	@Override
	public List<Unit> getSuccsOf(Unit u) {
		return delegate.getSuccsOf(u);
	}

	@Override
	public boolean isExitStmt(Unit u) {
		return delegate.isExitStmt(u);
	}

	@Override
	public boolean isStartPoint(Unit u) {
		return delegate.isStartPoint(u);
	}

	@Override
	public boolean isFallThroughSuccessor(Unit u, Unit succ) {
		return delegate.isFallThroughSuccessor(u, succ);
	}

	@Override
	public boolean isBranchTarget(Unit u, Unit succ) {
		return delegate.isBranchTarget(u, succ);
	}

	@Override
	public Collection<Unit> getStartPointsOf(SootMethod m) {
		return delegate.getStartPointsOf(m);
	}

	@Override
	public boolean isCallStmt(Unit u) {
		return delegate.isCallStmt(u);
	}

	@Override
	public Set<Unit> allNonCallStartNodes() {
		return delegate.allNonCallStartNodes();
	}

	@Override
	public Collection<SootMethod> getCalleesOfCallAt(Unit u) {
		return delegate.getCalleesOfCallAt(u);
	}

	@Override
	public Collection<Unit> getCallersOf(SootMethod m) {
		return delegate.getCallersOf(m);
	}

	@Override
	public Collection<Unit> getReturnSitesOfCallAt(Unit u) {
		return delegate.getReturnSitesOfCallAt(u);
	}

	@Override
	public Set<Unit> getCallsFromWithin(SootMethod m) {
		return delegate.getCallsFromWithin(m);
	}

	@Override
	public List<Unit> getPredsOf(Unit u) {
		return delegate.getPredsOf(u);
	}

	@Override
	public Collection<Unit> getEndPointsOf(SootMethod m) {
		return delegate.getEndPointsOf(m);
	}

	@Override
	public List<Unit> getPredsOfCallAt(Unit u) {
		return delegate.getPredsOf(u);
	}

	@Override
	public Set<Unit> allNonCallEndNodes() {
		return delegate.allNonCallEndNodes();
	}

	@Override
	public DirectedGraph<Unit> getOrCreateUnitGraph(SootMethod m) {
		return delegate.getOrCreateUnitGraph(m);
	}

	@Override
	public List<Value> getParameterRefs(SootMethod m) {
		return delegate.getParameterRefs(m);
	}

	@Override
	public boolean isReturnSite(Unit n) {
		return delegate.isReturnSite(n);
	}

	@Override
	public boolean isStaticFieldRead(SootMethod method, SootField variable) {
		return isStaticFieldUsed(method, variable, new HashSet<SootMethod>(), true);
	}

	@Override
	public boolean isStaticFieldUsed(SootMethod method, SootField variable) {
		return isStaticFieldUsed(method, variable, new HashSet<SootMethod>(), false);
	}

	private boolean isStaticFieldUsed(SootMethod method, SootField variable, Set<SootMethod> runList,
			boolean readOnly) {
		// Without a body, we cannot say much
		if (!method.hasActiveBody())
			return false;

		// Do not process the same method twice
		if (!runList.add(method))
			return false;

		// Do we already have an entry?
		Map<SootField, StaticFieldUse> entry = staticFieldUses.get(method);
		if (entry != null) {
			StaticFieldUse b = entry.get(variable);
			if (b != null && b != StaticFieldUse.Unknown) {
				if (readOnly)
					return b == StaticFieldUse.Read || b == StaticFieldUse.ReadWrite;
				else
					return b != StaticFieldUse.Unused;
			}
		}

		// Scan for references to this variable
		for (Unit u : method.getActiveBody().getUnits()) {
			if (u instanceof AssignStmt) {
				AssignStmt assign = (AssignStmt) u;

				if (assign.getLeftOp() instanceof StaticFieldRef) {
					SootField sf = ((StaticFieldRef) assign.getLeftOp()).getField();
					registerStaticVariableUse(method, sf, StaticFieldUse.Write);
					if (!readOnly && variable.equals(sf))
						return true;
				}

				if (assign.getRightOp() instanceof StaticFieldRef) {
					SootField sf = ((StaticFieldRef) assign.getRightOp()).getField();
					registerStaticVariableUse(method, sf, StaticFieldUse.Read);
					if (variable.equals(sf))
						return true;
				}
			}

			if (((Stmt) u).containsInvokeExpr())
				for (Iterator<Edge> edgeIt = Scene.v().getCallGraph().edgesOutOf(u); edgeIt.hasNext();) {
					Edge e = edgeIt.next();
					if (isStaticFieldUsed(e.getTgt().method(), variable, runList, readOnly))
						return true;
				}
		}

		// Variable is not read
		registerStaticVariableUse(method, variable, StaticFieldUse.Unused);
		return false;
	}

	private void registerStaticVariableUse(SootMethod method, SootField variable, StaticFieldUse fieldUse) {
		Map<SootField, StaticFieldUse> entry = staticFieldUses.get(method);
		StaticFieldUse oldUse;
		synchronized (staticFieldUses) {
			if (entry == null) {
				entry = new ConcurrentHashMap<SootField, StaticFieldUse>();
				staticFieldUses.put(method, entry);
				entry.put(variable, fieldUse);
				return;
			}

			oldUse = entry.get(variable);
			if (oldUse == null) {
				entry.put(variable, fieldUse);
				return;
			}
		}

		// This part is monotonic, so no need for synchronization
		StaticFieldUse newUse;
		switch (oldUse) {
		case Unknown:
		case Unused:
		case ReadWrite:
			newUse = fieldUse;
			break;
		case Read:
			newUse = (fieldUse == StaticFieldUse.Read) ? oldUse : StaticFieldUse.ReadWrite;
			break;
		case Write:
			newUse = (fieldUse == StaticFieldUse.Write) ? oldUse : StaticFieldUse.ReadWrite;
			break;
		default:
			throw new RuntimeException("Invalid field use");
		}
		entry.put(variable, newUse);
	}

	@Override
	public boolean hasSideEffects(SootMethod method) {
		return hasSideEffects(method, new HashSet<SootMethod>());
	}

	private boolean hasSideEffects(SootMethod method, Set<SootMethod> runList) {
		// Without a body, we cannot say much
		if (!method.hasActiveBody())
			return false;

		// Do not process the same method twice
		if (!runList.add(method))
			return false;

		// Do we already have an entry?
		Boolean hasSideEffects = methodSideEffects.get(method);
		if (hasSideEffects != null)
			return hasSideEffects;

		// Scan for references to this variable
		for (Unit u : method.getActiveBody().getUnits()) {
			if (u instanceof AssignStmt) {
				AssignStmt assign = (AssignStmt) u;

				if (assign.getLeftOp() instanceof FieldRef) {
					methodSideEffects.put(method, true);
					return true;
				}
			}

			if (((Stmt) u).containsInvokeExpr())
				for (Iterator<Edge> edgeIt = Scene.v().getCallGraph().edgesOutOf(u); edgeIt.hasNext();) {
					Edge e = edgeIt.next();
					if (hasSideEffects(e.getTgt().method(), runList))
						return true;
				}
		}

		// Variable is not read
		methodSideEffects.put(method, false);
		return false;
	}

	@Override
	public void notifyMethodChanged(SootMethod m) {
		if (delegate instanceof JimpleBasedInterproceduralCFG)
			((JimpleBasedInterproceduralCFG) delegate).initializeUnitToOwner(m);
	}

	@Override
	public boolean methodReadsValue(SootMethod m, Value v) {
		Local[] reads = methodToUsedLocals.getUnchecked(m);
		if (reads != null)
			for (Local l : reads)
				if (l == v)
					return true;
		return false;
	}

	@Override
	public boolean containsAllocSiteOfType(SootMethod method, Type type) {
		return (preanalysis == null ? true : preanalysis.containsAllocSiteOfType(method, type));
	}

	@Override
	public boolean accessesField(SootMethod method, SootField field) {
		return (preanalysis == null ? true : preanalysis.accessesField(method, field));
	}

	@Override
	public boolean writesToField(SootMethod method, SootField field) {
		return (preanalysis == null ? true : preanalysis.writesToField(method, field));
	}

	@Override
	public boolean isReachable(Unit u) {
		return false;
	}

}
