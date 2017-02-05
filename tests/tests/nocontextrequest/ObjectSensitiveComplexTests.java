package tests.nocontextrequest;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class ObjectSensitiveComplexTests extends AliasTest {
	@Test
	public void unbalancedTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void unbalanced()>");
				query.addQuery(new Query(12,  "f", new ResultObject(5,"e", "d", "f", "h"),
						new ResultObject(9,"e", "l6", "f")
						));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	@Test
	public void multipleQueriesTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void multipleQueries()>");
				query.addQuery(new Query(8,  "c", new ResultObject(2,"a", "b","c")));
				query.addQuery(new Query(8,  "z", new ResultObject(3,"x", "y","z")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	@Test
	public void multipleCallsETest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void multipleCalls()>");
				query.addQuery(new Query(5,  "e", new ResultObject(2,"e")
						));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	@Test
	public void multipleCallsBranchedTest() {
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void multipleCalls()>");
				query.addQuery(new Query(5,  "f", new ResultObject(3,"f")
						));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	
	@Test
	public void doubleCallStackTest() {

		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void doubleCallStack()>");
				query.addQuery(new Query(3,  "alias", new ResultObject(2,"alias")
						));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	@Test
	public void recursionTest() {


		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ObjectSensitiveComplex: void recursion()>");
				query.addQuery(new Query(6,  "alias", new ResultObject(5,"alias")
						));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.ObjectSensitiveComplex";
	}
}
