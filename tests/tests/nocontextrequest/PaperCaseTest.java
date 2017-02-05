package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class PaperCaseTest extends AliasTest {
	
	
	@Test
	public void paperCaseTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.PaperCase: void bar()>").
							askForLocalAtStmt(15, "l4", 
									andExpect(4,"l4" ,"b[f]","c[f]","$r1[f]","a[f]", "$r0[f]" )));

				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		return "cases.PaperCase";
	}
}
