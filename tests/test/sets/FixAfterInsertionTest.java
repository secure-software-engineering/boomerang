package test.sets;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class FixAfterInsertionTest extends AliasTest {

  @Test
  public void treeSetTest1() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(inMethod(
            "<sets.FixAfterInsertion: void fixAfterInsertion(sets.FixAfterInsertion$Entry)>")
                .askForLocalAtStmt(14, "x[parent]", andExpectNonEmpty()));
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "sets.FixAfterInsertion";
  }

  @Override
  public boolean includeJDK() {
    return true;
  }
}
