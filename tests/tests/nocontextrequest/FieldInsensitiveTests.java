package tests.nocontextrequest;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class FieldInsensitiveTests extends AliasTest {
  @Test
  public void branchingTestD() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void branching()>");
        query.addQuery(new Query(14, "d", new ResultObject(2, "e", "d", "$r0"), new ResultObject(5,
            "e", "a", "$r1", "d")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void branchWithOverwrite() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries(
            "<cases.FieldInsensitive: void branchWithOverwrite(java.lang.String)>");
        query.addQuery(new Query(14, "a", new ResultObject(11, "a", "$r2"),
            new ResultObject(6, "a", "b", "$r1")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }
  @Test
  public void castTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void cast()>");
        query.addQuery(new Query(6, "alias2", new ResultObject(2, "alias1", "alias2", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void branchingTestA() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void branching()>");
        query.addQuery(new Query(14, "a", new ResultObject(5, "e", "c", "a", "$r1", "d")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void forwardTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void forward()>");
        query.addQuery(new Query(11, "$r0", new ResultObject(3, "e", "a", "$r0", "d")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void identityNonStaticTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void identityTest()>");
        query.addQuery(new Query(7, "b", new ResultObject(3, "a", "b", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void identityReturnCallTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void identityReturnCall()>");
        query.addQuery(new Query(6, "aliased", new ResultObject(2, "alias", "aliased", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void identityReturnCall2Test() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void identityReturnCall2()>");
        query.addQuery(new Query(7, "aliased2", new ResultObject(2, "alias", "aliased", "$r0",
            "aliased2")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void identityCallBranchedTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void identityReturnCallWithBranch()>");
        query.addQuery(new Query(6, "aliased", new ResultObject(2, "alias", "aliased", "$r0"),
            new ResultObject(5, "aliased")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void doubleIdentityCallTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void doubleIdentityCall()>");
        query.addQuery(new Query(8, "alias4", new ResultObject(2, "alias1", "alias2", "alias3",
            "alias4", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void notAliasingIdentityTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void notAliasingIdentity()>");
        query.addQuery(new Query(7, "aliased", new ResultObject(2, "alias", "aliased", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });

  }

  @Test
  public void twoParameterMethodAliasingTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query =
            new MethodQueries("<cases.FieldInsensitive: void twoParameterMethodAliasing()>");
        query.addQuery(new Query(9, "aliased", new ResultObject(2, "alias", "aliased", "$r0")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void intraLoopTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void intraLoop()>");
        query.addQuery(new Query(17, "aliased2", new ResultObject(2, "alias", "$r0", "aliased",
            "aliased2"), new ResultObject(5, "aliased", "aliased2", "$r1")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void interLoopTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void interLoop()>");
        query.addQuery(new Query(17, "aliased2", new ResultObject(2, "alias", "$r0", "aliased",
            "aliased2"), new ResultObject(5, "aliased", "$r1", "aliased2")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void asynchronTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void asynchron()>");
        query.addQuery(new Query(13, "alias5a", new ResultObject(2, "alias5a", "alias4a", "$r0",
            "alias3a", "alias1", "alias2", "alias3")));
        query.addQuery(new Query(18, "alias5a", new ResultObject(2, "alias5a", "alias4a", "$r0",
            "alias3a", "alias1", "alias2", "alias3")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Test
  public void fakeIdentityTest() {
    runAnalysis(false, new IQueryHandler() {
      @Override
      public ArrayList<MethodQueries> queryAndResults() {
        MethodQueries query = new MethodQueries("<cases.FieldInsensitive: void fakeIdentity()>");
        query.addQuery(new Query(6, "aliased", new ResultObject(5, "aliased")));
        ArrayList<MethodQueries> res = new ArrayList<>();
        res.add(query);
        return res;
      }
    });
  }

  @Override
  public String getTargetClass() {
    return "cases.FieldInsensitive";
  }
}
