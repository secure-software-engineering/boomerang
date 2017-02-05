package boomerang;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.lang.model.type.PrimitiveType;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.bidi.PathEdgeStore;
import boomerang.bidi.Summaries;
import boomerang.cache.Query;
import boomerang.cache.ResultCache;
import boomerang.debug.IBoomerangDebugger;
import boomerang.forward.ForwardFlowFunctions;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IFDSSolver;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.ISummaries;
import boomerang.mock.DefaultBackwardDataFlowMocker;
import boomerang.mock.DefaultForwardDataFlowMocker;
import boomerang.mock.DefaultNativeCallHandler;
import boomerang.mock.MockedDataFlow;
import boomerang.mock.NativeCallHandler;
import heros.solver.Pair;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ReturnStmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

@SuppressWarnings("serial")
public class BoomerangContext extends LinkedList<SubQueryContext> {

  /**
   * Holds the summaries for all backward IFDS problems.
   */
  public ISummaries<Unit, SootMethod, AccessGraph> BW_SUMMARIES;

  /**
   * Holds the summaries for all forward IFDS problems.
   */
  public ISummaries<Unit, SootMethod, AccessGraph> FW_SUMMARIES;
  /**
   * The inter-procedural control flow graph to be used.
   */
  public IInfoflowCFG icfg;

  public IBoomerangDebugger debugger;

  private Set<Query> recursiveQueries = new HashSet<>();


  /**
   * The inter-procedural backward control flow graph to be used.
   */
  public IInfoflowCFG bwicfg;

  /**
   * Global query cache, holding different caches.
   */
  public ResultCache querycache;


  /**
   * Native call handler, defines how aliases flow at native call sites.
   */
  public NativeCallHandler ncHandler = new DefaultNativeCallHandler();

  /**
   * Can be used to specify forward flow function for certain functions.
   */
  public MockedDataFlow forwardMockHandler = new DefaultForwardDataFlowMocker(this);

  /**
   * Can be used to specify backward flow function for certain functions.
   */
  public MockedDataFlow backwardMockHandler = new DefaultBackwardDataFlowMocker(this);

  Stopwatch startTime;
  private long budgetInMilliSeconds = 10000;
  private boolean trackStaticFields;
  @SuppressWarnings("rawtypes")
  private Set<IFDSSolver> solvers = new HashSet<>();
  private Multimap<Pair<Unit, AccessGraph>, Unit> meetingPointToPath = HashMultimap.create();

  public BoomerangContext(IInfoflowCFG icfg, IInfoflowCFG bwicfg) {
    this(icfg, bwicfg, new BoomerangOptions());
  }

  public BoomerangContext(IInfoflowCFG icfg, IInfoflowCFG bwicfg, BoomerangOptions options) {
    this.icfg = icfg;
    this.bwicfg = bwicfg;
    this.debugger = options.getDebugger();
    this.debugger.setContext(this);
    this.budgetInMilliSeconds = options.getTimeBudget();
    WrappedSootField.TRACK_TYPE = options.getTrackType();
    WrappedSootField.TRACK_STMT = options.getTrackStatementsInFields();
    this.trackStaticFields = options.getTrackStaticFields();

    FW_SUMMARIES = new Summaries(this);
    BW_SUMMARIES = new Summaries(this);
    querycache = new ResultCache();
  }


  public SubQueryContext getSubQuery() {
    return peek();
  }

  public String toString() {
    SubQueryContext subQuery = getSubQuery();
    return "[Pos: " + size() + " " + (subQuery != null
        ? getSubQuery() + " € " + icfg.getMethodOf(getSubQuery().getStmt()) : "") + "]";
  }



  public boolean isValidAccessPath(AccessGraph a) {
    return true;
  }

  public boolean isParameterOrThisValue(Unit stmtInMethod, Local local) {
    SootMethod method = bwicfg.getMethodOf(stmtInMethod);
    return isParameterOrThisValue(method, local);
  }

  public static boolean isParameterOrThisValue(SootMethod method, Local local) {
    boolean isParameter = method.getActiveBody().getParameterLocals().contains(local);
    if (isParameter)
      return true;
    return isThisValue(method, local);
  }

  public static boolean isThisValue(SootMethod method, Local local) {
    if (!method.isStatic()) {
      return method.getActiveBody().getThisLocal().equals(local);
    }
    return false;
  }

  public void resetQueryCache() {
    querycache.clear();
  }

  public void cleanQueryCache() {
    if (querycache != null)
      querycache.clean();
  }


  public void resetSummaries() {
    FW_SUMMARIES.clear();
    BW_SUMMARIES.clear();
    FW_SUMMARIES = new Summaries(this);
    BW_SUMMARIES = new Summaries(this);
  }

  public void forceTerminate() {
    while (!isEmpty()) {
      SubQueryContext peek = this.pollFirst();
      if (peek != null) {
        peek.cleanup();
      }
    }
    if (solvers != null) {
      for (@SuppressWarnings("rawtypes")
      IFDSSolver s : new HashSet<>(solvers)) {
        if (s != null)
          s.cleanup();
      }
      solvers.clear();
    }
    cleanQueryCache();
    meetingPointToPath.clear();
    meetingPointToPath = HashMultimap.create();
  }

