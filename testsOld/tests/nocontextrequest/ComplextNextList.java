package tests.nocontextrequest;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

public class ComplextNextList extends AliasTest {

	@Test
	public void simpleQuery1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void simple()>").askForLocalAtStmt(14, "head",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}	
	@Test
	public void simpleQueryRec1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void simple()>").askForLocalAtStmt(14, "tail",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void queryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(34, "head",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void tailQueryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(18, "tail",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}	@Test
	public void subQueryOfheadQueryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(17, "tail",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void complex2QueryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex2()>").askForLocalAtStmt(29, "$r5",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	
	
	@Test
	public void headQueryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(35, "$r13",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void tailQuery2Test() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(18, "head",
						andExpectNonEmpty()));
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(18, "tail",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void headQueryTest4() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(29, "$r9",
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
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(25, "$r6",
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
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(25, "$r5",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	public void headQueryTest1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(25, "$r4",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
public void recQueryTest() {
	runAnalysis(false, new IQueryHandler() {
		@Override
		public ArrayList<MethodQueries> queryAndResults() {
			ArrayList<MethodQueries> res = new ArrayList<>();
			res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(17, "tail",
					andExpectNonEmpty()));

			
			return res;
		}
	});
}
	@Test
	public void queryTest1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(34, "$r14",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void queryTest2() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void complex()>").askForLocalAtStmt(41, "v1",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Test
	public void simpleTest2() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ComplexNextListTarget: void simple()>").askForLocalAtStmt(16, "g",
						andExpectNonEmpty()));

				
				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.ComplexNextListTarget";
	}
}
