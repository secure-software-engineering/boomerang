package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class InnerClassTest extends AliasTest {

  @Test
  public void test1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1()>").askForLocalAtStmt(8, "t",
            andExpect(3, "string", "a[this$0,g]", "t", "i[g]", "$r0[g]")));
        return res;
      }
    });
  }

  @Test
  public void test1e() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1()>").askForLocalAtStmt(6, "$r0[g]",
            andExpect(3, "a[this$0,g]", "i[g]", "$r0[g]")));
        return res;
      }
    });
  }

  @Test
  public void subQueryOfTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<cases.InnerClassTarget$Inner: void <init>(cases.InnerClassTarget,cases.InnerClassTarget$Inner)>")
            .askForLocalAtStmt(6, "l0", andExpect(1, "l0")));

        res.add(inMethod(
            "<cases.InnerClassTarget$Inner: void <init>(cases.InnerClassTarget,cases.InnerClassTarget$Inner)>")
            .askForLocalAtStmt(6, "l0[this$0]", andExpect(1, "l0[this$0]", "l1")));
        res.add(inMethod(
            "<cases.InnerClassTarget$Inner: void <init>(cases.InnerClassTarget,cases.InnerClassTarget$Inner)>")
            .askForLocalAtStmt(6, "l0[this$0,g]", andExpect(1, "l0[this$0,g]", "l1[g]")));
        return res;
      }
    });
  }


  @Test
  public void test1a() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1a()>").askForLocalAtStmt(6, "t",
            andExpect(1, "string", "a[this$0,g]", "t", "this[g]")));
        return res;
      }
    });
  }

  @Test
  public void test1b() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1a()>").askForLocalAtStmt(4, "this",
            andExpect(1, "this", "a[this$0]")));
        return res;
      }
    });
  }

  @Test
  public void test1c() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1a()>").askForLocalAtStmt(5, "this",
            andExpect(1, "this", "a[this$0]")));
        return res;
      }
    });
  }

  @Test
  public void test1d() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<cases.InnerClassTarget: void test1a()>").askForLocalAtStmt(4, "this",
            andExpect(1, "this", "a[this$0]")));
        res.add(inMethod("<cases.InnerClassTarget: void test1a()>").askForLocalAtStmt(5, "this",
            andExpect(1, "this", "a[this$0]")));
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
        res.add(inMethod("<cases.InnerClassTarget: void test2()>").askForLocalAtStmt(
            8,
            "t",
            andExpectNonEmpty()));
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
        res.add(inMethod("<cases.InnerClassTarget: void test3()>").askForLocalAtStmt(
            10,
            "v",
            andExpect(3, "v", "this[outerClassField]", "n[this$0,outerClassField]", "$r0",
                "$r1[this$0,outerClassField]")));
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
        res.add(inMethod("<cases.InnerClassTarget: void test4()>").askForLocalAtStmt(
            7,
            "v",
            andExpect(1, "v", "this[outerClassField]", "n[this$0,outerClassField]",
                "$r0[this$0,outerClassField]")));
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
        res.add(inMethod("<cases.InnerClassTarget: void test5()>").askForLocalAtStmt(
            11,
            "v",
            andExpect(1, "v", "this[outerClassField]", "n[this$0,outerClassField]",
                "$r0[this$0,outerClassField]", "$r1[this$0,outerClassField]",
                "n2[this$0,outerClassField]", "n2[field]", "$r1[field]")));
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
        res.add(inMethod("<cases.InnerClassTarget: void test6()>")
            .askForLocalAtStmt(
                11,
                "n2[field]",
 andExpectNonEmpty()));
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.InnerClassTarget";
  }
}
