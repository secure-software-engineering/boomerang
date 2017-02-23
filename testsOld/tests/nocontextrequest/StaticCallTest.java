package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class StaticCallTest extends AliasTest {
  @Test
  public void staticCallTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.StaticCallTarget: void method1()>").askForLocalAtStmt(4, "x",
            andExpect(2, "x", "c[c]")));

        return res;
      }
    });
  }

  @Test
  public void staticCallTest2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.StaticCallTarget: void method1()>").askForLocalAtStmt(3, "c[c]",
            andExpect(2, "c[c]")));
        res.add(inMethod("<cases.StaticCallTarget: void method1()>").askForLocalAtStmt(3, "c",
            andExpect(2, "c")));


        return res;
      }
    });
  }

  @Test
  public void staticCallTest3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.StaticCallTarget: cases.A staticallyGetA()>").askForLocalAtStmt(9,
            "a[c]", andExpect(2, "a[c]", "$r1[c]", "b", "$r0")));
        res.add(inMethod("<cases.StaticCallTarget: cases.A staticallyGetA()>").askForLocalAtStmt(9,
            "a", andExpect(5, "a", "$r1")));


        return res;
      }
    });
  }

  @Test
  public void staticCallTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.StaticCallTarget: void method1()>").askForLocalAtStmt(3, "c",
            andExpect(2, "c")));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.StaticCallTarget";
  }

}
