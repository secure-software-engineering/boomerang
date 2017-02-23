package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class StrongUpdateTest extends AliasTest {
	@Test
	public void test1(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.StrongUpdate: void test1()>").
							askForLocalAtStmt(15, "x", 
            andExpect(10, "x", "c[c,b]", "$r3[b]", "a[b]", "$r0[b]", "$r1[c,b]", "$r2"),
            andExpect(4, "$r1[c,b]", "$r3[b]", "c[c,b]", "$r0[b]", "x")));

				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.StrongUpdate";
	}

}
