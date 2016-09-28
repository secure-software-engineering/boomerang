package boomerang.preanalysis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.NumericConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JNopStmt;
import soot.jimple.internal.JimpleLocal;

public class PreparationTransformer extends SceneTransformer {
  private int replaceCounter = 1;

  @Override
  protected void internalTransform(String phaseName, Map<String, String> options) {
    addNopStmtToMethods();
    transformConstantInInvokes();
  }

  private void transformConstantInInvokes() {
    Map<Unit, Body> cwnc = getStmtsWithConstants();
    for (Unit u : cwnc.keySet()) {
      Body body = cwnc.get(u);
      if (u instanceof ReturnStmt && !(u instanceof ReturnVoidStmt)) {
        ReturnStmt returnStmt = (ReturnStmt) u;
        ValueBox opBox = returnStmt.getOpBox();
        Value value = opBox.getValue();
        String label = "varReplacer" + new Integer(replaceCounter++).toString();
        Local paramVal = new JimpleLocal(label, value.getType());
        AssignStmt newUnit = new JAssignStmt(paramVal, opBox.getValue());
        body.getLocals().add(paramVal);
        body.getUnits().insertBefore(newUnit, u);
        opBox.setValue(paramVal);
      } else {
        InvokeExpr ie = ((Stmt) u).getInvokeExpr();
        List<ValueBox> useBoxes = ie.getUseBoxes();
        for (ValueBox vb : useBoxes) {
          Value v = vb.getValue();
          if (v instanceof Constant) {

            String label =
                "varReplacer" + new Integer(replaceCounter).toString() + "i" + useBoxes.indexOf(vb);
            replaceCounter++;
            Local paramVal = new JimpleLocal(label, v.getType());
            AssignStmt newUnit = new JAssignStmt(paramVal, vb.getValue());
            body.getLocals().add(paramVal);
            cwnc.get(u).getUnits().insertBefore(newUnit, u);
            vb.setValue(paramVal);
          }
        }
      }
    }
  }

  private void addNopStmtToMethods() {
    for (SootClass c : Scene.v().getClasses()) {
      for (SootMethod m : c.getMethods()) {
        if (!m.hasActiveBody()) {
          continue;
        }
        Body b = m.getActiveBody();
        b.getUnits().addFirst(new JNopStmt());
      }
    }
  }

  private Map<Unit, Body> getStmtsWithConstants() {
    Map<Unit, Body> retMap = new LinkedHashMap<Unit, Body>();
    for (SootClass sc : Scene.v().getClasses()) {
      for (SootMethod sm : sc.getMethods()) {
        if (!sm.hasActiveBody())
          continue;
        Body methodBody = sm.retrieveActiveBody();
        for (Unit u : methodBody.getUnits()) {
          if (u instanceof ReturnStmt) {
            if (((ReturnStmt) u).getOp() instanceof Constant) {
              retMap.put(u, methodBody);
            }
          } else if (((Stmt) u).containsInvokeExpr()) {
            InvokeExpr ie = ((Stmt) u).getInvokeExpr();
            for (Value arg : ie.getArgs()) {
              if (arg instanceof StringConstant || arg instanceof NumericConstant) {
                retMap.put(u, methodBody);
                break;
              }
            }
          }
        }
      }
    }
    return retMap;
  }

}
