package tests.nocontextrequest;

import test.core.AliasTest;

@SuppressWarnings("rawtypes")
public class SubclassTest extends AliasTest {

  // @Test
  // public void method1Test() {
  // runAnalysis(false, new IQueryHandler() {
  // @Override
  // public ArrayList<MethodQueries> queryAndResults() {
  // ArrayList<MethodQueries> res = new ArrayList<>();
  // res.add(inMethod("<cases.SubclassTarget: void method1(cases.SubclassTarget$IFace)>")
  // .askForLocalAtStmt(
  // 5,
  // "x",
  // andExpect(
  // 3,
  // "x",
  // "e[c]",
  // "iface(cases.SubclassTarget$Class1)[<cases.SubclassTarget$Class1: cases.A fieldClass1>,c]",
  // "iface(cases.SubclassTarget$Class2)[<cases.SubclassTarget$Class2: cases.A fieldClass2>,c]")));
  //
  // return res;
  // }
  // });
  // }
  //
  // @Test(timeout = 10000)
  // public void method2Test() {
  // runAnalysis(false, new IQueryHandler() {
  // @Override
  // public ArrayList<MethodQueries> queryAndResults() {
  // ArrayList<MethodQueries> res = new ArrayList<>();
  // res.add(inMethod("<cases.SubclassTarget: void method2(cases.SubclassTarget$IFace)>")
  // .askForLocalAtStmt(
  // 6,
  // "x",
  // andExpect(
  // 3,
  // "x",
  // "e[c]",
  // "iface(cases.SubclassTarget$Class1)[<cases.SubclassTarget$Class1: cases.A fieldClass1>,c]",
  // "iface(cases.SubclassTarget$Class2)[<cases.SubclassTarget$Class2: cases.A fieldClass2>,c]")));
  //
  // return res;
  // }
  // });
  //
  // }
  //
  // @Test
  // public void baseWithFieldTest() {
  // runAnalysis(false, new IQueryHandler() {
  // @Override
  // public ArrayList<MethodQueries> queryAndResults() {
  // ArrayList<MethodQueries> res = new ArrayList<>();
  // res.add(inMethod("<cases.SubclassTarget: void baseWithField()>").askForLocalAtStmt(
  // 10,
  // "v",
  // andExpect(6, "v", "$r1", "subclass[<cases.SubclassTarget$Base: java.lang.String f>]",
  // "$r0[<cases.SubclassTarget$Base: java.lang.String f>]")));
  //
  // return res;
  // }
  // });
  // }
  //
  // @Test(timeout = 10000)
  // public void preQueryAndThenMethod2Test() {
  // runAnalysis(false, new IQueryHandler() {
  // @Override
  // public ArrayList<MethodQueries> queryAndResults() {
  // ArrayList<MethodQueries> res = new ArrayList<>();
  // res.add(inMethod("<cases.SubclassTarget$Class1: cases.A doSomething()>").askForLocalAtStmt(
  // 7, "$r0",
  // andExpect(3, "$r0", "$r1", "this[<cases.SubclassTarget$Class1: cases.A fieldClass1>]")));
  //
  // res.add(inMethod("<cases.SubclassTarget$Class2: cases.A doSomething()>").askForLocalAtStmt(
  // 7, "$r0",
  // andExpect(3, "$r0", "$r1", "this[<cases.SubclassTarget$Class2: cases.A fieldClass2>]")));
  // res.add(inMethod("<cases.SubclassTarget: void method2(cases.SubclassTarget$IFace)>")
  // .askForLocalAtStmt(
  // 6,
  // "x",
  // andExpect(
  // 3,
  // "x",
  // "e[c]",
  // "iface(cases.SubclassTarget$Class1)[<cases.SubclassTarget$Class1: cases.A fieldClass1>,c]",
  // "iface(cases.SubclassTarget$Class2)[<cases.SubclassTarget$Class2: cases.A fieldClass2>,c]")));
  //
  // return res;
  // }
  // });
  // }
  //
  // @Test(timeout = 10000)
  // public void correlatedCallsTest() {
  // runAnalysis(false, new IQueryHandler() {
  // @Override
  // public ArrayList<MethodQueries> queryAndResults() {
  // ArrayList<MethodQueries> res = new ArrayList<>();
  // res.add(inMethod("<cases.SubclassTarget: void correlatedCalls(java.lang.String[])>")
  // .askForLocalAtStmt(
  // 17,
  // "v",
  // andExpect(
  // 12,
  // "b(cases.SubclassTarget$Subclass2)[<cases.SubclassTarget$Subclass2: java.lang.String field2>]",
  // "b(cases.SubclassTarget$Subclass)[<cases.SubclassTarget$Base: java.lang.String f>]",
  // "b(cases.SubclassTarget$Subclass)[<cases.SubclassTarget$Subclass: java.lang.String field1>]",
  // "$r1",
  // "b(cases.SubclassTarget$Subclass2)[<cases.SubclassTarget$Base: java.lang.String f>]",
  // "$r2(cases.SubclassTarget$Subclass)[<cases.SubclassTarget$Base: java.lang.String f>]",
  // "v",
  // "$r0(cases.SubclassTarget$Subclass2)[<cases.SubclassTarget$Subclass2: java.lang.String field2>]",
  // "$r2(cases.SubclassTarget$Subclass)[<cases.SubclassTarget$Subclass: java.lang.String field1>]",
  // "$r0(cases.SubclassTarget$Subclass2)[<cases.SubclassTarget$Base: java.lang.String f>]")));
  //
  // return res;
  // }
  // });
  // }


  @Override
  public String getTargetClass() {
    return "cases.SubclassTarget";
  }
}
