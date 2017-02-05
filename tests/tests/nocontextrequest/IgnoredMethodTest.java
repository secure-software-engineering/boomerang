package tests.nocontextrequest;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class IgnoredMethodTest extends AliasTest {
  @Test
  public void equalsTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.IgnoredMethodTarget: void equalsTest1()>");
        query.addQuery(new Query(11, "a2", new ResultObject(2, "a2", "a1", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        MethodQueries query2 = new MethodQueries("<cases.IgnoredMethodTarget: void equalsTest1()>");
        query.addQuery(new Query(11, "b2", new ResultObject(5, "b2", "b1", "$r1")));
        res.add(query2);
        return res;
      }
    });
  }

  @Test
  public void equalsTest2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.IgnoredMethodTarget: void equalsTest2()>");
        query.addQuery(new Query(11, "a2", new ResultObject(3, "a1[d]", "a2", "$r0[d]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        MethodQueries query2 = new MethodQueries("<cases.IgnoredMethodTarget: void equalsTest2()>");
        query.addQuery(new Query(11, "b2", new ResultObject(6, "b2", "b1[f]", "$r1[f]")));
        res.add(query2);
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.IgnoredMethodTarget";
  }
}
