package example;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Stopwatch;

import boomerang.AliasFinder;
import boomerang.AliasResults;
import boomerang.context.AllCallersRequester;
import boomerang.preanalysis.PreparationTransformer;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.options.Options;

public class Example {

  public static void main(String[] args) {

    // Example 1
    initializeSoot("example.Program1");
    InfoflowCFG icfg = new InfoflowCFG();
    AliasFinder af = new AliasFinder(icfg);
    // A simple example: searching for aliases of the variable oneAlias at the 7th statement of
    // method main.
    Query query = new Query("<example.Program1: void main(java.lang.String[])>", "oneAlias", 7);

    af.startQuery();
    AliasResults aliases = af.findAliasAtStmt(query.getAccessGraph(), query.getStatement());
    System.out.println(query);
    System.out.println(aliases);

    // Example 2
    initializeSoot("example.Program2");
    icfg = new InfoflowCFG();
    af = new AliasFinder(icfg);
    // An example where fields are used: searching for aliases of alias.field
    query = new Query("<example.Program2: void main(java.lang.String[])>", "alias[field]", 9);
    af.startQuery();
    aliases = af.findAliasAtStmt(query.getAccessGraph(), query.getStatement());
    System.out.println(query);
    System.out.println(aliases.withMethodOfAllocationSite(icfg));


    // Example 3a
    // No Client-driven context-resolution
    initializeSoot("example.Program3");
    icfg = new InfoflowCFG();
    af = new AliasFinder(icfg);
    query = new Query("<example.Program3: void context(java.lang.Object,java.lang.Object)>",
        "andAnother", 4);
    af.startQuery();
    // The results the query provides are with no context information by default. Therefore, in that
    // example we do not yet have an allocation site, but will retrieve local aliases. The missing
    // allocation sites are found, if we use the AllCallersRequester (see Example 3b).
    aliases = af.findAliasAtStmt(query.getAccessGraph(), query.getStatement());
    System.out.println(query);
    System.out.println(aliases.withMethodOfAllocationSite(icfg));

    // Example 3b
    // Client-driven context-resolution (using All context)
    initializeSoot("example.Program3");
    icfg = new InfoflowCFG();
    af = new AliasFinder(icfg);
    query =
 new Query("<example.Program3: void context(java.lang.Object,java.lang.Object)>",
        "andAnother", 4);
    // An example for the client-driven context-resolution (using AllCallersRequester, that means we
    // go up all contexts to search for all possible allocation sites across all callers of the
    // method)
    af.startQuery();
    aliases = af.findAliasAtStmt(query.getAccessGraph(), query.getStatement(),
        new AllCallersRequester());
    System.out.println(query);
    System.out.println(aliases.withMethodOfAllocationSite(icfg));
  }


  @SuppressWarnings("static-access")
  private static void initializeSoot(String mainClass) {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("cg.spark", "on");

    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + "/targetsBin";
    Options.v().set_soot_classpath(sootCp);

    Options.v().set_prepend_classpath(true);
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_main_class(mainClass);

    Scene.v().addBasicClass(mainClass, SootClass.BODIES);
    Scene.v().loadNecessaryClasses();
    SootClass c = Scene.v().forceResolve(mainClass, SootClass.BODIES);
    if (c != null) {
      c.setApplicationClass();
    }
    SootMethod methodByName = c.getMethodByName("main");
    List<SootMethod> ePoints = new LinkedList<>();
    ePoints.add(methodByName);
    Scene.v().setEntryPoints(ePoints);
    // Add a transformer
    PackManager.v().getPack("wjtp")
        .add(new Transform("wjtp.preparationTransform", new PreparationTransformer()));
    PackManager.v().getPack("cg").apply();
    PackManager.v().getPack("wjtp").apply();
  }

}
