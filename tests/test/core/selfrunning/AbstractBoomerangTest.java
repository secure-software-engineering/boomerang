package test.core.selfrunning;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import boomerang.AliasFinder;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import heros.solver.Pair;
import soot.ArrayType;
import soot.Body;
import soot.G;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Modifier;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.util.queue.QueueReader;
import test.core.TestBoomerangOptions;

public class AbstractBoomerangTest {
	private IInfoflowCFG icfg;
	@Rule
	public TestName name = new TestName();
	private SootMethod sootTestMethod;

	@Before
	public void performQuery() {
		initializeSootWithEntryPoint(name.getMethodName());
		analyze(name.getMethodName());

		// To never execute the @Test method...
		org.junit.Assume.assumeTrue(false);
	}

	private void analyze(final String methodName) {
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				icfg = new InfoflowCFG(new JimpleBasedInterproceduralCFG());

				Query q = parseQuery();
				AliasResults expectedResults = parseExpectedQueryResults(q);
				AliasResults results = runQuery(q);
				compareQuery(q, expectedResults, results);
			}

		});

		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}

	private void compareQuery(Query q, AliasResults expectedResults, AliasResults results) {
		System.out.println("Boomerang Allocations Sites: " + results.keySet());
		System.out.println("Boomerang Results: " + results);
		System.out.println("Expected Results: " + expectedResults);
		Set<Pair<Unit, AccessGraph>> falseNegativeAllocationSites = new HashSet<>(expectedResults.keySet());
		falseNegativeAllocationSites.removeAll(results.keySet());
		results.keySet().equals(expectedResults.keySet());
		Set<Pair<Unit, AccessGraph>> falsePositiveAllocationSites = new HashSet<>(results.keySet());
		falsePositiveAllocationSites.removeAll(expectedResults.keySet());
		String answer = "Query: " + q.getAp() + "@" + q.getStmt() + "€" + q.getMethod() + " \n"
				+ (falseNegativeAllocationSites.isEmpty() ? "" : "\nFN:" + falseNegativeAllocationSites)
				+ (falsePositiveAllocationSites.isEmpty() ? "" : "\nFP:" + falsePositiveAllocationSites + "\n");
		if (!falseNegativeAllocationSites.isEmpty()) {
			throw new RuntimeException("Unsound results for:" + answer);
		}
		Set<String> aliasVariables = new HashSet<>();
		for (AccessGraph g : results.values()) {
			aliasVariables.add(g.toString());
		}
		for (AccessGraph remove : expectedResults.values())
			aliasVariables.remove(remove.toString());
		HashSet<String> missingVariables = new HashSet<>();

		for (String g : aliasVariables) {
			if (g.contains("alias"))
				missingVariables.add(g);
		}
		if (!missingVariables.isEmpty())
			throw new RuntimeException("Unsound, missed variables " + missingVariables);
		if (!falsePositiveAllocationSites.isEmpty())
			Assert.fail("Imprecise results: " + answer);

	}

	private AliasResults runQuery(Query q) {

		AliasFinder boomerang = new AliasFinder(icfg, new TestBoomerangOptions());
		boomerang.startQuery();
		return boomerang.findAliasAtStmt(q.getAp(), q.getStmt());
	}

	private AliasResults parseExpectedQueryResults(Query q) {
		Set<Pair<Unit, AccessGraph>> allocationSiteWithCallStack = parseAllocationSitesWithCallStack();
		Set<Local> aliasedVariables = parseAliasedVariables();
		aliasedVariables.add(q.getAp().getBase());
		AliasResults expectedResults = associateVariableAliasesToAllocationSites(allocationSiteWithCallStack,
				aliasedVariables);

		return expectedResults;
	}

	private AliasResults associateVariableAliasesToAllocationSites(
			Set<Pair<Unit, AccessGraph>> allocationSiteWithCallStack, Set<Local> aliasedVariables) {
		AliasResults res = new AliasResults();
		for (Pair<Unit, AccessGraph> allocatedVariableWithStack : allocationSiteWithCallStack) {
			for (Local l : aliasedVariables) {
				res.put(allocatedVariableWithStack, new AccessGraph(l, l.getType()));
			}
		}
		return res;
	}

	private Set<Local> parseAliasedVariables() {
		Set<Local> out = new HashSet<>();
		Body activeBody = sootTestMethod.getActiveBody();
		for (Unit u : activeBody.getUnits()) {
			if (!(u instanceof AssignStmt))
				continue;
			AssignStmt assignStmt = (AssignStmt) u;
			if (!(assignStmt.getLeftOp() instanceof Local))
				continue;
			if (!assignStmt.getLeftOp().toString().contains("alias")
					&& !assignStmt.getLeftOp().toString().contains("query"))
				continue;
			Local aliasedVar = (Local) assignStmt.getLeftOp();
			out.add(aliasedVar);
		}
		return out;
	}

	private Set<Pair<Unit, AccessGraph>> parseAllocationSitesWithCallStack() {
		Set<Unit> callsites = icfg.getCallsFromWithin(sootTestMethod);
		Set<Pair<Unit, AccessGraph>> out = new HashSet<>();
		for (Unit call : callsites) {
			for (AccessGraph accessGraphAtAllocationSite : transitivelyReachableAllocationSite(call,
					new HashSet<SootMethod>())) {
				out.add(new Pair<Unit, AccessGraph>(call, accessGraphAtAllocationSite));
			}
		}
		for (Unit u : sootTestMethod.getActiveBody().getUnits()) {
			if (!(u instanceof AssignStmt))
				continue;
			AssignStmt as = (AssignStmt) u;

			if (as.getLeftOp() instanceof Local && as.getRightOp() instanceof NewExpr) {
				if (allocatesObjectOfInterest((NewExpr) as.getRightOp())) {
					Local local = (Local) as.getLeftOp();
					AccessGraph accessGraph = new AccessGraph(local, ((NewExpr) as.getRightOp()).getBaseType());
					out.add(new Pair<Unit, AccessGraph>(as, accessGraph.deriveWithAllocationSite(as)));
				}
			}

		}
		return out;
	}

	private boolean allocatesObjectOfInterest(NewExpr rightOp) {
		SootClass interfaceType = Scene.v().getSootClass("test.core.selfrunning.AllocatedObject");
		RefType allocatedType = rightOp.getBaseType();
		return Scene.v().getActiveHierarchy().getImplementersOf(interfaceType).contains(allocatedType.getSootClass());
	}

	private Set<AccessGraph> transitivelyReachableAllocationSite(Unit call, Set<SootMethod> visited) {
		Set<AccessGraph> out = new HashSet<>();
		for (SootMethod m : icfg.getCalleesOfCallAt(call)) {
			if (visited.contains(m))
				continue;
			visited.add(m);
			if (!m.hasActiveBody())
				continue;
			for (Unit u : m.getActiveBody().getUnits()) {
				if (!(u instanceof AssignStmt))
					continue;
				AssignStmt as = (AssignStmt) u;

				if (as.getLeftOp() instanceof Local && as.getRightOp() instanceof NewExpr) {
					if (allocatesObjectOfInterest((NewExpr) as.getRightOp())) {
						Local local = (Local) as.getLeftOp();
						AccessGraph accessGraph = new AccessGraph(local, ((NewExpr) as.getRightOp()).getBaseType());
						out.add(accessGraph.deriveWithAllocationSite(as));
					}
				}

			}
			for (Unit u : icfg.getCallsFromWithin(m))
				out.addAll(transitivelyReachableAllocationSite(u, visited));
		}
		return out;
	}

	private Query parseQuery() {
		if (!sootTestMethod.hasActiveBody())
			throw new RuntimeException(
					"The method that contains the query does not have a body" + sootTestMethod.getName());
		Body activeBody = sootTestMethod.getActiveBody();

		System.out.println(sootTestMethod.getActiveBody());
		LinkedList<Query> queries = new LinkedList<>();
		for (Unit u : activeBody.getUnits()) {
			if (!(u instanceof Stmt))
				continue;

			Stmt stmt = (Stmt) u;
			if (!(stmt.containsInvokeExpr()))
				continue;
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			if (!invokeExpr.getMethod().getName().equals("queryFor"))
				continue;
			Value param = invokeExpr.getArg(0);
			if (!(param instanceof Local))
				continue;
			Local queryVar = (Local) param;
			queries.add(new Query(new AccessGraph(queryVar, queryVar.getType()), stmt));
		}
		if (queries.size() == 0)
			throw new RuntimeException(
					"No variable whose name contains query has been found in " + sootTestMethod.getName());
		if (queries.size() > 1)
			System.err.println(
					"More than one possible query found, might be unambigious, picking query " + queries.getLast());
		return queries.getLast();
	}

	@SuppressWarnings("static-access")
	private void initializeSootWithEntryPoint(String methodName) {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		// Options.v().set_output_format(Options.output_format_none);
		String userdir = System.getProperty("user.dir");
		String sootCp = userdir + "/testBin";
		if (includeJDK()) {
			Options.v().set_prepend_classpath(true);
			Options.v().setPhaseOption("cg", "trim-clinit:false");
			Options.v().set_no_bodies_for_excluded(true);
			Options.v().set_allow_phantom_refs(true);

			List<String> includeList = new LinkedList<String>();
			includeList.add("java.lang.*");
			includeList.add("java.util.*");
			includeList.add("java.io.*");
			includeList.add("sun.misc.*");
			includeList.add("java.net.*");
			includeList.add("javax.servlet.*");
			includeList.add("javax.crypto.*");

			includeList.add("android.*");
			includeList.add("org.apache.http.*");

			includeList.add("de.test.*");
			includeList.add("soot.*");
			includeList.add("com.example.*");
			includeList.add("libcore.icu.*");
			includeList.add("securibench.*");
			Options.v().set_include(includeList);

		} else {
			Options.v().set_no_bodies_for_excluded(true);
			Options.v().set_allow_phantom_refs(true);
			// Options.v().setPhaseOption("cg", "all-reachable:true");
		}

		Options.v().set_soot_classpath(sootCp);
		// Options.v().set_main_class(this.getTargetClass());
		SootClass sootTestCaseClass = Scene.v().forceResolve(getTestCaseClassName(), SootClass.BODIES);

		for (SootMethod m : sootTestCaseClass.getMethods()) {
			if (m.getName().equals(methodName))
				sootTestMethod = m;
		}
		if (sootTestMethod == null)
			throw new RuntimeException("The method with name " + methodName + " was not found in the Soot Scene.");
		Scene.v().addBasicClass(getTargetClass(), SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(getTargetClass(), SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}

		SootMethod methodByName = c.getMethodByName("main");
		List<SootMethod> ePoints = new LinkedList<>();
		ePoints.add(methodByName);
		Scene.v().setEntryPoints(ePoints);
	}

	private String getTargetClass() {
		SootClass sootClass = new SootClass("dummyClass");
		SootMethod mainMethod = new SootMethod("main",
				Arrays.asList(new Type[] { ArrayType.v(RefType.v("java.lang.String"), 1) }), VoidType.v(),
				Modifier.PUBLIC | Modifier.STATIC);
		sootClass.addMethod(mainMethod);
		JimpleBody body = Jimple.v().newBody(mainMethod);
		mainMethod.setActiveBody(body);
		RefType testCaseType = RefType.v(getTestCaseClassName());
		System.out.println(getTestCaseClassName());
		Local allocatedTestObj = Jimple.v().newLocal("dummyObj", testCaseType);
		body.getLocals().add(allocatedTestObj);
		body.getUnits().add(Jimple.v().newAssignStmt(allocatedTestObj, Jimple.v().newNewExpr(testCaseType)));
		body.getUnits().add(
				Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(allocatedTestObj, sootTestMethod.makeRef())));
		Scene.v().addClass(sootClass);
		return sootClass.toString();
	}

	private String getTestCaseClassName() {
		return this.getClass().getName().replace("class ", "");
	}

	protected boolean includeJDK() {
		return false;
	}

	/**
	 * The methods parameter describes the variable that a query is issued for.
	 * Note: We misuse the @Deprecated annotation to highlight the method in the
	 * Code.
	 */
	@Deprecated
	protected void queryFor(Object variable) {

	}

	/**
	 * This method can be used in test cases to create branching. It is not
	 * optimized away.
	 * 
	 * @return
	 */
	protected boolean staticallyUnknown() {
		return true;
	}
}
