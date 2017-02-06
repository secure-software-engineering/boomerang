package cases;

@SuppressWarnings("unused")
public class FieldInsensitive {
  static class A {
    String f;
  };
  static class B extends A {
  }

  public static void main(String[] args) {
    FieldInsensitive obj = new FieldInsensitive();
    branching();

    obj.forward();
    obj.identityTest();
    identityReturnCall();
    identityReturnCall2();
    identityReturnCallWithBranch();
    doubleIdentityCall();
    notAliasingIdentity();
    twoParameterMethodAliasing();
    interLoop();
    intraLoop();
    branchWithOverwrite("");
    fakeIdentity();
    asynchron();
    cast();
  }

  private static void branchWithOverwrite(String x) {
    A b = new A();
    A a = new A();
    if (x != null) {
      b = a;
      a = new A();
    }
  }

  private void identityTest() {
    A a = new A();
    A b = id(a);
  }

  private A id(A a) {
    return a;
  }

  private class J {
    int n;
  }

  private static void cast() {
    A alias1 = new B();
    B alias2 = (B) alias1;
  }

  private static void asynchron() {
    A alias1 = new A();
    A alias2 = alias1;
    A alias3 = alias2;
    if (alias1.f != null) {
      A alias3a = alias3;
      A alias4a = alias3;
      A alias5a = alias3;
      int s = 1;
      s++;
    } else {
      A alias3b = alias3;
      A alias4b = alias3;
      A alias5b = alias3;
      int e = 1000000000;
    }
  }

  private static A identity(A param) {
    A mapped = param;
    return mapped;
  }

  private static void identityReturnCallWithBranch() {
    A alias = new A(), aliased;
    aliased = identityBranched(alias);
  }

  private static A paramOverride(A param) {
    param = new A();
    return param;
  }

  private static void fakeIdentity() {
    A alias = new A(), aliased;
    aliased = paramOverride(alias);
  }

  private static A identityBranched(A param) {
    A x = new A();
    if (x != null) {
      return new A();
    }
    return param;
  }

  public void forward() {
    A a = new A(), b, c, d, e;
    d = a;
    c = new A();
    e = a;
  }


  private static A testCall(A testa) {
    A test = testa;
    return test;
  }

  public static void branching() {
    A e = new A();
    A a = new A();
    A c, d;
    if (e.f != null) {
      c = a;
    } else {
      e = a;
    }
    d = e;
  }

  private static void doubleIdentityCall() {
    A alias1 = new A(), alias2, alias3, alias4;
    alias2 = identity(alias1);
    alias3 = identity(alias2);
    alias4 = alias1;

  }


  private static void identityReturnCall() {
    A alias = new A(), aliased;
    aliased = identity(alias);
  }

  private static void identityReturnCall2() {
    A alias = new A(), aliased, aliased2;
    aliased = identity(alias);
    aliased2 = aliased;
  }

  private static void notAliasingIdentity() {
    A alias = new A(), aliased;
    identity(alias);
    aliased = alias;
  }


  private static void twoParameterMethodAliasing() {
    A alias = new A(), aliased, noAlias = new A();
    aliased = identity(noAlias, alias);
  }

  private static A identity(A noAlias, A alias) {
    return alias;
  }

  private static void intraLoop() {
    A alias = new A(), aliased2, aliased = new A(), noAlias = new A();
    for (int i = 0; i < 20; i++) {
      aliased = alias;
    }
    aliased2 = aliased;
  }

  private static void interLoop() {
    A alias = new A(), aliased = new A(), noAlias = new A(), aliased2;
    for (int i = 0; i < 20; i++) {
      aliased = identity(alias);
    }
    aliased2 = aliased;

  }
}
