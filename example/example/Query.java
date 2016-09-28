package example;
import boomerang.accessgraph.AccessGraph;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;

public class Query {
  private SootMethod method;
  private AccessGraph accessGraph;
  private Unit statement;


  /**
   * Constructs a query.
   * 
   * @param methodSig The method signature for the method the query is executed within. Must be in
   *        the right format soot expects it: \\ <packagename.Classname: (void|returnType)
   *        methodName(argType1,argType2,...)>
   * @param accessGraph the access graph as a String in the form:
   *        "baseLocalVariable[field1,field2,field3]"
   * @param statementNo The number of the statement at which the query should be executed (the nth
   *        Unit of the active body of the SootMethod)
   */
  public Query(String methodSig, String accessGraph, int statementNo) {
    this.method = Scene.v().getMethod(methodSig);
    this.accessGraph =
        Util.stringToAccessGraph(method.getActiveBody().getLocals(), accessGraph);
    this.statement = Util.getNthStmtOfMethod(method, statementNo);
  }

  public Unit getStatement() {
    return statement;
  }

  public AccessGraph getAccessGraph() {
    return accessGraph;
  }

  public String toString() {
    String s = "Query: " + accessGraph + "@" + statement + " in " + method.toString();
    s += "\n" + method.getActiveBody().toString();
    return s;
  }
}
