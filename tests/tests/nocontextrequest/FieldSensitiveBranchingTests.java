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
public class FieldSensitiveBranchingTests extends AliasTest {

  @Test(timeout = 100000)
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void branching1()>")
            .askForLocalAtStmt(15, "c", andExpect(7, "c", "a[d]", "$r0[d]", "$r2(cases.A)"),
 andExpect(11,
 "c", "a[d]", "$r0[d]", "$r1(cases.A)")));

        return res;
      }
    });
  }

  @Test(timeout = 10000)
  public void branching() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method(cases.A,cases.A)>")
            .askForLocalAtStmt(11, "x", andExpect(1, "x", "b", "a[f]", "a[c]")));

        return res;
      }
    });
  }

  @Test(timeout = 10000)
  public void branching2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method2(cases.A,cases.A)>")
            .askForLocalAtStmt(
                14,
                "x",
                andExpect(1, "x", "b", "a[c]", "a[f]", "a[d,c]", "$r2[f]", "$r0[f]", "$r2[c]",
                    "$r1[f]", "$r0[c]", "$r1[c]", "a[d,f]")));

        return res;
      }
    });
  }

  @Test(timeout = 10000)
  public void subTestOfBranching2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method2(cases.A,cases.A)>")
            .askForLocalAtStmt(12, "a", andExpect(1, "$r0", "$r1", "$r2", "a[d]", "a")));

        return res;
      }
    });
  }

  @Test
  public void branching5() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method5(cases.A,cases.A)>")
            .askForLocalAtStmt(11, "a", andExpect(1, "$r0", "$r1", "a[d]", "a")));

        return res;
      }
    });
  }

  @Test(timeout = 5000)
  public void branching3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method3(cases.A,cases.A)>")
            .askForLocalAtStmt(14, "x",
                andExpect(1, "x", "b", "a[c]", "a[f]", "b[d]", "a[c,d]", "x[d]", "a[f,d]")));

        return res;
      }
    });
  }

  @Test
  public void branching4() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void method4(cases.A,cases.A)>")
            .askForLocalAtStmt(11, "x", andExpect(1, "x", "a[d]", "x[d]", "b[d]", "b", "a[d,d]")));

        return res;
      }
    });
  }

  @Test(timeout = 100000)
  public void test2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void branching1()>")
            .askForLocalAtStmt(16, "y", andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Ignore
  @Test(timeout = 100000)
  public void test3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void fieldRead()>")
            .askForLocalAtStmt(11, "y", andExpect(2, "y")));

        return res;
      }
    });
  }

  @Test(timeout = 1000000)
  public void problematicTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void problematicTest()>")
            .askForLocalAtStmt(7, "y", andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Test(timeout = 1000000)
  public void recursiveTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.FieldSensitiveBranchingTarget: void recursiveTest()>")
            .askForLocalAtStmt(7, "y", andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.FieldSensitiveBranchingTarget";
  }
}
