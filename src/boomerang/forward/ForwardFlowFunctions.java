package boomerang.forward;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.ifdssolver.FlowFunctions;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Write;
import heros.FlowFunction;
import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

public class ForwardFlowFunctions extends AbstractFlowFunctions implements
    FlowFunctions<Unit, AccessGraph, SootMethod> {

  public ForwardFlowFunctions(BoomerangContext c) {
    this.context = c;
  }



  @Override
  public FlowFunction<AccessGraph> getNormalFlowFunction(final IPathEdge<Unit, AccessGraph> edge,
      final Unit succ) {
    final Unit curr = edge.getTarget();

    final SootMethod method = context.icfg.getMethodOf(curr);
    final Local thisLocal = method.isStatic() ? null : method.getActiveBody().getThisLocal();
    return new FlowFunction<AccessGraph>() {

      @Override
      public Set<AccessGraph> computeTargets(AccessGraph source) {
        assert thisLocal == null || !source.baseMatches(thisLocal)
            || ForwardFlowFunctions.hasCompatibleTypesForCall(source, method.getDeclaringClass()) : edge
            .toString();
        assert source.isStatic() || method.getActiveBody().getLocals().contains(source.getBase());

        if (!(curr instanceof AssignStmt)) {
          return Collections.singleton(source);
        }


        AssignStmt as = (AssignStmt) curr;
        Value leftOp = as.getLeftOp();
        Value rightOp = as.getRightOp();

        HashSet<AccessGraph> out = new HashSet<AccessGraph>();
        out.add(source);

        if (rightOp instanceof Constant || rightOp instanceof NewExpr) {
          // a = new || a = 2
          if (leftOp instanceof Local && source.baseMatches(leftOp))
            // source == a.*
            return Collections.emptySet();
          // a.f = new || a.f = 2;
          if (leftOp instanceof InstanceFieldRef) {
            InstanceFieldRef fr = (InstanceFieldRef) leftOp;
            Value base = fr.getBase();
            SootField field = fr.getField();
            // source == a.f.*
            if (source.baseAndFirstFieldMatches(base, field))
              return Collections.emptySet();
          }

        }

        if (leftOp instanceof Local) {
          if (source.baseMatches(leftOp)) {
            if (rightOp instanceof InstanceFieldRef) {
              InstanceFieldRef fr = (InstanceFieldRef) rightOp;
              Value base = fr.getBase();
              SootField field = fr.getField();

              if (source.baseAndFirstFieldMatches(base, field)) {
                Set<AccessGraph> popFirstField = source.popFirstField();
                out.addAll(popFirstField);
              } else {
                return Collections.emptySet();
              }
            } else {
              return Collections.emptySet();
        	  }
          }
        } else if (leftOp instanceof InstanceFieldRef) {
          InstanceFieldRef fr = (InstanceFieldRef) leftOp;
          Value base = fr.getBase();
          SootField field = fr.getField();
          if (source.baseAndFirstFieldMatches(base, field)) {
            return Collections.emptySet();
          }
        }
        if (rightOp instanceof CastExpr) {
          CastExpr castExpr = (CastExpr) rightOp;
          Value op = castExpr.getOp();
          if (op instanceof Local) {
            if (!source.isStatic() && source.baseMatches(op)
                && typeCompatible(castExpr.getCastType(), source.getBaseType())) {
              Type newType =
                  (Scene.v().getFastHierarchy()
                      .canStoreType(castExpr.getCastType(), source.getBaseType()) ? castExpr
                      .getCastType() : source.getBaseType());
              out.add(source.deriveWithNewLocal((Local) leftOp, newType));
            }
          }
        }
        if (rightOp instanceof Local && source.baseMatches(rightOp)) {

          if (leftOp instanceof Local) {
            // e = d;
            if (typeCompatible(((Local) leftOp).getType(), source.getBaseType())) {
              out.add(source.deriveWithNewLocal((Local) leftOp, source.getBaseType()));
            }
          } else if (leftOp instanceof InstanceFieldRef) {
            // d.f = e;
            InstanceFieldRef fr = (InstanceFieldRef) leftOp;
            Value base = fr.getBase();
            SootField field = fr.getField();

            if (base instanceof Local) {
              Local lBase = (Local) base;

              AccessGraph withNewLocal = source.deriveWithNewLocal(lBase, lBase.getType());
              WrappedSootField newFirstField = new WrappedSootField(field, source.getBaseType(), curr);
              if (withNewLocal.canPrepend(newFirstField)) {
                AccessGraph newAp = withNewLocal.prependField(newFirstField);
                out.add(newAp);
                computeAliasesOnInstanceWrite(curr, source, lBase, field, (Local) rightOp, out,
                    edge);
              }
            }
          } else if (leftOp instanceof ArrayRef) {
            ArrayRef fr = (ArrayRef) leftOp;
            Value base = fr.getBase();

            if (base instanceof Local) {
              Local lBase = (Local) base;

              AccessGraph withNewLocal = source.deriveWithNewLocal(lBase, lBase.getType());
              AccessGraph newAp =
                  withNewLocal.prependField(new WrappedSootField(AliasFinder.ARRAY_FIELD, source
                      .getBaseType(), curr));
              out.add(newAp);
              computeAliasesOnInstanceWrite(curr, source, lBase, AliasFinder.ARRAY_FIELD,
                  (Local) rightOp, out, edge);

            }
          } else if (leftOp instanceof StaticFieldRef && context.trackStaticFields()) {
            // d.f = e;
            StaticFieldRef fr = (StaticFieldRef) leftOp;
            SootField field = fr.getField();

            AccessGraph staticap = source.makeStatic();
            AccessGraph newAp =
                staticap.prependField(new WrappedSootField(field, source.getBaseType(), curr));
            out.add(newAp);
            return out;
          }
        } else if (rightOp instanceof InstanceFieldRef) {
          InstanceFieldRef fr = (InstanceFieldRef) rightOp;
          Value base = fr.getBase();
          SootField field = fr.getField();


          if (source.baseAndFirstFieldMatches(base, field)) {
            // e = a.f && source == a.f.*
            // replace in source
            if (leftOp instanceof Local && !source.baseMatches(leftOp)) {

            	  for(WrappedSootField wrappedField : source.getFirstField()){
		              AccessGraph deriveWithNewLocal =
		                  source.deriveWithNewLocal((Local) leftOp, wrappedField.getType());
		              
		              out.addAll(deriveWithNewLocal.popFirstField());
            	  }
            }
          }
        } else if (rightOp instanceof ArrayRef) {
          ArrayRef arrayRef = (ArrayRef) rightOp;
          if (source.baseAndFirstFieldMatches(arrayRef.getBase(), AliasFinder.ARRAY_FIELD)) {

            Set<AccessGraph> withoutFirstField = source.popFirstField();
            for (AccessGraph a : withoutFirstField) {
          	  for(WrappedSootField wrappedField : source.getFirstField())
          		  out.add(a.deriveWithNewLocal((Local) leftOp, wrappedField.getType()));
            }
          }
        } else if (rightOp instanceof StaticFieldRef && context.trackStaticFields()) {
          StaticFieldRef sfr = (StaticFieldRef) rightOp;
          if (source.isStatic() && source.firstFieldMustMatch(sfr.getField())) {
            if (leftOp instanceof Local) {
              Set<AccessGraph> withoutFirstField = source.popFirstField();
              for (AccessGraph a : withoutFirstField) {
            	  for(WrappedSootField wrappedField : source.getFirstField())
            		  out.add(a.deriveWithNewLocal((Local) leftOp, wrappedField.getType()));
              }
            }
          }
        }


        return out;
      }
    };
  }



  private void computeAliasesOnInstanceWrite(Unit curr, AccessGraph source, Local lBase,
      SootField field, Local rightLocal, HashSet<AccessGraph> out,
      IPathEdge<Unit, AccessGraph> edge) {
    Write write = new Write(curr, lBase, field, rightLocal, source, edge);
    if (context.addToDirectlyProcessed(write)) {
      Set<AccessGraph> aliases = write.process(context);
      out.addAll(aliases);
    }
  }

  @Override
  public FlowFunction<AccessGraph> getCallFlowFunction(final IPathEdge<Unit, AccessGraph> edge,
      final SootMethod callee, Unit calleeSp) {
    assert callee != null;
    final Unit callSite = edge.getTarget();
    final Local[] paramLocals = new Local[callee.getParameterCount()];
    for (int i = 0; i < callee.getParameterCount(); i++)
      paramLocals[i] = callee.getActiveBody().getParameterLocal(i);

    final AccessGraph d1 = edge.factAtSource();
    final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
    return new FlowFunction<AccessGraph>() {
      @Override
      public Set<AccessGraph> computeTargets(AccessGraph source) {
        assert source != null;
        Set<AccessGraph> out = new HashSet<>();
        Stmt is = (Stmt) callSite;
        source = source.deriveWithoutAllocationSite();
        if (context.trackStaticFields() && source.isStatic()) {
          if (callee != null
              && isFirstFieldUsedTransitivelyInMethod(source, callee)) {
            return Collections.singleton(source);
          } else {
            return Collections.emptySet();
          }
        }
        if (callee != null && !context.icfg.methodReadsValue(callee, source.getBase())) {
          // return Collections.emptySet();
        }
        if (edge.factAtSource() != null) {
          if (AliasFinder.IGNORED_METHODS.contains(callee)) {
            return Collections.emptySet();
          }
        }

        if (is.containsInvokeExpr()) {
          final InvokeExpr ie = is.getInvokeExpr();
          for (int i = 0; i < paramLocals.length; i++) {
            Value arg = ie.getArg(i);
            if (arg instanceof Local && source.baseMatches(arg)) {
              if (typeCompatible(paramLocals[i].getType(), source.getBaseType())) {
                out.add(source.deriveWithNewLocal(paramLocals[i], source.getBaseType()));
              }
            }
          }
          final Value[] callArgs = new Value[ie.getArgCount()];
          for (int i = 0; i < ie.getArgCount(); i++)
            callArgs[i] = ie.getArg(i);
          if (!context.forwardMockHandler.flowInto(callSite, source, ie, callArgs))
            return Collections.emptySet();

          if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();

            if (source.baseMatches(iIExpr.getBase())) {
              if (callee != null && !hasCompatibleTypesForCall(source, callee.getDeclaringClass()))
                return Collections.emptySet();
              if (context.isIgnoredMethod(callee)) {
                return Collections.emptySet();
              }
              if (d1 != null && d1.hasAllocationSite() && source.getFieldCount() < 1) {
                Unit sourceStmt = d1.getSourceStmt();
                if (sourceStmt instanceof AssignStmt) {
                  AssignStmt as = (AssignStmt) sourceStmt;
                  Value rightOp = as.getRightOp();
                  Type type = rightOp.getType();
                  if (type instanceof RefType) {
                    RefType refType = (RefType) type;
                    SootClass typeClass = refType.getSootClass();
                    SootClass methodClass = callee.getDeclaringClass();
                    if (typeClass != null && methodClass != null && typeClass != methodClass
                        && !typeClass.isInterface()) {
                      if (!Scene.v().getFastHierarchy().isSubclass(typeClass, methodClass)) {
                        return Collections.emptySet();
                      }
                    }
                  } else if (type instanceof PrimType) {
                    return Collections.emptySet();
                  }

                }
              }

              AccessGraph replacedThisValue =
                  source.deriveWithNewLocal(thisLocal, source.getBaseType());
              if (context.isValidAccessPath(replacedThisValue)) {
                out.add(replacedThisValue);
              }
            }
          }
        }
        return out;
      }
    };
  }

  @Override
  public FlowFunction<AccessGraph> getReturnFlowFunction(IPathEdge<Unit, AccessGraph> edge,
      final Unit callStmt, final SootMethod callee, final Unit returnSite) {
    final Local[] paramLocals = new Local[callee.getParameterCount()];
    for (int i = 0; i < callee.getParameterCount(); i++)
      paramLocals[i] = callee.getActiveBody().getParameterLocal(i);
    final Unit exitStmt = edge.getTarget();
    final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
    return new FlowFunction<AccessGraph>() {
      @Override
      public Set<AccessGraph> computeTargets(AccessGraph source) {
        HashSet<AccessGraph> out = new HashSet<AccessGraph>();

        // mapping of fields of AccessPath those will be killed in
        // callToReturn
        if (context.trackStaticFields() && source.isStatic())
          return Collections.singleton(source);

        if (callStmt instanceof Stmt) {
          Stmt is = (Stmt) callStmt;

          if (is.containsInvokeExpr()) {
            InvokeExpr ie = is.getInvokeExpr();
            for (int i = 0; i < paramLocals.length; i++) {

              if (paramLocals[i] == source.getBase()) {
                Value arg = ie.getArg(i);
                if (arg instanceof Local) {
                  if (typeCompatible(((Local) arg).getType(), source.getBaseType())) {
                    AccessGraph deriveWithNewLocal =
                        source.deriveWithNewLocal((Local) arg, source.getBaseType());
                    out.add(deriveWithNewLocal);
                  }
                }

              }
            }
            if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
              if (source.baseMatches(thisLocal)) {

                InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();
                Local newBase = (Local) iIExpr.getBase();
                if (typeCompatible(newBase.getType(), source.getBaseType())) {
                  AccessGraph possibleAccessPath =
                      source.deriveWithNewLocal((Local) iIExpr.getBase(), source.getBaseType());
                  out.add(possibleAccessPath);
                }
              }
            }
          }
        }

        if (callStmt instanceof AssignStmt && exitStmt instanceof ReturnStmt) {
          AssignStmt as = (AssignStmt) callStmt;
          Value leftOp = as.getLeftOp();
          // mapping of return value


          ReturnStmt returnStmt = (ReturnStmt) exitStmt;
          Value returns = returnStmt.getOp();
          // d = return out;
          if (leftOp instanceof Local) {
            if (returns instanceof Local && source.getBase() == returns) {
              out.add(source.deriveWithNewLocal((Local) leftOp, source.getBaseType()));
            }
          }
        }
        return out;
      }

    };
  }

  @Override
  public FlowFunction<AccessGraph> getCallToReturnFlowFunction(
      final IPathEdge<Unit, AccessGraph> edge, Unit returnSite, final Collection<SootMethod> callees) {
    final Unit callSite = edge.getTarget();
    return new FlowFunction<AccessGraph>() {
      @Override
      public Set<AccessGraph> computeTargets(AccessGraph source) {

        if (context.trackStaticFields() && source.isStatic()) {
          if (!isFirstFieldUsedTransitivelyInMethod(source, callees)) {
            return Collections.singleton(source);
          } else {
            return Collections.emptySet();
          }
        }
        Set<AccessGraph> out = new HashSet<>();
        boolean sourceIsKilled = false;
        if (callSite instanceof AssignStmt) {
          AssignStmt as = (AssignStmt) callSite;
          Value leftOp = as.getLeftOp();
          if (source.getBase() == leftOp) {
            return Collections.emptySet();
          }
        }

        Stmt is = (Stmt) callSite;
        if (is.containsInvokeExpr()) {
          final InvokeExpr ie = is.getInvokeExpr();
          final Value[] callArgs = new Value[ie.getArgCount()];
          for (int i = 0; i < ie.getArgCount(); i++)
            callArgs[i] = ie.getArg(i);

          if (context.forwardMockHandler.handles(callSite, ie, source, callArgs)) {
            return context.forwardMockHandler.computeTargetsOverCall(callSite, ie, source,
                callArgs, edge);
          }

          if (ie.getMethod().equals(AliasFinder.ARRAY_COPY)) {
            for (Value callVal : callArgs) {
              if (callVal == source.getBase()) {
                // java uses call by value, but fields of complex objects can be changed (and
                // tainted), so use this conservative approach:
                Set<AccessGraph> nativeAbs =
                    context.ncHandler.getForwardValues(is, source, callArgs);
                out.addAll(nativeAbs);
              }
            }
          }


          for (int i = 0; i < ie.getArgCount(); i++) {
            Value arg = ie.getArg(i);
            if (source.getBase() == arg) {
              if (!callees.isEmpty()) {
                sourceIsKilled = true;
              }
            }
          }
          if (ie instanceof InstanceInvokeExpr) {
            InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
            if (iie.getBase().equals(source.getBase())) {
              if (!callees.isEmpty()) {
                sourceIsKilled = true;
              }
            }
          }
        }
        if (!sourceIsKilled)
          out.add(source);

        return out;
      }
    };
  }



}
