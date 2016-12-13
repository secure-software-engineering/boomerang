package example;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import boomerang.AliasFinder;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.util.Chain;

public class Util {

  public static AccessGraph stringToAccessGraph(Chain<Local> locals, String arg) {
    String[] base = arg.split("\\[");
    LocalWithType baseVar = new LocalWithType(base[0], locals);
    if (base.length == 1) {
      return new AccessGraph(baseVar.getLocal(), baseVar.getType());
    }
    String rest = base[1];
    rest = rest.substring(0, rest.length() - 1);
    String[] splitted = rest.split(",");
    Type t = null;
    if (baseVar != null)
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
        fields[i] = new WrappedSootField(AliasFinder.ARRAY_FIELD, t, null);
      } else {
        throw new RuntimeException("Parsing of fields of locals failed");
      }

    }
    return new AccessGraph(baseVar.getLocal(), baseVar.getType(), fields);
  }

  private static class VarWithType {
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

  private static class LocalWithType extends VarWithType {
    private Chain<Local> locals;

    LocalWithType(String s, Chain<Local> locals) {
      super(s);
      this.locals = locals;
    }

    Local getLocal() {
      return findSingleLocal(locals, varName);
    }

    Type getType() {
      return type == null ? getLocal().getType() : type;
    }
  }

  private static class FieldWithType extends VarWithType {
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

  public static Unit getNthStmtOfMethod(SootMethod method, int n) {
    PatchingChain<Unit> units = method.getActiveBody().getUnits();
    int count = 0;
    if (n >= units.size())
      throw new RuntimeException(
          "The method " + method.toString() + " has only " + units.size() + " units!");
    for (Unit u : units) {
      if (count == n) {
        return u;
      }
      count++;
    }

    throw new RuntimeException(
        "The specified unit " + n + " was not found in " + method.toString());
  }

}
