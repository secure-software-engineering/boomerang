package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class BackwardEnterMethodTest extends AliasTest {
  @Test
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test1()>").askForLocalAtStmt(
            12, "x", andExpect(9, "x", "h[c]", "$r1[c]", "$r0[b,c]", "a[b,c]")));

        return res;
      }
    });
  }

  @Test
  public void test4() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test4()>").askForLocalAtStmt(
            12, "x", andExpect(9, "x", "h[c]", "$r1[c]", "$r0[b,c]", "a[b,c]")));

        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest4a() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();

        res.add(inMethod(
            "<cases.BackwardEnterMethodAliasTarget: void wrapped(cases.BackwardEnterMethodAliasTarget$A)>")
            .askForLocalAtStmt(5, "a[b,c]", andExpect(4, "a[b,c]"), andExpect(1, "a[b,c]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest4() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<cases.BackwardEnterMethodAliasTarget: void wrapped(cases.BackwardEnterMethodAliasTarget$A)>")
            .askForLocalAtStmt(5, "a[b]", andExpect(1, "a[b]")));

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
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test5()>").askForLocalAtStmt(
            8, "h", andExpect(6, "h", "a[b]", "$r0[b]")));

        return res;
      }
    });
  }

  @Test
  public void test6() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test6()>").askForLocalAtStmt(
            8, "e", andExpect(6, "e", "a[b]", "$r0[b]")));

        return res;
      }
    });
  }

  @Test
  @Ignore
  public void test7() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test7()>").askForLocalAtStmt(
            15, "h", andExpect(12, "h")));

        return res;
      }
    });
  }

  @Test
  public void subtest1OfTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<cases.BackwardEnterMethodAliasTarget: void foo(cases.BackwardEnterMethodAliasTarget$A)>")
            .askForLocalAtStmt(8, "a[b]", andExpect(1, "a[b]", "e")));

        return res;
      }
    });
  }

  @Test
  public void subtest2OfTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<cases.BackwardEnterMethodAliasTarget: void foo(cases.BackwardEnterMethodAliasTarget$A)>")
            .askForLocalAtStmt(4, "a", andExpect(1, "a")));

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
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test2()>").askForLocalAtStmt(
            16, "x", andExpect(10, "x", "h[c]", "$r2", "$r1[c]", "$r0[b,c]", "a[b,c]", "e[c]")));

        return res;
      }
    });
  }

  @Test
  public void test3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void test3()>").askForLocalAtStmt(
            15, "x", andExpect(9, "x", "b[c]", "h[c]", "$r2", "$r1[c]", "$r0[b,c]", "a[b,c]")));

        return res;
      }
    });
  }

  @Ignore
  @Test
  public void loop() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.BackwardEnterMethodAliasTarget: void loop()>").askForLocalAtStmt(
            12, "x", andExpect(9, "x", "a[b,c]", "$r1[c]", "$r0[b,c]", "$r2[c]")));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.BackwardEnterMethodAliasTarget";
  }
}
