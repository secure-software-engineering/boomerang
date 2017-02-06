package cases;

@SuppressWarnings("unused")
public class DoubleQueryTarget {

  public static void main(String[] args) {
    outer();
    simple();
  }

  private static void simple() {
    A n;
    A a = new A();
    n = a;
  }

  private static void outer() {
    A d = new A();
    A h = d.f;
    inner(d);
    int after = 1;
    A u = d.f;
  }

  private static void inner(A a) {
    A c = new A();
    a.f = c;
    A x = a.f;
    int shoulWotk = 1;
  }
}
