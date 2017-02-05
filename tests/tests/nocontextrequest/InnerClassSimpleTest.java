package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class InnerClassSimpleTest extends AliasTest {

  @Test
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassSimpleTarget: void test1()>").askForLocalAtStmt(8, "t",
            andExpect(3, "string", "a[this$0,g]", "t", "i[g]", "$r0[g]")));
        return res;
      }
    });
  }

  @Test
  public void test1a() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassSimpleTarget: void test1a()>").askForLocalAtStmt(6, "t",
            andExpect(1, "string", "a[this$0,g]", "t", "this[g]")));
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.InnerClassSimpleTarget";
  }
}
