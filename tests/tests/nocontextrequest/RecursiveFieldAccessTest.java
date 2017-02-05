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

@SuppressWarnings("rawtypes")
public class RecursiveFieldAccessTest extends AliasTest {

  @Test
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.RecursiveFieldAccess: void test1()>").askForLocalAtStmt(
            17,
            "x",
            andExpectNonEmpty()));

        return res;
      }
    });
  }


  @Test
  public void test2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.RecursiveFieldAccess: void test2(int)>").askForLocalAtStmt(
            19,
            "x",
            andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Test
  public void separatedTreeTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.RecursiveFieldAccess: cases.A separatedTreeTest()>")
            .askForLocalAtStmt(
                28,
                "x",
                andExpect(21, "$r4[child,right,data]", "$r3[child,right,data]", "$r10[data]",
                    "$r6[right,data]", "$r9", "$r13[data]", "$r7[child,right,data]", "$r5[data]",
                    "$r1[child,right,data]", "myTree[left,child,right,data]", "$r8[right,data]",
                    "$r11[child,right,data]", "$r2[right,data]", "x", "$r0[left,child,right,data]",
                    "$r12[right,data]")));

        return res;
      }
    });
  }

  @Ignore
  @Test
  public void test4() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.RecursiveFieldAccess: void test4()>").askForLocalAtStmt(10, "x",
            andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Test
  public void test5() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.RecursiveFieldAccess: void test5()>").askForLocalAtStmt(13, "h",
            andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.RecursiveFieldAccess";
  }
}
