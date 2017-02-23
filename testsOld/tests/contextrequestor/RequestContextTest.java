package tests.contextrequestor;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class RequestContextTest extends AliasTest {
	@Test
	public void reqContextTest(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void foo(java.lang.StringBuffer,java.lang.StringBuffer)>").
							askForLocalAtStmt(2, "buf", 
									andExpect("<cases.RequestContextTarget: void doGet()>",2,"buf", "buf2")));

				return res;
			}
		});
	}
	
	@Test
	public void reqContextWithTreeTest(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void c3(cases.A,cases.A)>").
							askForLocalAtStmt(5, "x", 
									andExpect("<cases.RequestContextTarget: void c1()>",3,"x","bInC3[f]","aInC3[f]"),
									andExpect("<cases.RequestContextTarget: void a1()>",3,"x","bInC3[f]","aInC3[f]")));

				return res;
			}
		});
	}
	@Test
	public void reqContextWithRecursionTest(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void recTermination(cases.A,cases.A)>").
							askForLocalAtStmt(5, "x", 
									andExpect("<cases.RequestContextTarget: void definition()>",3,"x","alias1Final[f]","alias2Final[f]")));

				return res;
			}
		});
	}
	
	@Test
	public void reqContextWithRecursion2Test(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void recTermination2(cases.A,cases.A)>").
							askForLocalAtStmt(5, "x", 
 andExpect("<cases.RequestContextTarget: void definition2()>",
 3, "x",
                    "alias1Final[f]", "alias2Final[f]"),
                andExpect("<cases.RequestContextTarget: void definition2()>", 6, "x",
                    "alias1Final[f]", "alias2Final[f]")));

				return res;
			}
		});
	}
	
	@Test
	public void reqContextTripleTest(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void recTerminationTriple(cases.A,cases.A,cases.A)>").
							askForLocalAtStmt(6, "x", 
									andExpect("<cases.RequestContextTarget: void definitionTriple()>",3,"x","alias1Final[f]","alias2Final[f]","alias3Final[f]")));

				return res;
			}
		});
	}
	@Test
	public void reqContextDoubleTest(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.RequestContextTarget: void recTerminationDouble(cases.A,cases.A)>").
							askForLocalAtStmt(7, "alias1Final[f]", 
                andExpect(4, "$r0", "alias1Final[f]", "alias2Final[f]"),
                andExpect("<cases.RequestContextTarget: void def()>", 3, "alias1Final[f]",
                    "alias2Final[f]")));

				return res;
			}
		});
	}
	@Test
	public void allocationSiteInOuterMethod(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.RequestContextTarget: cases.A inner(cases.A)>");
				query.addQuery(new Query(4,  "z", new ResultObject("<cases.RequestContextTarget: void outerObject()>", 2, "z","a")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	
	@Override
	public String getTargetClass() {
		return "cases.RequestContextTarget";
	}
}
