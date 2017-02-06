package cases;

@SuppressWarnings("unused")
public class ReverseTarget {
  public static void main(String... args) {
    new ReverseTarget().test1();
    new ReverseTarget().test2();
    new ReverseTarget().test2branched();
    new ReverseTarget().meetingPointAtReturnWithAllocInside();
    new ReverseTarget().meetingPointAtReturnWithAllocOutside();
    new ReverseTarget().paramFieldTest(new A());
  }

  private void meetingPointAtReturnWithAllocOutside() {
    A a = new A();
    a.b = new B();
    id(a);
    B x = a.b;
  }

  private void id(A a) {}

  private void meetingPointAtReturnWithAllocInside() {
    A a = new A();
    foo(a);
    B x = a.b;
  }

  private void paramFieldTest(A a2) {
    B x = a2.b;
  }

  private void foo(A a) {
    a.b = new B();
  }

  private void test2branched() {
    A a = new A();
    A b = null, c = null;
    if (a.d != null)
      b = a;
    else
      c = a;

    if (c.d != null || b.d != null)
      return;
  }

  private void test2() {
    A a = new A();
    A b = a;
  }

  private void test1() {
    A a = new A();
    a.b = new B();
    A c = new A();
    foo(a, c);
    B x = c.b;
  }

  private void foo(A a, A c) {
    c.b = a.b;
  }
}
