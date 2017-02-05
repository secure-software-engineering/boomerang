package test.core;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.context.AllCallersRequester;
import boomerang.context.IContextRequester;
import boomerang.context.NoContextRequester;
import boomerang.preanalysis.PreparationTransformer;
import heros.solver.Pair;
import soot.Local;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.spark.pag.AllocNode;
import soot.jimple.spark.pag.Node;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.util.Chain;
import soot.util.MultiMap;

public class BoomerangAliasStrategy implements IAliasStrategy<AccessGraph, AliasResults> {

  private BoomerangContext dartcontext;
  private InfoflowCFG icfg;

  @Override
  public AccessGraph parseFact(String method, Chain<Local> locals, String arg) {
    String[] base = arg.split("\\[");
    SootMethod m = Scene.v().getMethod(method);
    LocalWithType baseVar = new LocalWithType(base[0], locals);
    if (base.length == 1) {
      return new AccessGraph(baseVar.getLocal(), baseVar.getType());
    }
    String rest = base[1];
    rest = rest.substring(0, rest.length() - 1);
    String[] splitted = rest.split(",");
    Type t = null;
    if (baseVar != null && !arg.startsWith("STATIC"))
      t = baseVar.getType();
    WrappedSootField[] fields = new WrappedSootField[splitted.length];
    for (int i = 0; i < splitted.length; i++) {
      if (t instanceof RefType || t == null) {
        // SootClass sootClass = refType.getSootClass();
        FieldWithType fieldWithType;
        if (splitted[i].startsWith("<")) {
          fieldWithType = new FieldWithType(splitted[i]);
        } else {
          RefType refType = (RefType) t;
          fieldWithType = new FieldWithType(splitted[i], refType);
        }
        t = fieldWithType.getType();
        fields[i] = new WrappedSootField(fieldWithType.getField(), fieldWithType.getType(), null);
      } else if (splitted[i].equals("array")) {
        t = Scene.v().getType("java.lang.Object");
        fields[i] = new WrappedSootField(AliasFinder.ARRAY_FIELD, t, null);
      } else {
        throw new RuntimeException("Parsing of fields of locals failed");
      }

    }
    if (arg.startsWith("STATIC"))
      return new AccessGraph(null, null, fields);
    return new AccessGraph(baseVar.getLocal(), baseVar.getType(), fields);
  }

  private class VarWithType {
    String varName;
    Type type;

    VarWithType(String s) {
      Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(s);
      while (m.find()) {
        type = Scene.v().getRefType(m.group(1));
      }
      if (s.contains("("))
        varName = s.substring(0, s.indexOf("("));
      else
        varName = s;
    }
  }

  private class LocalWithType extends VarWithType {
    private Chain<Local> locals;

    LocalWithType(String s, Chain<Local> locals) {
      super(s);
      this.locals = locals;
    }

    Local getLocal() {
      return AliasTest.findSingleLocal(locals, varName);
    }

    Type getType() {
      return type == null ? getLocal().getType() : type;
    }
  }

  private class FieldWithType extends VarWithType {
    private SootField field;

    FieldWithType(String s) {
      super(s);
      field = Scene.v().getField(s);
    }

    FieldWithType(String name, RefType t) {
      super(name);
      field = t.getSootClass().getFieldByName(varName);
    }

    SootField getField() {
      return field;
    }

    Type getType() {
      return type == null ? field.getType() : type;
    }
  }

  @Override
  public void setup() {
  }

  @Override
  public void beforeAll() {
    icfg = new InfoflowCFG();
  }

  @Override
  public AliasResults query(AccessGraph fact, Unit stmt, boolean allContexts) {
    //
    AliasFinder boomerang = new AliasFinder(icfg, new TestBoomerangOptions());

    IContextRequester c =
        (allContexts ? new AllCallersRequester<BiDiInterproceduralCFG<Unit, SootMethod>>(
boomerang.context.icfg)
        : new NoContextRequester());
    boomerang.startQuery();
    return boomerang.findAliasAtStmt(fact, stmt, c);
  }

  @Override
  public Multimap<Unit, AccessGraph> makeComparable(AliasResults res) {
    Multimap<Unit, AccessGraph> out = HashMultimap.create();
    for (Pair<Unit, AccessGraph> key : res.keySet()) {
      Collection<AccessGraph> value = res.get(key);
      out.putAll(key.getO1(), value);
    }
    return out;
  }

  @Override
  public void afterSootInit() {
    PreparationTransformer preparationTransformer = new PreparationTransformer();
    PackManager.v().getPack("wjtp")
        .add(new Transform("wjtp.preparationTransform", preparationTransformer));
  }

  @Override
  public Local getLocal(AccessGraph fact) {
    return fact.getBase();
  }

  @Override
  public boolean isLocal(AccessGraph fact) {
    return fact.getFieldCount() == 0;
  }

  @Override
  public void compareToSpark(AliasResults res,
 MultiMap<Node, AccessGraph> sparkResults) {
    for (Pair<Unit, AccessGraph> allocSite : res.keySet()) {
      if (!allocSite.getO2().hasAllocationSite())
        continue;
      boolean sparkContains = false;
      for (Node n : sparkResults.keySet()) {
        if (n instanceof AllocNode) {
          AllocNode allocNode = (AllocNode) n;
          NewExpr newExpr = (NewExpr) allocNode.getNewExpr();
          if (allocSite.getO2().hasAllocationSite()
              && ((AssignStmt) allocSite.getO2().getSourceStmt()).getRightOp().equals(newExpr)) {
            sparkContains = true;
            // sparkMustContain(expected, sparkResults.get(n));
          }
        }
      }
      if (!sparkContains)
        throw new AssertionError("Not sound");
    }

  }
}
