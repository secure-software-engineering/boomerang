package tests.nocontextrequest;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class FieldInstanceCheckTest  extends AliasTest{
	@Test
	public void fieldInstanceTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldInstanceCheckTarget: void paramTransferTest()>").
							askForLocalAtStmt(12, "x", 
							    andExpectNonEmpty()));

				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.FieldInstanceCheckTarget";
	}
}
