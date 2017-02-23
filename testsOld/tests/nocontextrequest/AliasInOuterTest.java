package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
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
public class AliasInOuterTest extends AliasTest {

  @Test
  public void simple() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void test1()>").askForLocalAtStmt(11, "x",
            andExpect(7, "x", "d[f]", "$r0[f]", "a[f]", "$r1[f]")));
        return res;
      }
    });
  }

  @Test
  public void wrappedAlloc() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void wrappedAlloc()>").askForLocalAtStmt(10, "h",
            andExpect(7, "h", "$r1[c]", "e[b,c]", "$r0[b,c]", "g[b,c]")));

        return res;
      }
    });
  }

  @Test
  public void subTestOfSimple() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter$D: void <init>(cases.AliasInOuter)>")
            .askForLocalAtStmt(13, "this", andExpect(1, "this")));
        return res;
      }
    });
  }

  @Test
  public void simple2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void test2()>").askForLocalAtStmt(11, "x",
            andExpect(7, "x", "d[f]", "$r0[f]", "a[g]", "$r1[g]")));
        return res;
      }
    });
  }

  @Test
  public void paramTurnTest() {

    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries(
                "<cases.AliasInOuter: void aliasF(cases.AliasInOuter$D,cases.AliasInOuter$D)>");
        query.addQuery(new Query(7, "$r0", new ResultObject(1, "$r0", "a[f]", "d[f]")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void returnValueTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void returnValue()>").askForLocalAtStmt(7, "t",
            andExpect(4, "t", "f[f(java.lang.Object)]", "$r0[f(java.lang.Object)]")));
        return res;
      }
    });
  }

  @Test
  public void doubleReturnValueTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void doubleReturnValue()>").askForLocalAtStmt(7,
            "t", andExpect(4, "t", "f[d,f(java.lang.Object)]", "$r0[d,f(java.lang.Object)]")));
        return res;
      }
    });
  }

  @Test
  public void recursiveAliasInOuter() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn()>").askForLocalAtStmt(
            19,
            "x",
            andExpect(16, "x", "c[d,f]", "$r2[d,f]", "$r1[f]", "$r3[f]", "$r4[f]", "b[b,f]",
                "e[b,f]", "a[b,f]", "$r0[b,f]")));
        return res;
      }
    });
  }


  @Test
  @Ignore
  public void subqueryRecursiveAliasInOuter() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn()>").askForLocalAtStmt(
            16, "a[b]", andExpect(16, "a[b]")));
        return res;
      }
    });
  }

  @Test
  public void recursiveAliasInOuter2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            10, "x", andExpect(7, "x", "$r1[c]", "a[b,c]", "c[c]", "$r0[b,c]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryOfRecursiveAliasInOuter2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            6, "a[b]", andExpect(4, "a[b]", "$r0[b]")));
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            7, "a[b]", andExpect(4, "a[b]", "c", "$r0[b]")));
        return res;
      }
    });
  }

  @Test
  public void subQuery2OfRecursiveAliasInOuter2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            7, "a", andExpect(3, "a", "$r0")));
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            7, "a[b]", andExpect(4, "a[b]", "c", "$r0[b]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryAtLevel2OfRecursiveAliasInOuter2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void recursiveAliasOnReturn2()>").askForLocalAtStmt(
            7, "a[b]", andExpect(4, "a[b]", "$r0[b]", "c")));
        res.add(inMethod("<cases.AliasInOuter$A1: void <init>(cases.AliasInOuter)>")
            .askForLocalAtStmt(9, "this[b]", andExpect(6, "this[b]", "$r0")));
        return res;
      }
    });
  }

  @Test
  public void subQuery3OfRecursiveAliasInOuter2() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter$A1: void <init>(cases.AliasInOuter)>")
            .askForLocalAtStmt(9, "this[b]", andExpect(6, "this[b]", "$r0")));
        return res;
      }
    });
  }

  @Test
  public void aliasOfBaseTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.AliasInOuter: void aliasOfBase()>").askForLocalAtStmt(13, "h",
            andExpect(3, "h", "$r2[c]", "$r0", "e[b,c]", "g[b,c]", "$r1[b,c]", "a")));
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.AliasInOuter";
  }
}
