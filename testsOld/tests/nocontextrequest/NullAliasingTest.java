package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class NullAliasingTest extends AliasTest {
	@Test
	public void nullAliasingTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.NullAliasingTarget: void nullAliasing()>").
							askForLocalAtStmt(7, "b[c]", 
									andExpect(4,"b[c]", "$r0")));

				return res;
			}
		});
	}
	@Test
	public void returnNullAliasingTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.NullAliasingTarget: void returnNull()>").
							askForLocalAtStmt(5, "o", 
									andExpectEmpty()));

				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.NullAliasingTarget";
	}
	
}
