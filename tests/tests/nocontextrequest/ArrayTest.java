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
public class ArrayTest extends AliasTest {
  @Test
  public void multipleAllocationSitesTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<arrays.ArrayTarget: void test1()>").askForLocalAtStmt(12, "k",
            andExpect(6, "k", "o[array]", "i2(java.lang.Object)", "$r1(java.lang.Object)"),
            andExpect(3, "k", "o[array]", "i1(java.lang.Object)", "$r0(java.lang.Object)"),
            andExpect(2, "k", "o[array]")));


        return res;
      }
    });
  }

  @Test
  public void systemArrayCopyTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<arrays.ArrayTarget: void arrayCopyTest()>").askForLocalAtStmt(16, "k",
            andExpect(4, "k", "o[array]", "v[array]", "i1(java.lang.Object)",
                "$r0(java.lang.Object)"),
            andExpect(2, "k", "o[array]"), andExpect(3, "k", "o[array]", "v[array]")));


        return res;
      }
    });
  }

  @Test
  @Ignore
  public void doubleArrayTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<arrays.ArrayTarget: void test2()>").askForLocalAtStmt(13, "k",
            andExpect(4, "k", "o[array,array]", "$r1[array]", "$r0", "$r3[array]", "$r4[array]"),
            andExpect(8, "k", "o[array,array]", "$r1[array]", "$r2", "$r3[array]", "$r4[array]")));


        return res;
      }
    });
  }

  @Test
  public void simpleTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<arrays.ArrayTarget: void method()>").askForLocalAtStmt(8, "k",
            andExpect(3, "k", "o[array]", "i1(java.lang.Object)",
                        "$r0(java.lang.Object)"),
            andExpect(2, "k", "o[array]")));


        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "arrays.ArrayTarget";
  }

}
