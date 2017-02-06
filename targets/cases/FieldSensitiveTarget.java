package cases;

import contextrequestor.Loop1;


@SuppressWarnings("unused")
public class FieldSensitiveTarget {
  private static A a;

  public static void main(String[] args) {
    test1();
    test1a();
    test2();
    fieldNullAndThenWrite();
    askForSameBaseTwice();
    fieldReadRecursion();
    fieldWriteRecursion();
    fieldSensitiveFieldRead();
    fieldSensitiveFieldReadAndWriteWithLoop();
    longerAccessPath();
    fieldWrite();
    simpleTest();
    simpleTest2();
    simpleTest3();
    doubleQuery();
    doubleReadSameField();
    doubleReadSameFieldFromConstructor();
    new FieldSensitiveTarget().longerAccessNotSameAlloc();
    new FieldSensitiveTarget().staticFieldTest();
    new FieldSensitiveTarget().thisAliasTest();
    new FieldSensitiveTarget().thisAliasTest2();
    new FieldSensitiveTarget().fieldReadRec();
    new FieldSensitiveTarget().paramAlias();
    new FieldSensitiveTarget().fieldWriteTestWithinMethod();
  }



  private void fieldWriteTestWithinMethod() {
    A a = new A();
    A b = a;
    A alias = new A();
    A c = bax(a, b, alias);
  }



  private A bax(A a, A b, A o) {
    b.f = o;
    A c = a.f;
    return c;
  }



  private static void doubleQuery() {
    Loop1 c = new Loop1();
    c.loop();
  }



  private static void fieldNullAndThenWrite() {
    A a = new A();
    A e = a;
    e.b = new B();
  }



  public class B3 {
    C e = new C();
  }
  public class A4 {
    B3 b = new B3();
  }

  private void paramAlias() {
    A4 a = new A4();
    a.b = new B3();
    B3 b = new B3();
    foo(a, b);
    C y = b.e;
    C x = a.b.e;
  }

  private void foo(A4 a2, B3 b) {
    a2.b.e = b.e;
  }

  private class P {
    FieldSensitiveTarget t;
  }

  private void thisAliasTest() {
    P p = new P();
    foo(p);
    FieldSensitiveTarget a = p.t;
  }

  private void thisAliasTest2() {
    P p = new P();
    foo1(p);
    FieldSensitiveTarget a = p.t;
  }

  private void foo1(P p) {
    foo(p);
  }

  private void foo(P p) {
    p.t = this;
  }


  private static void doubleReadSameField() {
    A a = new A();
    a.d.d = new A();
    A b = a.d.d;
  }

  private static void doubleReadSameFieldFromConstructor() {
    D a = new D();
    D b = a.next.next;
  }

  private void staticFieldTest() {
    a = new A();
    A b = a;
  }

  private static void simpleTest() {
    E a = new E();
    a.f = new E();
    E b = a.f;
  }

  private static void fieldWrite() {
    E a = new E();
    E b = a;
    E h = new E();
    a.f = h;
    E x = a.f;
  }

  private static void simpleTest2() {
    A a = new A();
    a.f = new A();
    A b = a.f;
  }

  private static void simpleTest3() {
    A a = new A();
    A b = a.f;
  }

  private static void longerAccessPath() {
    A a = new A();
    a.f = new A();
    A b = a.f;
    b.f = new A();
    A c = b.f;
  }

  private class A1 {
    A2 f;
  }
  private class A2 {
    A3 f;
  }
  private class A3 {
  }

  private void longerAccessNotSameAlloc() {
    A1 a = new A1();
    a.f = new A2();
    A2 b = a.f;
    b.f = new A3();
    A3 c = b.f;
  }

  private static void fieldSensitiveFieldReadAndWriteWithLoop() {
    A a = new A();
    a.f = new A();
    a.f.f = new A();
    while (a != null) {
      a = a.f;
    }
    A x = a;
  }

  private static void fieldSensitiveFieldRead() {
    A a = new A();
    a.f = new A();
    A c = a;
    A b = a.f;
  }

  private static void fieldWriteRecursion() {
    A a = new A();
    A b;
    for (int i = 0; i < 2; i++) {
      b = a;
      a.c = b;
    }
  }

  private static void fieldReadRecursion() {
    A a = new A();
    A b;
    for (int i = 0; i < 2; i++) {
      b = a;
      a = b.c;
    }
  }
  private static void askForSameBaseTwice() {
    A d = new A();
    A e = new A();
    A f = e;
    d.c = e;
    int nee = 1;
    d.d = e;
  }


  public static void test2() {
    A e = new A();
    // 8. Turn around analysis from 7.
    A d = new A();
    A f = e;
    A k = getAndAlias(e);
    // 9. alias = {e.d, k}, trigger alias search for e to find f!
    // 7. receives {e.d, k} triggers allocSite analysis for e //we actually analysis getAndAlias(e)
    // partly twice?
    // 2. still looking for aliases of k (searching alloc site)

    // 10. processCall with {e.d, k} -> {a.d, b}
    k = alias(e, k, d);
    // 15. returns {k, e.c} which come form {b,a.c} inside method, triggers alloc analysis for e, we
    // need to clue the path of 7?!

    A taint = new A();
    // 1. ask for alias of k?
    k.f = taint;
  }

  static A getAndAlias(A h) {
    // 5. allocsite of h is here. turnaround
    A z = new A();
    // 3. Turn around
    h.d = z;
    // 4. search for allocsite of h upwards, will find h and nothing else
    return z;
    // 6. returning (z = new) = {h.d, z}
  }

  public static void test1() {
    A e = new A();
    A k = new A();
    A d = new A();
    k = alias(e, k, d);
    // 3. k = {e.c, k} find alias for e... found nothing! finished!
    A taint = new A();
    // 1. ask for alias of k?
    k.f = taint;
  }

  public static void test1a() {
    A e = new A();
    A k = new A();
    A d = new A();
    k = alias(e, k, d);
    A b = e.c;
    // 3. k = {e.c, k} find alias for e... found nothing! finished!
    A taint = new A();
    // 1. ask for alias of k?
    k.f = taint;
  }

  public static A alias(A a, A b, A c) {
    // 13. Turn around alloc analysis from 12.
    A y = a;
    // 11. a.d -> y.d
    y.c = b; // *
    // 12. ask for aliases of y, will find a in 13.
    y.d = b;
    a.c.f = null;
    return b;
    // 14. we will find b = {y.c,b,a.c(derived from y.c by asking for alias of y at stmt *)}
    // so b and a.c will be returned
  }

  private void fieldReadRec() {
    N node = new N();
    N n = node;

    int i = 0;
    while (i < 10) {
      node = node.next;
      i++;
    }
    N o = n.next;
    N p = n.next.next;
    N q = n.next.next.next;

  }

  public class N {
    N next;

    public N() {
      next = new N();
    }
  }

}
