package cases;

@SuppressWarnings("unused")
public class AliasInOuter {
  public static void main(String... args) {
    AliasInOuter aliasInOuter = new AliasInOuter();
    aliasInOuter.test1();
    aliasInOuter.test2();
    aliasInOuter.recursiveAliasOnReturn();
    aliasInOuter.recursiveAliasOnReturn2();
    aliasInOuter.aliasOfBase();
    aliasInOuter.returnValue();
    aliasInOuter.wrappedAlloc();
    aliasInOuter.doubleReturnValue();
  }

  private void returnValue() {
    D f = new D();
    Object t = f.getField();
  }


  private void doubleReturnValue() {
    D f = new D();
    Object t = f.getDoubleField();
  }

  public class D {
    String f = new String("HERE");
    D d = new D();

    public Object getField() {
      return f;
    }

    public Object getDoubleField() {
      return d.getField();
    }
  }
  public class C {
    String g = new String("HERE");
  }

  public void test1() {
    D d = new D();
    D a = new D();

    aliasF(d, a);

    String x = d.f;
  }

  private void aliasF(D d, D a) {
    d.f = a.f;
  }

  private void aliasF(D d, C a) {
    d.f = a.g;
  }

  public void test2() {
    D d = new D();
    C a = new C();

    aliasF(d, a);

    String x = d.f;
  }


  private class A {
    A b;
    String f;
  }
  private class C1 {
    A d;
  }

  public void recursiveAliasOnReturn() {
    A a = new A();
    A b = a;
    b.b = new A();
    C1 c = new C1();
    c.d = b.b;
    A e = a;

    alloc(a);
    String x = c.d.f;
  }

  private void alloc(A a) {
    a.b.f = new String("Here");
  }

  public void recursiveAliasOnReturn2() {
    A1 a = new A1();
    B1 c = alias(a);
    foo(a);
    C2 x = a.b.c;
  }

  public B1 alias(A1 a) {
    return a.b;
  }

  public void foo(A1 a) {
    a.b.c = new C2();
  }



  public void aliasOfBase() {
    C2 a = new C2();
    A1 e = new A1();
    A1 g = e;
    alloc(a, e);
    C2 h = e.b.c;
  }

  public void wrappedAlloc() {
    A1 e = new A1();
    A1 g = e;
    wrapper(g);
    C2 h = e.b.c;
  }

  private void wrapper(A1 g) {
    alloc(g);
  }

  private void alloc(A1 g) {
    g.b.c = new C2();
  }

  private void alloc(C2 a, A1 b) {
    b.b.c = a;
  }

  public class A1 {
    B1 b = new B1();
  }


  public class B1 {
    C2 c;
  }
  public class C2 {
    String g;
  }
}
