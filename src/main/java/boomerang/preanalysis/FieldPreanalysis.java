package boomerang.preanalysis;

import heros.InterproceduralCFG;
import heros.solver.IDESolver;
import heros.solver.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NewExpr;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.EdgePredicate;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;

public class FieldPreanalysis {
  private Multimap<SootMethod, SootField> methodsToFieldsReads = HashMultimap.create();
  private Multimap<SootMethod, SootField> methodsToFieldsWrites = HashMultimap.create();
  private Multimap<SootMethod, SootField> methodsToFieldsWritesAndReads = HashMultimap.create();
  private Multimap<SootMethod, Type> methodsToAllocs = HashMultimap.create();
  private Map<SootMethod, ReachableMethods> reachables = new HashMap<>();

  // retains only callers that are explicit call sites or Thread.start()
  private static class EdgeFilter extends Filter {
    protected EdgeFilter() {
      super(new EdgePredicate() {
        @Override
        public boolean want(Edge e) {
          return e.kind().isExplicit() || e.kind().isThread() || e.kind().isExecutor()
              || e.kind().isAsyncTask() || e.kind().isPrivileged();
        }
      });
    }
  }

  public FieldPreanalysis(InterproceduralCFG<Unit, SootMethod> icfg) {
    compute();
  }

  private void compute() {
    QueueReader<MethodOrMethodContext> reachableMethods =
        Scene.v().getReachableMethods().listener();
    while (reachableMethods.hasNext()) {
      MethodOrMethodContext next = reachableMethods.next();
      if (next.method() != null) {
        compute(next.method());
      }
    }

  }

  private void compute(SootMethod method) {
    if (!method.hasActiveBody())
      return;
    if (isIgnoredMethod(method)) {
      AliasFinder.IGNORED_METHODS.add(method);
      return;
    }
    for (Unit stmt : method.getActiveBody().getUnits()) {
      if (!(stmt instanceof AssignStmt))
        continue;
      AssignStmt assignStmt = (AssignStmt) stmt;
      Value leftOp = assignStmt.getLeftOp();
      if (leftOp instanceof InstanceFieldRef) {
        SootField field = ((InstanceFieldRef) leftOp).getField();
        methodsToFieldsWrites.put(method, field);
        methodsToFieldsWritesAndReads.put(method, field);
      }
      Value rightOp = assignStmt.getRightOp();
      if (rightOp instanceof InstanceFieldRef) {
        SootField field = ((InstanceFieldRef) rightOp).getField();
        methodsToFieldsReads.put(method, field);
        methodsToFieldsWritesAndReads.put(method, field);
      }
      if (rightOp instanceof NewExpr)
        methodsToAllocs.put(method, ((NewExpr) rightOp).getType());
    }
  }

  private boolean isIgnoredMethod(SootMethod method) {
    return method.toString().contains("int hashCode()")
        || method.toString().contains("java.lang.Object equals(java.lang.Object)")
        || method.toString().contains("toString()");
  }

  private boolean readsToFieldInternal(final SootMethod method, final SootField field) {
    return interactWithField(method, field, new GrepFieldRead());
  }

  private final LoadingCache<Pair<SootMethod, SootField>, Boolean> writeCache =
      IDESolver.DEFAULT_CACHE_BUILDER
          .build(new CacheLoader<Pair<SootMethod, SootField>, Boolean>() {


            @Override
            public Boolean load(Pair<SootMethod, SootField> arg0) throws Exception {
              // TODO Auto-generated method stub
              return writesToFieldInternal(arg0.getO1(), arg0.getO2());
            }
          });
  private final LoadingCache<Pair<SootMethod, SootField>, Boolean> readCache =
      IDESolver.DEFAULT_CACHE_BUILDER
          .build(new CacheLoader<Pair<SootMethod, SootField>, Boolean>() {


            @Override
            public Boolean load(Pair<SootMethod, SootField> arg0) throws Exception {
              return readsToFieldInternal(arg0.getO1(), arg0.getO2());
            }
          });
  private final LoadingCache<Pair<SootMethod, Type>, Boolean> allocCache =
      IDESolver.DEFAULT_CACHE_BUILDER.build(new CacheLoader<Pair<SootMethod, Type>, Boolean>() {


        @Override
        public Boolean load(Pair<SootMethod, Type> arg0) throws Exception {
          return containsAllocSiteOfTypeInternal(arg0.getO1(), arg0.getO2());
        }
      });
  private final LoadingCache<Pair<SootMethod, SootField>, Boolean> readOrWriteCache =
      IDESolver.DEFAULT_CACHE_BUILDER
          .build(new CacheLoader<Pair<SootMethod, SootField>, Boolean>() {


            @Override
            public Boolean load(Pair<SootMethod, SootField> arg0) throws Exception {
              return readOrWriteToFieldInternal(arg0.getO1(), arg0.getO2());
            }
          });

