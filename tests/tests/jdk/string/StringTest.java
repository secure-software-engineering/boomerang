package tests.jdk.string;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class StringTest extends AliasTest {
  @Test
  public void appendStringTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<jdk.StringTarget: void main(java.lang.String[])>").askForLocalAtStmt(11,
            "c", andExpect(6, "c")));


        return res;
      }
    });
  }

  @Test
  public void returnFromAppenderTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod("<jdk.StringTarget: void main(java.lang.String[])>").askForLocalAtStmt(11,
            "$r1", andExpect(6, "c")));


        return res;
      }
    });
  }
  @Override
  protected boolean includeJDK() {
    return true;
  }

  @Override
  public String getTargetClass() {
    return "jdk.StringTarget";
  }

}
