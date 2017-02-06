package cases;

@SuppressWarnings("unused")
public class FieldSensitiveBranchingTarget {
  public static void main(String... args) {
    branching1();
    fieldRead();
    problematicTest();
    recursiveTest();
    branchingWithParam();
  }

  private static class B extends A {
    A classB = new A();

    public B() {
      classB = this.f;
    }
  }
  private static class C extends A {
    A classC = new A();

    public C() {
      classC = this.d;
    }
  }

  private static void branching1() {
    A a = new A();
    int x = 1;

    if (x > 9) {
      a.d = new FieldSensitiveBranchingTarget.B();
    } else {
      a.d = new FieldSensitiveBranchingTarget.C();
    }

    A c = a.d;
    A y = c.d;
  }

  private static void fieldRead() {
    A a = new A();
    int x = 1;
    a.d = new FieldSensitiveBranchingTarget.C();
    A c = a.d;
    A y = c.d;
  }

  private static void problematicTest() {
    A a = new A();
    a = a.d;
    A y = a.d;
  }

  private static void recursiveTest() {
    A a = new A();
    A c = a.d;
    A y = c.d;
  }

  private static void branchingWithParam() {
    A a = new A();
    method(a, a);
    method2(a, a);
    method3(a, a);
    method4(a, a);
    method5(a, a);
  }

  private static void method(A a, A b) {
    if (a.d != null) {
      a.f = b;
    }
    if (a.d != null) {
      a.c = b;
    }
    A x = b;
  }

  private static void method2(A a, A b) {
    if (a.d != null)
      a = a.d;
    if (a.d != null) {
      a.f = b;
    }
    if (a.d != null) {
      a.c = b;
    }
    A x = b;
  }

  private static void method3(A a, A b) {
    if (a.d != null)
      b = b.d;
    if (a.d != null) {
      a.f = b;
    }
    if (a.d != null) {
      a.c = b;
    }
    A x = b;
  }

  private static void method4(A a, A b) {
    if (a.d != null)
      b = b.d;
    if (a.d != null) {
      a.d = b;
    }
    A x = b;
  }

  private static void method5(A a, A b) {
    if (a.d != null)
      a = a.d;
    if (a.d != null) {
      a.f = b;
    }
    A x = b;
  }
}
