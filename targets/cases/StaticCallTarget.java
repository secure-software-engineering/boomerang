package cases;

@SuppressWarnings("unused")
public class StaticCallTarget {
  public static void main(String[] args) {
    method1();
  }

  private static void method1() {
    A c = staticallyGetA();
    A x = c.c;
  }

  private static A staticallyGetA() {
    A b = new A();
    A a = new A();
    a.c = b;
    return a;
  }
}
