package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class ReverseTest extends AliasTest {
  @Test
  public void testWhereWeMeetWithSameQuery() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(12, "a[b]",
            andExpect(6, "a[b]", "$r1", "$r0[b]")));
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(14, "x",
            andExpect(6, "a[b]", "$r1", "$r0[b]", "x", "c[b]", "$r2[b]")));

        return res;
      }
    });
  }

  @Test
  public void testWhereWeMeetNotWithSameQuery() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(12, "$r1",
            andExpect(6, "a[b]", "$r1", "$r0[b]")));
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(14, "x",
            andExpect(6, "a[b]", "$r1", "$r0[b]", "x", "c[b]", "$r2[b]")));

        return res;
      }
    });
  }

  @Test
  public void meetingPointAtReturnWithAllocInside() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void foo(cases.A)>").askForLocalAtStmt(7, "a[b]",
            andExpect(4, "a[b]", "$r0")));
        res.add(inMethod("<cases.ReverseTarget: void meetingPointAtReturnWithAllocInside()>")
            .askForLocalAtStmt(8, "x", andExpect(6, "a[b]", "$r0[b]", "x")));

        return res;
      }
    });
  }

  @Test
  public void subQueryOfmeetingPointAtReturnWithAllocInside() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void meetingPointAtReturnWithAllocInside()>")
            .askForLocalAtStmt(8, "x", andExpect(6, "a[b]", "$r0[b]", "x")));

        return res;
      }
    });
  }

  @Test
  public void meetingPointAtReturnWithAllocOutside() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void id(cases.A)>").askForLocalAtStmt(4, "a[b]",
            andExpect(1, "a[b]")));
        res.add(inMethod("<cases.ReverseTarget: void meetingPointAtReturnWithAllocOutside()>")
            .askForLocalAtStmt(11, "x", andExpect(6, "a[b]", "x", "$r0[b]", "$r1")));

        return res;
      }
    });
  }

  @Test
  public void subtest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void meetingPointAtReturnWithAllocOutside()>")
            .askForLocalAtStmt(11, "x", andExpect(6, "a[b]", "x", "$r0[b]", "$r1")));

        return res;
      }
    });
  }

  @Test
  public void testWithinMethodAtReturnQuery() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void foo(cases.A,cases.A)>").askForLocalAtStmt(7,
            "c[b]", andExpect(1, "a[b]", "$r0", "c[b]")));
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(14, "x",
            andExpect(6, "a[b]", "$r1", "$r0[b]", "x", "c[b]", "$r2[b]")));

        return res;
      }
    });
  }


  @Test
  public void testWithinMethodAlmostAtReturnQuery() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void foo(cases.A,cases.A)>").askForLocalAtStmt(6,
            "$r0", andExpect(1, "a[b]", "$r0")));
        res.add(inMethod("<cases.ReverseTarget: void test1()>").askForLocalAtStmt(14, "x",
            andExpect(6, "a[b]", "$r1", "$r0[b]", "x", "c[b]", "$r2[b]")));

        return res;
      }
    });
  }

  @Test
  public void paramFieldTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void paramFieldTest(cases.A)>").askForLocalAtStmt(
            5, "a2[b]", andExpect(1, "a2[b]", "x")));
        return res;
      }
    });
  }


  @Test
  public void paramAndAfterwardsFieldTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void paramFieldTest(cases.A)>").askForLocalAtStmt(
            5, "a2", andExpect(1, "a2")));
        res.add(inMethod("<cases.ReverseTarget: void paramFieldTest(cases.A)>").askForLocalAtStmt(
            5, "a2[b]", andExpect(1, "a2[b]", "x")));
        return res;
      }
    });
  }

  @Test
  public void onlyOnObjectLevel1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        // res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(5, "$r0",
        // andExpect(3, "$r0")));
        res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(6, "$r0",
            andExpect(3, "$r0", "a")));
        return res;
      }
    });
  }

  @Test
  public void onlyOnObjectLevel2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(5, "$r0",
            andExpect(3, "$r0")));
        res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(7, "$r0",
            andExpect(3, "$r0", "a", "b")));
        return res;
      }
    });
  }

  @Test
  public void onlyOnObjectLevel3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(6, "$r0",
            andExpect(3, "$r0", "a")));
        res.add(inMethod("<cases.ReverseTarget: void test2()>").askForLocalAtStmt(7, "$r0",
            andExpect(3, "$r0", "a", "b")));
        return res;
      }
    });
  }

  @Test
  public void onlyOnObjectLevelBranched() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(11, "b",
            andExpect(3, "$r0", "a", "b")));
        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(13, "c",
            andExpect(3, "$r0", "a", "c")));

        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(15, "c",
            andExpect(3, "$r0", "a", "c")));
        return res;
      }
    });
  }

  @Test
  public void onlyOnObjectLevelBranchedAPA() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(11, "b",
            andExpect(3, "$r0", "a", "b")));
        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(13, "c",
            andExpect(3, "$r0", "a", "c")));

        res.add(inMethod("<cases.ReverseTarget: void test2branched()>").askForLocalAtStmt(15, "c",
            andExpect(3, "$r0", "a", "c")));
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    // TODO Auto-generated method stub
    return "cases.ReverseTarget";
  }

}
