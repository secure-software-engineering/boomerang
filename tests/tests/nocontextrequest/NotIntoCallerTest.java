package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class NotIntoCallerTest extends AliasTest {
	@Test
	public void askAtCallSiteTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.NotIntoCallTarget: void outer()>").
							askForLocalAtStmt(8, "h[f]", 
									andExpect(3,"h[f]","$r0[f]")));

				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.NotIntoCallTarget";
	}
	
}
