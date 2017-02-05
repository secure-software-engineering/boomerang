package tests.contextrequestor;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class ContextResolveTest extends AliasTest {
  @Test
  public void reqContextTest1() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ContextResolve: void foo(contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A)>")
            .askForLocalAtStmt(
                12,
                "x",
                andExpect(7, "x", "$r2[c,s]", "a1[b,c,s]", "$r0[s]", "$r1[c,s]", "$r3[s]",
                    "a2[b,c,s]")));

        return res;
      }
    });
  }

  @Test
  public void reqContextTest2() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ContextResolve: void fooRedefine(contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A)>")
            .askForLocalAtStmt(9, "x", andExpect(5, "$r0", "a2[b]", "a1[b]", "x")));
        // andExpect("<contextrequestor.ContextResolve: void test2()>",4,"$r0", "a2[b]", "a1[b]",
        // "x")));

        return res;
      }
    });
  }

  @Test
  public void reqContextTest2Null() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ContextResolve: void fooRedefineNull(contextrequestor.ContextResolve$A1,contextrequestor.ContextResolve$A1)>")
            .askForLocalAtStmt(9, "x", andExpect(5, "$r0", "a2[b]", "a1[b]", "x")));
        // andExpect("<contextrequestor.ContextResolve: void test2()>",4,"$r0", "a2[b]", "a1[b]",
        // "x")));

        return res;
      }
    });
  }


  @Test
  public void reqContextTest3() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ContextResolve: void level2(contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A)>")
            .askForLocalAtStmt(10, "x", andExpect(6, "c[b]", "a[b]", "b[b]", "x", "$r0")));

        return res;
      }
    });
  }

  @Test
  public void reqContextTest4() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<contextrequestor.ContextResolve: void level2test4(contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A)>")
            .askForLocalAtStmt(
                7,
                "x",
                andExpect(
                    "<contextrequestor.ContextResolve: void level1test4(contextrequestor.ContextResolve$A,contextrequestor.ContextResolve$A)>",
                    6, "c[b]", "a[b]", "b[b]", "x")));

        return res;
      }
    });
  }

  @Test
  public void test5() {
    runAnalysis(true, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries(
                "<contextrequestor.ContextResolve: void inner(contextrequestor.ContextResolve$X)>");
        query
            .addQuery(new Query(5, "h", new ResultObject(
                "<contextrequestor.ContextResolve: void test5(int)>", 10, "x[a]", "h"),
                new ResultObject("<contextrequestor.ContextResolve: void test5(int)>", 6, "x[a]",
                    "h")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "contextrequestor.ContextResolve";
  }
}
