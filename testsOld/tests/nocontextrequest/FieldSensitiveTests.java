package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class FieldSensitiveTests extends AliasTest {

	@Test
	public void test1() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void test1()>");
				query.addQuery(new Query(12, "k", new ResultObject(5, "$r1", "k", "$r0[c]", "$r0[d]", "e[c]", "e[d]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	public void test1a() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void test1a()>");
				query.addQuery(
						new Query(13, "k", new ResultObject(5, "$r1", "k", "b", "$r0[c]", "$r0[d]", "e[c]", "e[d]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void subtestOfTest1() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries(
						"<cases.FieldSensitiveTarget: cases.A alias(cases.A,cases.A,cases.A)>");
				query.addQuery(new Query(6, "y", new ResultObject(1, "y", "a")));
				query.addQuery(new Query(7, "y", new ResultObject(1, "y", "a")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void subtest2OfTest1() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries(
						"<cases.FieldSensitiveTarget: cases.A alias(cases.A,cases.A,cases.A)>");
				query.addQuery(new Query(7, "y", new ResultObject(1, "y", "a")));
				query.addQuery(new Query(6, "y", new ResultObject(1, "y", "a")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void staticFieldTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {

				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void staticFieldTest()>").askForLocalAtStmt(7, "b",
						andExpect(3, "b", "$r0", "STATIC[<cases.FieldSensitiveTarget: cases.A a>]")));
				return res;
			}
		});
	}

	@Test
	public void doubleQueryTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {

				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void doubleQuery()>").askForLocalAtStmt(5, "c",
						andExpect(2, "c", "$r0")));
				res.add(inMethod("<cases.FieldSensitiveTarget: void doubleQuery()>").askForLocalAtStmt(5, "c[a,d]",
						andExpect(3, "c[a,d]", "$r0[a,d]")));
				return res;
			}
		});
	}

	@Test
	@Ignore
	public void fieldNullAndThenWrite() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldNullAndThenWrite()>");
				query.addQuery(new Query(9, "a[b]", new ResultObject(6, "e[b]", "$r1", "$r0[b]", "a[b]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void fieldWriteRecursion() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldWriteRecursion()>");
				query.addQuery(new Query(11, "b", new ResultObject(2, "a[c]", "a[c,c]", "b[c,c]", "a", "b[c]", "b",
						"$r0", "$r0[c]", "$r0[c,c]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void fieldReadRecursion() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldReadRecursion()>");
				query.addQuery(new Query(11, "b",
						new ResultObject(3, "b", "b[c]", "a[c]", "a[c,c]", "$r0[c,c]", "b[c,c]", "$r0[c]", "a"),
						new ResultObject(2, "a", "$r0", "b")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void thisAliasTest() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void thisAliasTest()>");
				query.addQuery(new Query(8, "a",
						new ResultObject(1, "a", "p[t]", "$r0[t]", "this",
								"$r0[<cases.FieldSensitiveTarget$P: cases.FieldSensitiveTarget this$0>]",
								"p[<cases.FieldSensitiveTarget$P: cases.FieldSensitiveTarget this$0>]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void thisAliasTest2() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void thisAliasTest2()>");
				query.addQuery(new Query(8, "a",
						new ResultObject(1, "a", "p[t]", "$r0[t]", "this",
								"$r0[<cases.FieldSensitiveTarget$P: cases.FieldSensitiveTarget this$0>]",
								"p[<cases.FieldSensitiveTarget$P: cases.FieldSensitiveTarget this$0>]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void paramAlias() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void paramAlias()>");
				query.addQuery(new Query(16, "x",
						andExpectNonEmpty()/*
											 * new ResultObject(10, "b[e]",
											 * "$r3[e]", "y", "x", "$r2[e]",
											 * "$r0[b,e]", "$r1[e]", "a[b,e]")
											 */));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void fieldReadRecursion2() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldReadRecursion()>");
				query.addQuery(new Query(11, "a", andExpectNonEmpty()));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 10000)
	public void fieldSensitiveFieldReadAndWriteWithLoop() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries(
						"<cases.FieldSensitiveTarget: void fieldSensitiveFieldReadAndWriteWithLoop()>");
				query.addQuery(new Query(16, "x", andExpectNonEmpty()));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 3000000)
	public void test2() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void test2()>");
				query.addQuery(new Query(15, "k",
						new ResultObject(9, "$r0[d]", "k", "$r0[c]", "e[c]", "f[c]", "e[d]", "f[d]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 3000000)
	public void doubleReadSameFieldFromConstructor() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries(
						"<cases.FieldSensitiveTarget: void doubleReadSameFieldFromConstructor()>");
				query.addQuery(new Query(7, "b", andExpectNonEmpty()));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 3000000)
	public void simpleTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void simpleTest()>");
				query.addQuery(new Query(9, "b", new ResultObject(5, "b", "a[f]", "$r1", "$r0[f]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 3000000)
	public void simpleTest2() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void simpleTest()>");
				query.addQuery(new Query(9, "b", new ResultObject(5, "b", "a[f]", "$r1", "$r0[f]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void simpleTest3() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void simpleTest3()>");
				query.addQuery(new Query(6, "b", new ResultObject(3, "b", "a[f]", "$r0[f]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 3000000)
	public void fieldWrite() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldWrite()>");
				query.addQuery(new Query(11, "x", new ResultObject(6, "b[f]", "a[f]", "$r1", "$r0[f]", "h", "x")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 1000000)
	public void doubleReadSameField() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void doubleReadSameField()>").askForLocalAtStmt(11, "b",
						andExpectNonEmpty()));

				return res;
			}
		});
	}

	@Test
	public void fieldSensitiveFieldReadTest() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldSensitiveFieldRead()>");
				query.addQuery(new Query(10, "b", new ResultObject(5, "b", "a[f]", "c[f]", "$r0", "$r1[f]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test(timeout = 10000)
	public void fieldReadRecTest() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void fieldReadRec()>");
				query.addQuery(new Query(18, "p", andExpectNonEmpty()));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void askForSameBaseTwiceTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.FieldSensitiveTarget: void askForSameBaseTwice()>");
				query.addQuery(
						new Query(12, "e", new ResultObject(5, "e", "f", "$r1", "d[c]", "d[d]", "$r0[d]", "$r0[c]")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}

	@Test
	public void longerAccessPathTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void longerAccessPath()>").askForLocalAtStmt(13, "c",
						andExpect(9, "c", "b[f]", "$r1[f]", "$r2", "a[f,f]", "$r0[f,f]")));

				return res;
			}
		});
	}

	@Test
	public void longerAccessPathNotSameAllocTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void longerAccessNotSameAlloc()>").askForLocalAtStmt(14,
						"c", andExpect(10, "c", "$r2", "$r1[f]", "a[f,f]", "$r0[f,f]", "b[f]")));

				return res;
			}
		});
	}

	@Test
	public void longerAccessPathForNullTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void longerAccessPath()>").askForLocalAtStmt(13, "c[c]",
						andExpect(10, "c[c]", "b[f,c]", "$r1[f,c]", "$r2[c]", "a[f,f,c]", "$r0[f,f,c]")));

				return res;
			}
		});
	}

	@Test
	public void fieldWriteTestWithinMethodTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldSensitiveTarget: void fieldWriteTestWithinMethod()>")
						.askForLocalAtStmt(11, "c", andExpect(10, "c")));

				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		return "cases.FieldSensitiveTarget";
	}
}
