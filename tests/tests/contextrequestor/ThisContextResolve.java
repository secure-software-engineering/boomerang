package tests.contextrequestor;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class ThisContextResolve extends AliasTest {
  @Test
  public void reqContextTest1() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.ThisContextResolve$B: void foo()>")
            .askForLocalAtStmt(
                4,
                "c",
                andExpect("<contextrequestor.ThisContextResolve: void test1()>", 4, "c",
                    "this[field]")));

        return res;
      }
    });
  }

  @Test
  public void recursiveContextTest() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void foo(contextrequestor.ThisContextResolve$A,int)>")
                .askForLocalAtStmt(12, "c", andExpectNonEmpty()));

        return res;
      }
    });
  }

  @Test
  public void subQueryOfBaseAlias() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<contextrequestor.ThisContextResolve: void baseAlias()>")
            .askForLocalAtStmt(11, "$r0[f,g]",
 andExpectNonEmpty()));



        return res;
      }
    });
  }

  @Test
  public void baseAlias() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level1(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "h",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));

        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level1(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "b[h,g]",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));


        return res;
      }
    });
  }

  @Test
  public void baseAlias3() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level2(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "h",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));

        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level1(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "h",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));

        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level1(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "b[h,g]",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));


        return res;
      }
    });
  }

  @Test
  public void baseAlias2() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ThisContextResolve: void level2(contextrequestor.ThisContextResolve$Inner1,contextrequestor.ThisContextResolve$Inner2)>")
            .askForLocalAtStmt(
                7,
                "h",
                andExpect("<contextrequestor.ThisContextResolve: void baseAlias()>", 7, "$r0[g]",
                    "b[h,g]", "a[f,g]", "h")));

        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "contextrequestor.ThisContextResolve";
  }
}
