package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class SuperClassTest  extends AliasTest{
	@Test
	public void superclassTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.SuperClassTarget: void superTest()>").
							askForLocalAtStmt(23, "x", andExpect(19,"x","$r1[c]","a[c]","e[c]")).
									askForLocalAtStmt(23, "z", andExpect(15,"z","$r1[f]","$r4","a[f]","e[f]")).
									askForLocalAtStmt(23, "y", andExpect(19,"y","$r1[d]","a[d]","e[d]")).
									askForLocalAtStmt(23, "y", andExpect(19,"y","$r1[d]","a[d]","e[d]")));

		
				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		return "cases.SuperClassTarget";
	}
	
}
