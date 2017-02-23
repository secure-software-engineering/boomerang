package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class NonDistributiveTest extends AliasTest {

  @Test
  public void test1() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.NonDistributive: void outer()>").askForLocalAtStmt(11, "x",
            andExpect(7, "x", "b1[f]", "b2[f]", "$r0[f]", "$r1")));

        return res;
      }
    });
  }

  @Test
  public void subQueryTest1() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.NonDistributive: void outer()>").askForLocalAtStmt(10, "b2",
            andExpect(3, "b2", "b1", "$r0")));

        return res;
      }
    });
  }

  @Test
  public void subQuery2Test1() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        // res.add(inMethod(
        // "<cases.NonDistributive$B: void <init>(cases.NonDistributive,cases.NonDistributive$B)>")
        // .askForLocalAtStmt(6, "l0", andExpect(1, "l0")));
        res.add(inMethod(
            "<cases.NonDistributive$B: void <init>(cases.NonDistributive,cases.NonDistributive$B)>")
            .askForLocalAtStmt(6, "l0[f]", andExpect(5, "l0[f]")));
        res.add(inMethod("<cases.NonDistributive: void outer()>").askForLocalAtStmt(10, "b2",
            andExpect(3, "b2", "b1", "$r0")));

        return res;
      }
    });
  }

  @Test
  public void subQuery3Test1() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        // res.add(inMethod(
        // "<cases.NonDistributive$B: void <init>(cases.NonDistributive,cases.NonDistributive$B)>")
        // .askForLocalAtStmt(6, "l0", andExpect(1, "l0")));
        res.add(inMethod("<cases.NonDistributive: void outer()>").askForLocalAtStmt(5, "$r0[f]",
            andExpect(4, "$r0[f]")));
        res.add(inMethod("<cases.NonDistributive: void outer()>").askForLocalAtStmt(10, "b2",
            andExpect(3, "b2", "b1", "$r0")));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    // TODO Auto-generated method stub
    return "cases.NonDistributive";
  }
}
