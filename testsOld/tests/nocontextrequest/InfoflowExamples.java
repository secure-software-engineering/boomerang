package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class InfoflowExamples extends AliasTest {
	
	@Test
	public void doubledQueryTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.DoubleQueryTarget: void inner(cases.A)>").
							askForLocalAtStmt(9, "x", 
									andExpect(3,"x","a[f]","c","$r0")));

				
				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.DoubleQueryTarget";
	}
	

}
