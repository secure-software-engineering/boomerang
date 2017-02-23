package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectEmpty;
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
public class ThreeLevelFieldTest extends AliasTest {
  @Test
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void test()>");
        query.addQuery(new Query(13, "h", new ResultObject(7, "h", "$r2[l3]", "x[l2,l3]",
            "$r1[l2,l3]", "a", "$r3[l3]", "l[l1,l2,l3]", "$r0[l1,l2,l3]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }


  @Test
  public void subQueryOfTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void test()>");
        query.addQuery(new Query(7, "l[l1]", new ResultObject(4, "x", "$r0[l1]", "l[l1]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(5,
            "$r0[l1,l2]", andExpect(4, "$r0[l1,l2]")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(5,
            "$r0[l1]", andExpect(4, "$r0[l1]")));
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
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(5,
            "$r0[l1]", andExpect(4, "$r0[l1]")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(5,
            "$r0[l1,l2]", andExpect(4, "$r0[l1,l2]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget$Level2: void <init>(cases.ThreeLevelFieldTarget)>")
            .askForLocalAtStmt(8, "this", andExpect(1, "this")));
        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget$Level1: void <init>(cases.ThreeLevelFieldTarget)>")
            .askForLocalAtStmt(7, "$r0", andExpect(6, "$r0")));
        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget$Level1: void <init>(cases.ThreeLevelFieldTarget)>")
            .askForLocalAtStmt(8, "this", andExpect(1, "this")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(4, "$r0",
            andExpect(3, "$r0")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(4,
            "$r0[l1]", andExpectEmpty()));

        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(9, "l", andExpect(1, "l")));
        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(9, "l[l1]", andExpect(1, "l[l1]", "$r0")));


        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(9, "l[l1,l2]", andExpect(1, "l[l1,l2]", "$r0[l2]", "$r2")));
        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(9, "$r0[l2]", andExpect(1, "l[l1,l2]", "$r0[l2]", "$r2")));


        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(8, "$r2", andExpect(1, "l[l1,l2]", "$r0[l2]", "$r2")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(6, "l",
            andExpect(3, "l", "$r0")));
        res.add(inMethod("<cases.ThreeLevelFieldTarget: void test()>").askForLocalAtStmt(6,
            "l[l1]", andExpect(4, "l[l1]", "$r0[l1]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest2a() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();


        res.add(inMethod(
            "<cases.ThreeLevelFieldTarget: void getField(cases.ThreeLevelFieldTarget$Level1)>")
            .askForLocalAtStmt(9, "l[l1,l2]", andExpect(1, "l[l1,l2]", "$r0[l2]", "$r2")));
        return res;
      }
    });
  }

  @Test
  public void test2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void test2()>");
        query.addQuery(new Query(9, "x", new ResultObject(6, "level1[l1,left]", "x", "$r1[left]",
            "$r0[l1,left]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void test3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void test3()>");
        query.addQuery(new Query(10, "x", andExpectEmpty()));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  @Ignore
  public void subQuery2OfTest3() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void test3()>");
        query.addQuery(new Query(7, "$r0[l1]", andExpectEmpty()));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }


  @Ignore
  @Test
  public void loop1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.ThreeLevelFieldTarget: void loop()>");
        query.addQuery(new Query(12, "x", new ResultObject(9, "$r0[l1,left]", "level1[l1,left]",
            "$r2[left]", "x", "$r1[left]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.ThreeLevelFieldTarget";
  }
}
