package cases;

@SuppressWarnings("unused")
public class BackwardEnterMethodAliasTarget {
  public static void main(String... args) {
    new BackwardEnterMethodAliasTarget().test1();
    new BackwardEnterMethodAliasTarget().test2();
    new BackwardEnterMethodAliasTarget().test3();
    new BackwardEnterMethodAliasTarget().test4();
    new BackwardEnterMethodAliasTarget().test5();
    new BackwardEnterMethodAliasTarget().test6();
    new BackwardEnterMethodAliasTarget().test7();
    new BackwardEnterMethodAliasTarget().loop();
  }

  private void loop() {
    A a = new A();
    a.b = new B();
    entry(a, a);
    C x = a.b.c;
  }

  private void entry(A a, A b) {
    b.b.c = new C();
    if (a.b != null)
      entry(a, b);
    else if (a.b != null)
      entry(b, a);
    a.b.c = b.b.c;
  }

  private void test1() {
    A a = new A();
    a.b = new B();
    foo(a);
    B h = a.b;
    C x = h.c;
  }

  private void test5() {
    A a = new A();
    allocViaAlias(a);
    B h = a.b;
  }

  private void test6() {
    A a = new A();
    allocViaFieldRead(a);
    B e = a.b;
  }

  private void allocViaFieldRead(A a) {
    E c = new E();
    c.f = a;
    c.f.b = new B();
  }

  private void allocViaAlias(A a) {
    A b = a;
    b.b = new B();
  }

  private void test4() {
    A a = new A();
    a.b = new B();
    wrapped(a);
    B h = a.b;
    C x = h.c;
  }

  private void test7() {
    A a = new A();
    a.b = new B();
    A b = new A();
    wrapped(a, b);
    C h = b.b.c;
  }

  private void wrapped(A a, A b) {
    alias(a, b);
    a.b.c = new C();
  }

  private void alias(A a, A b) {
    b.b = a.b;
  }

  private void wrapped(A a) {
    foo(a);
  }

  private void foo(A a) {
    B e = a.b;
    e.c = new C();
  }

  private void test2() {
    A a = new A();
    a.b = new B();
    B e = a.b;
    e.c = new C();
    noAllocWithin(a);
    B h = a.b;
    C x = h.c;
  }

  private void noAllocWithin(A a) {
    int x = 0;
    if (a != null) {
      x++;
    }
  }

  private void test3() {
    A a = new A();
    B b = new B();
    b.c = new C();
    alias(a, b);
    B h = a.b;
    C x = h.c;
  }

  private void alias(A a, B b) {
    a.b = b;
  }

  public class A {
    B b;
  }
  public class B {
    C c;
  }
  public class C {
    D d;
  }
  public class D {
  }

  public class E {
    A f;
  }
}
