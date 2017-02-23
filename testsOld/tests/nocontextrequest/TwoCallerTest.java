package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class TwoCallerTest extends AliasTest {
	@Test
	public void twoCallerTest(){
		runAnalysis(true , new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.TwoCallerTarget: void outer1()>").
							askForLocalAtStmt(12, "query1", andExpect(6,"query1", "param3","$r1")).
							askForLocalAtStmt(12, "query2", andExpect(2,"query2", "param2","param1","$r0")));
				res.add(inMethod("<cases.TwoCallerTarget: void outer2()>").
						askForLocalAtStmt(12, "query1", andExpect(2,"query1", "param3", "param2","$r0")).
						askForLocalAtStmt(12, "query2", andExpect(6,"query2", "param1","$r1")));
				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "cases.TwoCallerTarget";
	}
	
}
