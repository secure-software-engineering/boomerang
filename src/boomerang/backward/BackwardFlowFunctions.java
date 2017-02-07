package boomerang.backward;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.forward.AbstractFlowFunctions;
import boomerang.forward.ForwardFlowFunctions;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Alloc;
import boomerang.pointsofindirection.Read;
import heros.FlowFunction;
import soot.Local;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThrowStmt;

public class BackwardFlowFunctions extends AbstractFlowFunctions
		implements FlowFunctions<Unit, AccessGraph, SootMethod> {

	public BackwardFlowFunctions(BoomerangContext context) {
		this.context = context;
	}

	@Override
	public FlowFunction<AccessGraph> getNormalFlowFunction(final IPathEdge<Unit, AccessGraph> edge, final Unit succ) {
		final Unit curr = edge.getTarget();
		final SootMethod method = context.icfg.getMethodOf(curr);
		context.getSubQuery().addAsVisitedBackwardMethod(method);
		final Local thisLocal = method.isStatic() ? null : method.getActiveBody().getThisLocal();
		return new FlowFunction<AccessGraph>() {
			@Override
			public Set<AccessGraph> computeTargets(AccessGraph source) {
				assert thisLocal == null || !source.baseMatches(thisLocal)
						|| ForwardFlowFunctions.hasCompatibleTypesForCall(source, method.getDeclaringClass());
				assert !context.isIgnoredMethod(context.icfg.getMethodOf(curr));
				if (!(curr instanceof AssignStmt)) {
					return Collections.singleton(source);
				}
				if (curr instanceof ThrowStmt)
					return Collections.emptySet();
				AssignStmt as = (AssignStmt) curr;
				Value leftOp = as.getLeftOp();
				Value rightOp = as.getRightOp();
				if (leftOp instanceof Local && source.baseMatches(leftOp)) {
					if (isAllocationSite(rightOp)) {
						if (source.getFieldCount() > 0 && !source.firstFieldMustMatch(AliasFinder.ARRAY_FIELD)) {
							return Collections.emptySet();
						}

						allocationSiteReached(edge, as, rightOp);
						return Collections.emptySet();
					} else if (rightOp instanceof CastExpr) {
						CastExpr castExpr = (CastExpr) rightOp;
						Value op = castExpr.getOp();
						if (op instanceof Local) {
							if (typeCompatible(((Local) op).getType(), source.getBaseType())) {
								return Collections
										.singleton(source.deriveWithNewLocal((Local) op, source.getBaseType()));
							} else
								return Collections.emptySet();
						}
					} else if (rightOp instanceof Local) {
						if (typeCompatible(((Local) rightOp).getType(), source.getBaseType())) {
							return Collections
									.singleton(source.deriveWithNewLocal((Local) rightOp, source.getBaseType()));
						} else {
							return Collections.emptySet();
						}
					} else if (rightOp instanceof InstanceFieldRef) {
						// d = e.f, source = d.c ;
						InstanceFieldRef fr = (InstanceFieldRef) rightOp;

						if (fr.getBase() instanceof Local) {
							Local base = (Local) fr.getBase();
							Read handler = new Read(edge, base, fr.getField(), succ, source);
							if (context.getSubQuery() != null)
								context.getSubQuery().add(handler);

							Set<AccessGraph> out = new HashSet<>();
							WrappedSootField newFirstField = new WrappedSootField(fr.getField(), source.getBaseType(),
									curr);
							AccessGraph ap = source.deriveWithNewLocal(base, base.getType());
							if (ap.canPrepend(newFirstField)) {
								AccessGraph prependField = ap.prependField(newFirstField);
								out.add(prependField);
							}
							return out;
						}
					} else if (rightOp instanceof ArrayRef) {
						ArrayRef arrayRef = (ArrayRef) rightOp;
						Local base = (Local) arrayRef.getBase();
						Read handler = new Read(edge, base, AliasFinder.ARRAY_FIELD, succ, source);
						if (context.getSubQuery() != null)
							context.getSubQuery().add(handler);

						Set<AccessGraph> out = new HashSet<>();
						AccessGraph prependField = source.prependField(
								new WrappedSootField(AliasFinder.ARRAY_FIELD, source.getBaseType(), curr));
						AccessGraph ap = prependField.deriveWithNewLocal(base, base.getType());
						out.add(ap);
						return out;
					} else if (rightOp instanceof StaticFieldRef && context.trackStaticFields()) {
						StaticFieldRef fr = (StaticFieldRef) rightOp;
						AccessGraph prependField = source
								.prependField(new WrappedSootField(fr.getField(), source.getBaseType(), curr));
						AccessGraph ap = prependField.makeStatic();
						return Collections.singleton(ap);
					}
				} else if (leftOp instanceof ArrayRef) {
					ArrayRef arrayRef = (ArrayRef) leftOp;
					Local base = (Local) arrayRef.getBase();
					if (source.baseAndFirstFieldMatches(base, AliasFinder.ARRAY_FIELD)) {
						if (isAllocationSite(rightOp)) {
							allocationSiteReached(edge, as, rightOp);
							return Collections.emptySet();
						}
						Set<AccessGraph> out = new HashSet<>();
						out.add(source);
						if (rightOp instanceof Local)
							out.add(new AccessGraph((Local) rightOp, arrayRef.getType()));
						return out;
					}
				} else if (leftOp instanceof InstanceFieldRef) {
					InstanceFieldRef fr = (InstanceFieldRef) leftOp;
					Value base = fr.getBase();
					SootField field = fr.getField();
					if (source.baseAndFirstFieldMatches(base, field)) {
						if (isAllocationSite(rightOp)) {
							allocationSiteReached(edge, as, rightOp);
							return Collections.emptySet();
						}

						if (rightOp instanceof Local) {
							Set<AccessGraph> out = new HashSet<>();
							for (WrappedSootField wrappedField : source.getFirstField()) {
								out.addAll(source.deriveWithNewLocal((Local) rightOp, wrappedField.getType()).popFirstField());
							}
							return out;
						}
					}
				} else if (leftOp instanceof StaticFieldRef && context.trackStaticFields()) {
					StaticFieldRef sfr = (StaticFieldRef) leftOp;
					if (source.isStatic() && source.firstFieldMustMatch(sfr.getField())) {
						if (rightOp instanceof Local) {
							Set<AccessGraph> newAp = source.popFirstField();
							Set<AccessGraph> out = new HashSet<>();
							for (AccessGraph a : newAp) {
								for (WrappedSootField wrappedField : source.getFirstField())
									out.add(a.deriveWithNewLocal((Local) rightOp, wrappedField.getType()));
							}
							return out;
						}
					}

				}

				return Collections.singleton(source);
			}

		};
	}

	@Override
	public FlowFunction<AccessGraph> getCallFlowFunction(final IPathEdge<Unit, AccessGraph> edge,
			final SootMethod callee, final Unit calleeSp) {
		final Unit callStmt = edge.getTarget();
		final Local[] paramLocals = new Local[callee.getParameterCount()];
		for (int i = 0; i < callee.getParameterCount(); i++)
			paramLocals[i] = callee.getActiveBody().getParameterLocal(i);

		context.getSubQuery().addAsVisitedBackwardMethod(callee);
		final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
		return new FlowFunction<AccessGraph>() {
			@Override
			public Set<AccessGraph> computeTargets(AccessGraph source) {
				if (calleeSp instanceof ThrowStmt) {
					return Collections.emptySet();
				}
				if (context.trackStaticFields() && source.isStatic()) {
					if (callee != null && isFirstFieldUsedTransitivelyInMethod(source, callee)) {
						return Collections.singleton(source);
					} else {
						return Collections.emptySet();
					}
				}
				if (AliasFinder.IGNORED_METHODS.contains(callee)) {
					return Collections.emptySet();
				}
				HashSet<AccessGraph> out = new HashSet<AccessGraph>();
				// mapping of fields of AccessPath those will be killed in
				// callToReturn
				if (callStmt instanceof Stmt) {
					Stmt is = (Stmt) callStmt;
					if (is.containsInvokeExpr()) {
						InvokeExpr ie = is.getInvokeExpr();

						final Value[] callArgs = new Value[ie.getArgCount()];
						for (int i = 0; i < ie.getArgCount(); i++)
							callArgs[i] = ie.getArg(i);

						for (int i = 0; i < paramLocals.length; i++) {
							if (ie.getArgs().get(i).equals(source.getBase()) && source.getFieldCount() > 0) {
								if (typeCompatible(paramLocals[i].getType(), source.getBaseType())) {
									out.add(source.deriveWithNewLocal(paramLocals[i], source.getBaseType()));
								}
							}
						}

						if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();
							if (source.baseMatches(iIExpr.getBase())) {
								if (callee != null && !ForwardFlowFunctions.hasCompatibleTypesForCall(source,
										callee.getDeclaringClass()))
									return Collections.emptySet();
								if (source.getFieldCount() == 0)
									return Collections.emptySet();
								if (!context.isIgnoredMethod(callee)) {
									AccessGraph replacedThisValue = source.deriveWithNewLocal(thisLocal,
											source.getBaseType());
									if (context.isValidAccessPath(replacedThisValue)) {
										out.add(replacedThisValue);
									}
								}
							}
						}
					}

				}

				if (callStmt instanceof AssignStmt && calleeSp instanceof ReturnStmt) {
					AssignStmt as = (AssignStmt) callStmt;
					Value leftOp = as.getLeftOp();
					// mapping of return value
					if (leftOp instanceof Local && source.baseMatches(leftOp)) {
						ReturnStmt retSite = (ReturnStmt) calleeSp;
						Value retOp = retSite.getOp();
						if (!context.isIgnoredMethod(callee)) {
							if (retOp instanceof Local) {
								if (typeCompatible(((Local) retOp).getType(), source.getBaseType())) {
									AccessGraph possibleAccessPath = source.deriveWithNewLocal((Local) retOp,
											source.getBaseType());
									out.add(possibleAccessPath);
								}
							}
							if (isAllocationSite(retOp)) {
								allocationSiteReached(edge, as, retOp);
								return Collections.emptySet();
							}
						}
					}

				}
				return out;
			}
		};
	}

	@Override
	public FlowFunction<AccessGraph> getReturnFlowFunction(final IPathEdge<Unit, AccessGraph> edge, final Unit callSite,
			final SootMethod callee, final Unit returnSite) {

		final Local[] paramLocals = new Local[callee.getParameterCount()];
		for (int i = 0; i < callee.getParameterCount(); i++)
			paramLocals[i] = callee.getActiveBody().getParameterLocal(i);
		final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
		return new FlowFunction<AccessGraph>() {

			@Override
			public Set<AccessGraph> computeTargets(AccessGraph source) {
				AccessGraph derivedSource = source;
				Set<AccessGraph> out = new HashSet<>();

				if (context.trackStaticFields() && source.isStatic())
					return Collections.singleton(source);

				if (callSite instanceof Stmt) {
					Stmt is = (Stmt) callSite;
					if (is.containsInvokeExpr()) {
						final InvokeExpr ie = is.getInvokeExpr();
						for (int i = 0; i < paramLocals.length; i++) {
							if (source.getBase().equivTo(paramLocals[i])) {
								Value arg = ie.getArg(i);
								if (arg instanceof Local) {
									if (typeCompatible(((Local) arg).getType(), source.getBaseType())) {
										AccessGraph ap = derivedSource.deriveWithNewLocal((Local) arg,
												source.getBaseType());
										out.add(ap);
									}
								}
							}
						}
						if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();
							if (source.baseMatches(thisLocal)) {
								Local newBase = (Local) iIExpr.getBase();
								if (typeCompatible(newBase.getType(), source.getBaseType())) {
									AccessGraph ap = derivedSource.deriveWithNewLocal(newBase, source.getBaseType());
									out.add(ap);
								}
							}
						}
					}
				}
				return out;
			}
		};

	}

	@Override
	public FlowFunction<AccessGraph> getCallToReturnFlowFunction(final IPathEdge<Unit, AccessGraph> edge,
			Unit returnSite, final SootMethod callee, final Unit calleeSp) {
		if (callee != null) {
			final Local[] paramLocals = new Local[callee.getParameterCount()];
			for (int i = 0; i < callee.getParameterCount(); i++)
				paramLocals[i] = callee.getActiveBody().getParameterLocal(i);
		}
		context.getSubQuery().addAsVisitedBackwardMethod(context.icfg.getMethodOf(edge.getTarget()));
		final Unit callSite = edge.getTarget();
		return new FlowFunction<AccessGraph>() {
			@Override
			public Set<AccessGraph> computeTargets(AccessGraph source) {
				boolean sourceIsKilled = false;

				if (context.trackStaticFields() && source.isStatic()) {
					if (callee == null) {
						return Collections.singleton(source);

					}
					boolean staticFieldUsed = isFirstFieldUsedTransitivelyInMethod(source, callee);
					if (!staticFieldUsed) {
						return Collections.singleton(source);
					} else {
						return Collections.emptySet();
					}
				}
				Set<AccessGraph> out = new HashSet<>();
				if (callSite instanceof Stmt) {
					Stmt is = (Stmt) callSite;
					if (is.containsInvokeExpr()) {
						final InvokeExpr ie = is.getInvokeExpr();

						final Value[] callArgs = new Value[ie.getArgCount()];
						for (int i = 0; i < ie.getArgCount(); i++)
							callArgs[i] = ie.getArg(i);
						if (context.backwardMockHandler.handles(callSite, ie, source, callArgs)) {
							return context.backwardMockHandler.computeTargetsOverCall(callSite, ie, source, callArgs,
									edge);
						}
						if (ie.getMethod().equals(AliasFinder.ARRAY_COPY)) {
							for (Value callVal : callArgs) {
								if (callVal.equals(source.getBase())) {
									// java uses call by value, but fields of
									// complex objects can be changed (and
									// tainted), so use this conservative
									// approach:
									Set<AccessGraph> nativeAbs = context.ncHandler.getBackwardValues(is, source,
											callArgs);
									out.addAll(nativeAbs);
								}
							}
						}
					}

				}

				if (callSite instanceof AssignStmt) {
					AssignStmt as = (AssignStmt) callSite;
					Value leftOp = as.getLeftOp();
					// mapping of return value
					if (leftOp instanceof Local && source.getBase().equals(leftOp)) {
						sourceIsKilled = true;
					}
				}
				if (!sourceIsKilled)
					out.add(source);

				return out;
			}

		};
	}

	private boolean isAllocationSite(Value val) {
		return (val instanceof StringConstant || val instanceof NewExpr || val instanceof NewArrayExpr
				|| val instanceof NewMultiArrayExpr || val instanceof NullConstant);
	}

	private void allocationSiteReached(IPathEdge<Unit, AccessGraph> pe, AssignStmt as, Value val) {
		Value leftOp = as.getLeftOp();
		AccessGraph factAtTarget = pe.factAtTarget();
		if (leftOp instanceof Local) {
			if (factAtTarget.getFieldCount() > 0) {
				if (factAtTarget.firstFieldMustMatch(AliasFinder.ARRAY_FIELD)) {
					if (factAtTarget.getFieldCount() > 1) {
						return;
					}
				} else {
					return;
				}
			} else {
				if (!typeCompatible(factAtTarget.getBaseType(), val.getType())) {
					return;
				}
				if (!queryTypeMatch(val.getType()))
					return;
			}

		}

		if (context.getSubQuery() != null) {
			context.debugger.onAllocationSiteReached(as, pe);
			context.getSubQuery().add(new Alloc(pe));
		}
	}

	private boolean queryTypeMatch(Type allocationSiteType) {
		for (Type t : context.getSubQuery().getType())
			if (Scene.v().getOrMakeFastHierarchy().canStoreType(allocationSiteType, t))
				return true;
		return false;
	}
}
