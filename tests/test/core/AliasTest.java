package test.core;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import heros.solver.Pair;
import soot.G;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.spark.pag.Node;
import soot.jimple.stmtselector.StmtFinder;
import soot.options.Options;
import soot.util.Chain;
import soot.util.HashMultiMap;
import soot.util.MultiMap;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;


public abstract class AliasTest<Fact, Results> {
  private int actualQueryCount;
  private int expectedQueryCount;
  protected static Logger logger = LoggerFactory.getLogger(AliasTest.class);
  private Map<Pair<Unit, Fact>, Results> answeredQuery;
  IAliasStrategy<Fact, Results> strat;



  @SuppressWarnings("unchecked")
  public AliasTest() {
    strat = (IAliasStrategy<Fact, Results>) new BoomerangAliasStrategy();
  }

  @Before
  public void setup() {
    strat.setup();
    actualQueryCount = 0;
    expectedQueryCount = 0;
  }

  public static boolean deleteDirectory(File directory) {
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (null != files) {
        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            deleteDirectory(files[i]);
          } else {
            files[i].delete();
          }
        }
      }
    }
    return (directory.delete());
  }

  @After
  public void checkCounters() {
    // assertEquals("Some queries were not executed!", expectedQueryCount, actualQueryCount);
  }

  public void runAnalysis(final boolean goOutOfMethod, final IQueryHandler handler) {
    initializeSoot();
    strat.afterSootInit();
    for (MethodQueries mq : handler.queryAndResults()) {
      expectedQueryCount += mq.getQueries().size();
    }

    answeredQuery = new HashMap<>();
    Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

      protected void internalTransform(String phaseName,
          @SuppressWarnings("rawtypes") Map options) {
        strat.beforeAll();

        for (MethodQueries mq : handler.queryAndResults()) {
          String method = mq.getMethod();

          for (Query q : mq.getQueries()) {
            // Persistent.resetJumpFn();
            int stmtNo = q.getStmtNo();
            try {
              System.out.println(Scene.v().getMethod(method).getActiveBody());
            } catch (RuntimeException e) {
              for (SootClass c : Scene.v().getApplicationClasses()) {
                System.out.println(c.getMethods());
              }
            }
            Set<Unit> nthStmt = StmtFinder.inMethod(method).after().nthStmt(stmtNo);
            Chain<Local> locals = Scene.v().getMethod(method).getActiveBody().getLocals();

            String queriedFactAsString = q.getQueriedValue();
            Fact queryFact = strat.parseFact(method, locals, queriedFactAsString);
            for (Unit u : nthStmt) {
              Multimap<Unit, Fact> expected = HashMultimap.create();
              boolean nonEmpty = false;

              long before = System.currentTimeMillis();

              Results results = strat.query(queryFact, u, goOutOfMethod);
              long after = System.currentTimeMillis();
              for (ResultObject res : q.getExpectedResults()) {
                if (res.isNonEmpty()) {
                  nonEmpty = true;
                }
                if (res.isEmpty() || res.isNonEmpty())
                  continue;

                String queryMethod;
                if (res.getMethod() != null) {
                  queryMethod = res.getMethod();
                  System.out.println(Scene.v().getMethod(queryMethod).getActiveBody());
                } else {
                  queryMethod = method;
                }
                Set<Unit> allocStmts =
                    StmtFinder.inMethod(queryMethod).after().nthStmt(res.getStmtNo());

                for (Unit allocSite : allocStmts) {
                  Set<Fact> expectedPerAllocSite =
                      parseAllFacts(method, locals, res.getExpectedresult());

                  expected.putAll(allocSite, expectedPerAllocSite);
                }
              }

              System.out.println("SUB QUERY TOOK: " + (after - before) + "ms");
              System.out.println("SUB QUERY TOOK Results: " + results);
              answeredQuery.put(new Pair<Unit, Fact>(u, queryFact), results);
              Multimap<Unit, Fact> cleanedAliases = strat.makeComparable(results);

              
              if (nonEmpty) {
                if( cleanedAliases.size() == 0)
                  throw new RuntimeException("Unsound result. Should contain some results!");
              } else {
                Set<Entry<Unit, Fact>> falseNegatives = new HashSet<>(expected.entries());
                falseNegatives.removeAll(cleanedAliases.entries());
                Set<Entry<Unit, Fact>> falsePositives = new HashSet<>(cleanedAliases.entries());
                falsePositives.removeAll(expected.entries());
                String answer = "Query: " + queryFact + "@" + u + "€" + method + " \n"
                    + (falseNegatives.isEmpty() ? "" : "\nFN:" + falseNegatives)
                + (falsePositives.isEmpty() ? ""
                      : "\nFP:" + falsePositives + "\n");
                if(!falseNegatives.isEmpty()){
                  throw new RuntimeException("Unsound results for:" + answer);
                }
                assertEquals(
"Imprecise results: " + answer,
                    expected, cleanedAliases);
              }
              actualQueryCount++;
            }
          }
        }

      }


    });

    PackManager.v().getPack("wjtp").add(transform);
    PackManager.v().getPack("cg").apply();
    PackManager.v().getPack("wjtp").apply();

  }

  protected MultiMap<Node, AccessGraph> filterByMethod(MultiMap<Node, AccessGraph> in,
      SootMethod method) {
    MultiMap<Node, AccessGraph> out = new HashMultiMap<>();
    for (Node n : in.keySet()) {
      for (AccessGraph alias : in.get(n)) {
        if (method.getActiveBody().getLocals().contains(alias.getBase())) {
          out.put(n, alias);
        }
      }
    }
    return out;
  }

  protected Set<Fact> parseAllFacts(String method, Chain<Local> locals, String[] expectedresult) {
    Set<Fact> out = new HashSet<>();
    for (String exp : expectedresult)
      out.add(strat.parseFact(method, locals, exp));
    return out;
  }

  @SuppressWarnings("static-access")
  private void initializeSoot() {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().setPhaseOption("cg.spark", "verbose:true");
    // Options.v().set_output_format(Options.output_format_none);
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + "/targetsBin";
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
      Options.v().setPhaseOption("cg", "all-reachable:true");
    }
    Options.v().set_soot_classpath(sootCp);
    Options.v().set_main_class(this.getTargetClass());

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

  protected boolean includeJDK() {
    return false;
  }

  public static void main(String... args) {
    String[] arg = new String[] {"-output-format", "jimple", "-p", "jb", "use-original-names:true",
        "-allow-phantom-refs", "-no-bodies-for-excluded", "-x", "javax.", "-x", "java.",
        "-process-dir", "/Users/spaeth/Documents/workspace/soot-infoflow-alias/bin/"};

    soot.Main.main(arg);
  }

  public abstract String getTargetClass();



  public static Local findSingleLocal(Chain<Local> locals, String arg) {
    if (arg.toString().equals("STATIC")) {
      return null;
    }
    for (Local l : locals) {
      if (l.toString().equals(arg)) {
        return l;
      }
    }
    throw new RuntimeException(String.format("Could not find local %s in %s", arg, locals));
  }
}