  public boolean isIgnoredMethod(SootMethod m) {
    return false;
  }

  public void sanityCheckEdge(IPathEdge<Unit, AccessGraph> edge) {
    if (edge.getStart() == null)
      return;
    SootMethod m1 = icfg.getMethodOf(edge.getStart());
    SootMethod m2 = icfg.getMethodOf(edge.getTarget());
    assert m1 == m2 : "The path edge " + edge + "contains statements of two different method: "
        + m1.toString() + " and " + m2.toString();;
    assert !isIgnoredMethod(m1) : "The path edge resides in a method which should be ignored "
        + m1.toString();
  }

  @SuppressWarnings("rawtypes")
  public void addSolver(IFDSSolver backwardsolver) {
    this.solvers.add(backwardsolver);
  }

  @SuppressWarnings("rawtypes")
  public void removeSolver(IFDSSolver solver) {
    solver.cleanup();
    this.solvers.remove(solver);
  }


  public boolean isAlreadyStartedMeetingPoint(Pair<Unit, AccessGraph> startedPair, Unit stmt) {
    Collection<Unit> collection = meetingPointToPath.get(startedPair);
    return collection.contains(stmt);
  }

  public void addMeetingPoint(Pair<Unit, AccessGraph> startedPair, Unit stmt) {
    Set<Unit> paths = constructAllPathFrom(stmt, startedPair.getO1());
    meetingPointToPath.putAll(startedPair, paths);
  }

  private Set<Unit> constructAllPathFrom(Unit source, Unit dest) {
    Set<Unit> visited = new HashSet<>();
    LinkedList<Unit> worklist = new LinkedList<>();
    worklist.add(source);
    visited.add(source);
    while (!worklist.isEmpty()) {
      Unit next = worklist.removeFirst();
      for (Unit pred : icfg.getPredsOf(next)) {
        if (!visited.contains(pred)) {
          visited.add(pred);
          if (!dest.equals(pred))
            worklist.add(pred);
        }
      }
    }
    return visited;
  }

  public boolean isReturnValue(SootMethod method, Local base) {
    Collection<Unit> endPointsOf = icfg.getEndPointsOf(method);

    for (Unit eP : endPointsOf) {
      if (eP instanceof ReturnStmt) {
        ReturnStmt returnStmt = (ReturnStmt) eP;
        Value op = returnStmt.getOp();
        if (op.equals(base))
          return true;
      }
    }
    return false;
  }

  public boolean isValidQuery(AccessGraph ap, Unit stmt) {
    SootMethod m = bwicfg.getMethodOf(stmt);
    if (!ap.isStatic() && !m.getActiveBody().getLocals().contains(ap.getBase())) {
      return false;
    }

    if (ap.getBase() instanceof PrimitiveType) {
      return false;
    }
    if (!ap.isStatic() && !m.isStatic()) {
      Local thisLocal = m.getActiveBody().getThisLocal();
      if (ap.baseMatches(thisLocal)) {
        if (!ForwardFlowFunctions.hasCompatibleTypesForCall(ap, m.getDeclaringClass())) {
          return false;
        }
      }
    }
    return true;
  };

  public boolean isOutOfBudget() {
    if (startTime.elapsed(TimeUnit.MILLISECONDS) > budgetInMilliSeconds)
      return true;
    return false;
  }

  public void validateInput(AccessGraph ap, Unit stmt) {
    SootMethod m = bwicfg.getMethodOf(stmt);
    if (!ap.isStatic() && !m.getActiveBody().getLocals().contains(ap.getBase())) {
      throw new IllegalArgumentException("Base value of access path " + ap
          + " is not a local of the Method at which the Query was asked!");
    }
    if (stmt == null)
      throw new IllegalArgumentException("Statment must not be null");

    if (ap.getBase() instanceof PrimitiveType) {
      throw new IllegalArgumentException("The queried variable is not of pointer type");
    }
    if (!ap.isStatic() && !m.isStatic()) {
      Local thisLocal = m.getActiveBody().getThisLocal();
      if (ap.baseMatches(thisLocal)) {
        if (!ForwardFlowFunctions.hasCompatibleTypesForCall(ap, m.getDeclaringClass())) {
          throw new IllegalArgumentException("The type is incompatible");
        }
      }
    }
  }

  public PathEdgeStore getBackwardsPathEdges() {
    return getSubQuery().getBwEdges();
  }

  public PathEdgeStore getForwardPathEdges() {
    return getSubQuery().getFwEdges();
  }

  public void setRecursive(Query q) {
    recursiveQueries.add(q);
  }

  public boolean isRecursive(Query q) {
    return recursiveQueries.contains(q);
  }

  public boolean trackStaticFields() {
    return this.trackStaticFields;
  }

  public void setCurrentForwardSolver(ForwardSolver solver) {
    getSubQuery().setCurrentForwardSolver(solver);
  }

  public ForwardSolver getCurrentForwardSolver() {
    return getSubQuery().getCurrentForwardSolver();
  }

}
