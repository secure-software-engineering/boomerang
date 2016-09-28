package boomerang.pathconstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import boomerang.BoomerangTimeoutException;
import boomerang.backward.IBackwardPathEdge;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

abstract class AbstractPathConstructor<N, D, M, I extends BiDiInterproceduralCFG<N, M>> {

  protected IBackwardPathEdge<N, D, M, I> jumpFn;
  private List<Runnable> workList;

  private Pair<N, D> startPair;

  private LoadingCache<IPathEdge<N, D>, Set<N>> pathEdgesToPath = CacheBuilder.newBuilder()
      .maximumSize(100000).build(new CacheLoader<IPathEdge<N, D>, Set<N>>() {
        @Override
        public Set<N> load(final IPathEdge<N, D> pe) throws Exception {
          final Set<IPathEdge<N, D>> allReachablePathEdges = new HashSet<>();
          startPair = new Pair<N, D>(pe.getStart(), pe.factAtSource());;
          allReachablePathEdges.add(pe);
          workList = new LinkedList<>();
          workList.add(new Runnable() {
            @Override
            public void run() {
              doLookup(pe, allReachablePathEdges);
            }

          });
          while (!workList.isEmpty()) {
            Runnable runnable = workList.get(0);
            workList.remove(0);
            runnable.run();
          }

          return flattenPathEdges(allReachablePathEdges);
        }

      });


  AbstractPathConstructor(IBackwardPathEdge<N, D, M, I> jumpFn) {
    this.jumpFn = jumpFn;
  }

  private void doLookup(IPathEdge<N, D> edge, final Set<IPathEdge<N, D>> allReachablePathEdges) {
    Collection<IPathEdge<N, D>> reverseLookup = lookUp(startPair, edge);
    for (final IPathEdge<N, D> prevEdge : reverseLookup) {
      if (!prevEdge.getStartNode().equals(startPair)) {
        continue;
      }
      if (!allReachablePathEdges.contains(prevEdge)) {
        allReachablePathEdges.add(prevEdge);
        workList.add(new Runnable() {
          @Override
          public void run() {
            doLookup(prevEdge, allReachablePathEdges);
          }

        });
      }

    }
  }

  protected abstract Collection<IPathEdge<N, D>> lookUp(Pair<N, D> sPAndD1, IPathEdge<N, D> edge);



  public Set<N> computeSetOfVisitableStatements(IPathEdge<N, D> pe) {
    try {
      return pathEdgesToPath.get(pe);
    } catch (UncheckedExecutionException | BoomerangTimeoutException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
    }
    return Collections.emptySet();
  }

  private Set<N> flattenPathEdges(Set<IPathEdge<N, D>> allReachablePathEdges) {
    HashSet<N> outSet = new HashSet<N>();
    Pair<N, D> first = null;
    for (IPathEdge<N, D> p : allReachablePathEdges) {
      if (first == null)
        first = p.getStartNode();

      outSet.add(p.getTarget());
    }
    return outSet;
  }
}
