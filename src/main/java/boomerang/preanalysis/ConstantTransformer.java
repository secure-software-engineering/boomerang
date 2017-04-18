package boomerang.preanalysis;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
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
import soot.jimple.internal.JimpleLocal;

public class ConstantTransformer extends BodyTransformer{
	private int replaceCounter = 1;

	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		List<Unit> constant = getConstants(b);
		transform(b, constant);
	}

	private void transform(Body body, List<Unit> constant) {
		for (Unit u : constant) {
			if (u instanceof ReturnStmt && !(u instanceof ReturnVoidStmt)) {
				ReturnStmt returnStmt = (ReturnStmt) u;
				ValueBox opBox = returnStmt.getOpBox();
				Value value = opBox.getValue();
				String label = "varReplacer" + new Integer(replaceCounter).toString();
				Local paramVal = new JimpleLocal(label, value.getType());
				replaceCounter++;
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

						String label = "varReplacer" + new Integer(replaceCounter).toString()
								+ "i" + useBoxes.indexOf(vb);
						replaceCounter++;
						Local paramVal = new JimpleLocal(label, v.getType());
						AssignStmt newUnit = new JAssignStmt(paramVal,
								vb.getValue());
						body.getLocals().add(paramVal);
						body.getUnits().insertBefore(newUnit, u);
						vb.setValue(paramVal);
					}
				}
			}
		}
	}


	private List<Unit> getConstants(Body b) {
		List<Unit> ret = new LinkedList<>();
		for (Unit u : b.getUnits()) {
			if (u instanceof ReturnStmt) {
				if (((ReturnStmt) u).getOp() instanceof Constant) {
					ret.add(u);
				}
			} else if (((Stmt) u).containsInvokeExpr()) {
				InvokeExpr ie = ((Stmt) u).getInvokeExpr();
				for (Value arg : ie.getArgs()) {
					if (arg instanceof StringConstant || arg instanceof NumericConstant) {
						ret.add(u);
						break;
					}
				}
			}
		}
		return ret;
	}

}

