package tests.nocontextrequest;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

public class ComplextNextList2 extends AliasTest {

	
	@Test
	public void headQueryTest1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget2: void main(java.lang.String[])>").askForLocalAtStmt(30, "$r5",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	
	@Test
	public void headQueryTest2() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget2: void main(java.lang.String[])>").askForLocalAtStmt(33, "$r8",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	
	@Test
	public void headQueryTest3() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget2: void main(java.lang.String[])>").askForLocalAtStmt(32, "$r6",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.ComplexNextListTarget2";
	}
}
