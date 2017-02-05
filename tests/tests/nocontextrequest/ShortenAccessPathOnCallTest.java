package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class ShortenAccessPathOnCallTest  extends AliasTest{
	@Test
	public void simpleTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ShortenAccessPathAtCall: void test1()>").
							askForLocalAtStmt(16, "x", 
									andExpect(6,"x","$r0[f]","a[f]","b[a,f]","$r3[a,f]")));

				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.ShortenAccessPathAtCall";
	}
}
