package tests.contextrequestor;

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
public class SCCThisTest extends AliasTest {
  @Test
  public void sccTest1() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.Loop1: void loop()>").askForLocalAtStmt(
            8,
            "x",
            andExpect("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>", 8, "x",
                "$r0[d]", "this[a,d]")));

        return res;
      }
    });
  }

  @Test
  @Ignore
  public void subQueryOfSccTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>")
            .askForLocalAtStmt(
                15,
                "c[<contextrequestor.Loop1: cases.A a>,d]",
                andExpect(8, "c[<contextrequestor.Loop1: cases.A a>,d]",
                    "$r2[<contextrequestor.Loop1: cases.A a>,d]")));

        return res;
      }
    });
  }

  @Test
  @Ignore
  public void subQueryOfSccTest1a() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>")
            .askForLocalAtStmt(15, "c", andExpect(7, "c", "$r2"), andExpect(11, "c", "$r1")));

        res.add(inMethod("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>")
            .askForLocalAtStmt(
                15,
                "c[<contextrequestor.Loop1: cases.A a>,d]",
                andExpect(8, "c[<contextrequestor.Loop1: cases.A a>,d]",
                    "$r2[<contextrequestor.Loop1: cases.A a>,d]")));

        return res;
      }
    });
  }

  @Test
  public void sccTest2() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.Loop2: void loop()>").askForLocalAtStmt(
            8,
            "h",
            andExpectNonEmpty()));


        return res;
      }
    });
  }

  @Test
  @Ignore
  public void subQueryOfSccTest2() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>")
            .askForLocalAtStmt(14, "c", andExpect(7, "c", "$r2"), andExpect(11, "c", "$r1")));
        res.add(inMethod("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>")
            .askForLocalAtStmt(
                14,
                "c[<contextrequestor.Loop2: cases.A d>,f]",
                andExpect(12, "$r1[<contextrequestor.Loop2: cases.A d>,f]",
                    "c[<contextrequestor.Loop2: cases.A d>,f]")));


        return res;
      }
    });
  }

  @Test
  @Ignore
  public void sccTest3DoubleQuery() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.Loop2: void loop()>").askForLocalAtStmt(
            8,
            "h",
            andExpect("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>", 13, "h",
                "$r0[f]", "this[d,f]")));


        res.add(inMethod("<contextrequestor.Loop1: void loop()>").askForLocalAtStmt(
            8,
            "x",
            andExpect("<contextrequestor.SCCThisTarget: void main(java.lang.String[])>", 9, "x",
                "$r0[d]", "this[a,d]")));


        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    // TODO Auto-generated method stub
    return "contextrequestor.SCCThisTarget";
  }
}
