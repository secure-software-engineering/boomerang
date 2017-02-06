package cases;

@SuppressWarnings("unused")
public class InnerClassSimpleTarget {
  private String g = new String("internal");

  public static void main(String... args) {
    test1();
    new InnerClassSimpleTarget().test1a();
  }

  private static void test1() {
    InnerClassSimpleTarget i = new InnerClassSimpleTarget();
    Inner a = i.createInner();
    String string = a.getString();
    String t = string;
  }

  private void test1a() {
    Inner a = createInner();
    String string = a.getString();
    String t = string;
  }

  public Inner createInner() {
    return new Inner();
  }

  private class Inner {
    String getString() {
      return g;
    }
  }
}
