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
public class TwoCallerFieldTest extends AliasTest {

  MethodQueries query1 = inMethod("<cases.TwoCallerFieldTarget: void outer1()>").askForLocalAtStmt(
      11, "query1", andExpect(7, "query1", "param3[f]", "$r1[f]")).askForLocalAtStmt(12, "query2",
      andExpect(2, "query2", "param2", "param1", "$r0"));
  MethodQueries query2 = inMethod("<cases.TwoCallerFieldTarget: void outer2()>")
      .askForLocalAtStmt(
          12,
          "query1",
          andExpect(7, "query1", "$r1[f]", "$r0[f]", "param1[f]", "param2[f]", "param3[f]",
 "query2[f]"), andExpect(3, "$r0[f]", "param3[f]", "query1"));

  @Test
  public void twoCallerTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query1);
        res.add(query2);
        return res;
      }
    });
  }

  @Test
  public void twoCallerDifferentOrderTest() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query2);
        res.add(query1);
        return res;
      }
    });
  }

  @Test
  public void singleQuery2() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query2);
        return res;
      }
    });
  }

  @Test
  public void singleQuery1() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query1);
        return res;
      }
    });
  }

  @Test
  @Ignore
  public void twoCallerInnerFirstTest() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.TwoCallerFieldTarget: void inner(cases.A,cases.A,cases.A)>")
            .askForLocalAtStmt(
                8,
                "innerParam1",
                andExpect("<cases.TwoCallerFieldTarget: void outer1()>", 2, "innerParam1",
                    "innerParam2", "a", "b"),
                andExpect("<cases.TwoCallerFieldTarget: void outer2()>", 6, "innerParam1", "a"))
            .askForLocalAtStmt(
                8,
                "innerParam2",
                andExpect("<cases.TwoCallerFieldTarget: void outer1()>", 2, "innerParam1",
                    "innerParam2", "a", "b"),
                andExpect("<cases.TwoCallerFieldTarget: void outer2()>", 2, "innerParam3",
                    "innerParam2", "b", "c"))
            .askForLocalAtStmt(
                8,
                "innerParam3",
                andExpect("<cases.TwoCallerFieldTarget: void outer2()>", 2, "innerParam3",
                    "innerParam2", "c", "b"),
                andExpect("<cases.TwoCallerFieldTarget: void outer1()>", 6, "innerParam3", "c")));
        res.add(inMethod("<cases.TwoCallerFieldTarget: void outer1()>").askForLocalAtStmt(12,
            "query1", andExpect(7, "query1", "param3[f]", "$r1[f]")).askForLocalAtStmt(12,
            "query2", andExpect(2, "query2", "param2", "param1", "$r0")));


        res.add(inMethod("<cases.TwoCallerFieldTarget: void outer2()>").askForLocalAtStmt(
            12,
            "query1",
            andExpect(7, "query1", "param3[f]", "param1[f]", "query2[f]", "$r1[f]", "param2[f]",
                "$r0[f]")).askForLocalAtStmt(12, "query2", andExpect(6, "query2", "param1", "$r1")));



        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.TwoCallerFieldTarget";
  }
}
