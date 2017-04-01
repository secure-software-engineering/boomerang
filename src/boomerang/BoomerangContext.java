package boomerang;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.lang.model.type.PrimitiveType;

import com.google.common.base.Stopwatch;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.backward.BackwardFlowFunctions;
import boomerang.backward.BackwardProblem;
import boomerang.backward.BackwardSolver;
import boomerang.bidi.PathEdgeStore;
import boomerang.context.IContextRequester;
import boomerang.debug.IBoomerangDebugger;
import boomerang.debug.JSONOutputDebugger;
import boomerang.forward.ForwardFlowFunctions;
import boomerang.forward.ForwardProblem;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.PathEdge;
import boomerang.mock.DefaultBackwardDataFlowMocker;
import boomerang.mock.DefaultForwardDataFlowMocker;
import boomerang.mock.DefaultNativeCallHandler;
import boomerang.mock.MockedDataFlow;
import boomerang.mock.NativeCallHandler;
import boomerang.pointsofindirection.AliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import heros.FlowFunction;
import heros.solver.Pair;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ReturnStmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

@SuppressWarnings("serial")
public class BoomerangContext {

	/**
	 * The inter-procedural control flow graph to be used.
	 */
	public IInfoflowCFG icfg;

	public IBoomerangDebugger debugger;

	/**
	 * The inter-procedural backward control flow graph to be used.
	 */
	public IInfoflowCFG bwicfg;


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

	private Set<PointOfIndirection> processedPOIs = new HashSet<>();
	private Set<SootMethod> backwardVisitedMethods = new HashSet<>();

	public BoomerangContext(IInfoflowCFG icfg, IInfoflowCFG bwicfg) {
		this(icfg, bwicfg, new BoomerangOptions());
	}

	public BoomerangContext(IInfoflowCFG icfg, IInfoflowCFG bwicfg, BoomerangOptions options) {
		this.icfg = icfg;
		this.bwicfg = bwicfg;
		this.debugger = options.getDebugger();
		if (debugger instanceof JSONOutputDebugger)
			System.err.println("WARNING: Using JSON output slows down performance");
		this.debugger.setContext(this);
		this.budgetInMilliSeconds = options.getTimeBudget();
		WrappedSootField.TRACK_STMT = options.getTrackStatementsInFields();
		this.trackStaticFields = options.getTrackStaticFields();
		if(!trackStaticFields)
			System.err.println("Boomerang does not track static fields");

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

	public boolean isIgnoredMethod(SootMethod m) {
		return false;
	}

	public void sanityCheckEdge(IPathEdge<Unit, AccessGraph> edge) {
		if (edge.getTarget() == null)
			return;
		SootMethod m1 = icfg.getMethodOf(edge.getTarget());
		assert !isIgnoredMethod(m1) : "The path edge resides in a method which should be ignored " + m1.toString();
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

	private ForwardSolver forwardSolver;

	private BackwardSolver backwardSolver;

	private IContextRequester contextRequester;

	public boolean isOutOfBudget() {
		if (startTime.elapsed(TimeUnit.MILLISECONDS) > budgetInMilliSeconds)
			return true;
		return false;
	}

	public void validateInput(AccessGraph ap, Unit stmt) {
		SootMethod m = bwicfg.getMethodOf(stmt);
		if (!ap.isStatic() && !m.getActiveBody().getLocals().contains(ap.getBase())) {
			throw new IllegalArgumentException(
					"Base value of access path " + ap + " is not a local of the Method at which the Query was asked!");
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

	public boolean trackStaticFields() {
		return this.trackStaticFields;
	}

	public boolean visitedBackwardMethod(SootMethod m) {
		return backwardVisitedMethods.contains(m);
	}

	public void addAsVisitedBackwardMethod(SootMethod m) {
		backwardVisitedMethods.add(m);
	}

	public ForwardSolver getForwardSolver() {
		if (forwardSolver == null) {
			ForwardProblem forwardProblem = new ForwardProblem(this);
			forwardSolver = new ForwardSolver(forwardProblem, this);
		}
		return forwardSolver;
	}

	public BackwardSolver getBackwardSolver() {
		if(backwardSolver == null){
			BackwardProblem problem = new BackwardProblem(this);
			backwardSolver = new BackwardSolver(problem, this);
		}
		return backwardSolver;
	}

	public PathEdgeStore getForwardPathEdges() {
		return (PathEdgeStore) getForwardSolver().getPathEdges();
	}

	public Set<? extends IPathEdge<Unit, AccessGraph>> getForwardIncomings(AccessGraph startNode,SootMethod m) {
		return getForwardSolver().incoming(startNode, m);
	}
	public void registerPOI(Unit stmt, PointOfIndirection poi, AliasCallback cb) {
		getForwardPathEdges().registerPointOfIndirectionAt(stmt, poi,cb);
	}

	public void setContextRequester(IContextRequester req) {
		this.contextRequester = req;
	}
	
	public IContextRequester getContextRequester(){
		return contextRequester;
	}
	
	public Set<AccessGraph> getForwardTargetsFor(AccessGraph d2, Unit callSite, SootMethod callee) {
		Collection<Unit> calleeSps = this.icfg.getStartPointsOf(callee);
		Set<AccessGraph> factsInCallee = new HashSet<>();
		ForwardFlowFunctions ptsFunction = new ForwardFlowFunctions(this);
		for (Unit calleeSp : calleeSps) {
			FlowFunction<AccessGraph> callFlowFunction = ptsFunction.getCallFlowFunction(
					new PathEdge<Unit, AccessGraph>(null, callSite, null), callee, calleeSp);
			Set<AccessGraph> targets = callFlowFunction.computeTargets(d2);
			factsInCallee.addAll(targets);
		}
		return factsInCallee;
	}

}
