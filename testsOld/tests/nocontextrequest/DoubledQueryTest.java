package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class DoubledQueryTest extends AliasTest {
	
	@Test
	public void doubledQueryTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.DoubleQueryTarget: void inner(cases.A)>").
							askForLocalAtStmt(9, "x", 
									andExpect(3,"x","a[f]","c","$r0")));

				res.add(inMethod("<cases.DoubleQueryTarget: void outer()>").
							askForLocalAtStmt(9, "u", 
									andExpect(6, "u", "d[f]","$r0[f]")));
				return res;
			}
		});
	}
	@Test
	public void simpleQuery(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();

				res.add(inMethod("<cases.DoubleQueryTarget: void outer()>").
							askForLocalAtStmt(9, "u", 
									andExpect(6, "u", "d[f]","$r0[f]")));
				return res;
			}
		});
	}
	@Test
	public void simpleQuery2(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();

				res.add(inMethod("<cases.DoubleQueryTarget: void outer()>").
							askForLocalAtStmt(9, "h", 
									andExpect(3, "h","$r0[f]")));
				return res;
			}
		});
	}	
	@Test
	public void combinedQuery(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();


				res.add(inMethod("<cases.DoubleQueryTarget: void outer()>").
							askForLocalAtStmt(9, "u", 
									andExpect(6, "u", "d[f]","$r0[f]")));
				res.add(inMethod("<cases.DoubleQueryTarget: void outer()>").
							askForLocalAtStmt(9, "h", 
									andExpect(3, "h","$r0[f]")));
				return res;
			}
		});
	}
	@Test
	public void simlpeDoubledQueryTest(){
		runAnalysis(false, new IQueryHandler() {
		@Override
		public ArrayList<MethodQueries> queryAndResults() {
			ArrayList<MethodQueries> res = new ArrayList<>();
			res.add(inMethod("<cases.DoubleQueryTarget: void simple()>").
						askForLocalAtStmt(5,  "a", 
								andExpect(2,"a","$r0")).
						askForLocalAtStmt(6,  "a", 
								andExpect(2,"a", "n","$r0")));
			return res;
		}
	});}

	@Override
	public String getTargetClass() {
		return "cases.DoubleQueryTarget";
	}
}