  public boolean writesToField(final SootMethod method, final SootField field) {
    return writeCache.getUnchecked(new Pair<SootMethod, SootField>(method, field));
  }

  public boolean readsFromField(final SootMethod method, final SootField field) {
    return readCache.getUnchecked(new Pair<SootMethod, SootField>(method, field));
  }

  private boolean writesToFieldInternal(final SootMethod method, final SootField field) {
    return interactWithField(method, field, new GrepFieldWrite());
  }

  private boolean readOrWriteToFieldInternal(final SootMethod method, final SootField field) {
    return interactWithField(method, field, new GrepFieldWriteOrRead());
  }


  public boolean containsAllocSiteOfType(final SootMethod method, final Type type) {
    return allocCache.getUnchecked(new Pair<SootMethod, Type>(method, type));
  }

  private boolean containsAllocSiteOfTypeInternal(final SootMethod method, final Type type) {
    return interactWithField(method, type, new GrepAllocSite());
  }

  public boolean accessesField(final SootMethod method, final SootField field) {
    return readOrWriteCache.getUnchecked(new Pair<SootMethod, SootField>(method, field));
  }

  private <T> boolean interactWithField(final SootMethod method, final T field, final Grep<T> grep) {
    QueueReader<MethodOrMethodContext> reachable = getReachable(method).listener();
    while (reachable.hasNext()) {
      MethodOrMethodContext next = reachable.next();
      if (next.method() == null)
        continue;
      if (isIgnoredMethod(next.method()))
        continue;

      if (grep.grep(next.method(), field))
        return true;
    }
    return false;
  }

  private ReachableMethods getReachable(SootMethod m) {
    ReachableMethods mtds = reachables.get(m);

    if (mtds == null) {
      ArrayList<MethodOrMethodContext> entryPoints = new ArrayList<MethodOrMethodContext>();
      entryPoints.add(m);
      mtds =
          new ReachableMethods(Scene.v().getCallGraph(), entryPoints.iterator(), new EdgeFilter());
      mtds.update();
      reachables.put(m, mtds);
    }

    return mtds;
  }

  private class GrepAllocSite implements Grep<Type> {

    @Override
    public boolean grep(SootMethod method, Type t) {
      Collection<Type> collection = methodsToAllocs.get(method);
      for (Type child : collection) {
        if (Scene.v().getFastHierarchy().canStoreType(child, t)) {
          return true;
        }
      }
      return false;
    }
  }
  private interface Grep<T> {
    boolean grep(SootMethod method, T t);
  }

  private class GrepFieldRead implements Grep<SootField> {

    @Override
    public boolean grep(SootMethod method, SootField t) {
      return methodsToFieldsReads.get(method).contains(t);
    }

  }
  private class GrepFieldWriteOrRead implements Grep<SootField> {

    @Override
    public boolean grep(SootMethod method, SootField t) {
      return methodsToFieldsWritesAndReads.get(method).contains(t);
    }

  }
  private class GrepFieldWrite implements Grep<SootField> {

    @Override
    public boolean grep(SootMethod method, SootField t) {
      return methodsToFieldsWrites.get(method).contains(t);
    }

  }



}
