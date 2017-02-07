package test.selfrunning;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import heros.solver.Pair;
import soot.ArrayType;
import soot.Body;
import soot.Context;
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
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;

public class AbstractTest {
	private JimpleBasedInterproceduralCFG icfg;
    @Rule public TestName name = new TestName();
	private SootMethod sootTestMethod;


	@After
	public void performQuery() {
		initializeSootWithEntryPoint(name.getMethodName());
		analyze(name.getMethodName());
	}

	private void analyze(final String methodName) {
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				icfg = new JimpleBasedInterproceduralCFG();
				
				Query q = parseQuery();
				AliasResults expectedResults = parseExpectedQueryResults();
				System.out.println(expectedResults);
			}

		});

		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}
	private AliasResults parseExpectedQueryResults() {
		Set<Pair<Unit, AccessGraph>> allocationSiteWithCallStack = parseAllocationSitesWithCallStack();
		System.out.println(allocationSiteWithCallStack);
		Set<Local> aliasedVariables = parseAliasedVariables();
		System.out.println(aliasedVariables);
		AliasResults expectedResults = associateVariableAliasesToAllocationSites(allocationSiteWithCallStack,
				aliasedVariables);
		
		return expectedResults;
	}
	
	private AliasResults associateVariableAliasesToAllocationSites(
			Set<Pair<Unit, AccessGraph>> allocationSiteWithCallStack, Set<Local> aliasedVariables) {
		AliasResults res = new AliasResults();
		for(Pair<Unit, AccessGraph> allocatedVariableWithStack : allocationSiteWithCallStack){
			for(Local l : aliasedVariables){
				res.put(allocatedVariableWithStack, new AccessGraph(l,l.getType()));
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
			if (!assignStmt.getLeftOp().toString().contains("alias") && !assignStmt.getLeftOp().toString().contains("query") )
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
				if(allocatesObjectOfInterest((NewExpr)as.getRightOp())){
					Local local = (Local) as.getLeftOp();
					AccessGraph accessGraph = new AccessGraph(local, local.getType());
					out.add(new Pair<Unit, AccessGraph>(as,accessGraph.deriveWithAllocationSite(as)));
				}
			}

		}
		return out;
	}

	private boolean allocatesObjectOfInterest(NewExpr rightOp) {
		return Scene.v().getFastHierarchy().getSubclassesOf(Scene.v().getSootClass("test.selfrunning.AllocatedObject")).contains(rightOp.getBaseType().getSootClass());
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
					if(allocatesObjectOfInterest((NewExpr)as.getRightOp())){
						Local local = (Local) as.getLeftOp();
						AccessGraph accessGraph = new AccessGraph(local, local.getType());
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
			throw new RuntimeException("The method that contains the query does not have a body" + sootTestMethod.getName());
		Body activeBody = sootTestMethod.getActiveBody();
		for (Unit u : activeBody.getUnits()) {
			if (!(u instanceof AssignStmt))
				continue;
			AssignStmt assignStmt = (AssignStmt) u;
			if (!(assignStmt.getLeftOp() instanceof Local))
				continue;
			if (!assignStmt.getLeftOp().toString().contains("query"))
				continue;
			Local queryVar = (Local) assignStmt.getLeftOp();
			return new Query(new AccessGraph(queryVar, queryVar.getType()), assignStmt);
		}
		throw new RuntimeException("No variable whose name contains query has been found in " + sootTestMethod.getName());
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
//			 Options.v().setPhaseOption("cg", "all-reachable:true");
		}
		
		Options.v().set_soot_classpath(sootCp);
//		Options.v().set_main_class(this.getTargetClass());
		SootClass sootTestCaseClass = Scene.v().forceResolve(getTestCaseClassName(), SootClass.BODIES);
		for(SootMethod m : sootTestCaseClass.getMethods())
			if(m.getName().equals(methodName))
					sootTestMethod = m;
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
			    Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
			    VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		sootClass.addMethod(mainMethod);
		JimpleBody body = Jimple.v().newBody(mainMethod);
		mainMethod.setActiveBody(body);
		RefType testCaseType = RefType.v(getTestCaseClassName());
		Local allocatedTestObj = Jimple.v().newLocal("dummyObj",testCaseType );
		body.getLocals().add(allocatedTestObj);
		body.getUnits().add(Jimple.v().newAssignStmt(allocatedTestObj, 
		      Jimple.v().newNewExpr(testCaseType)));
		body.getUnits().add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(allocatedTestObj,sootTestMethod.makeRef())));
		Scene.v().addClass(sootClass);
		return sootClass.toString();
	}

	private String getTestCaseClassName() {
		return this.getClass().getName().replace("class ","");
	}

	protected boolean includeJDK() {
		return false;
	}
}
