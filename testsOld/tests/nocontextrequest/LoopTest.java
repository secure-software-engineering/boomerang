package tests.nocontextrequest;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class LoopTest	extends AliasTest{
	
	@Test
	public void test1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.LoopTarget: void test1()>").
							askForLocalAtStmt(28, "h", 
									andExpectNonEmpty()));
				
				return res;
			}
		}); 
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.LoopTarget";
	}
}
