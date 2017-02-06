package cases;

@SuppressWarnings("unused")
public class InnerClassTarget {
  private String g = new String("internal");
  private Inner inner;
  private String outerClassField = new String();
  private Nested nested;

  public static void main(String... args) {
    test1();
    new InnerClassTarget().test1a();
    test2();
    new InnerClassTarget().test3();
    new InnerClassTarget().test4();
    new InnerClassTarget().test5();
    new InnerClassTarget().test6();
  }

  private class Nested {
    String getField() {
      return outerClassField;
    }
  }

  private void test3() {
    outerClassField = new String();
    Nested n = new Nested();
    String v = n.getField();
  }


  private void test4() {
    Nested n = new Nested();
    String v = n.getField();
  }

  private class Nested2 {
    String field;

    void grep() {
      field = outerClassField;
    }
  }

  private void test5() {
    Nested n = new Nested();
    Nested2 n2 = new Nested2();
    n2.grep();
    String v = n.getField();
  }

  private void test6() {
    nested = new Nested();
    Nested2 n2 = new Nested2();
    n2.grep();
    String v = nested.getField();
  }

  private static void test1() {
    InnerClassTarget i = new InnerClassTarget();
    Inner a = i.createInner();
    String string = a.getString();
    String t = string;
  }

  private void test1a() {
    Inner a = createInner();
    String string = a.getString();
    String t = string;
  }

  private static void test2() {
    InnerClassTarget i = new InnerClassTarget();
    Inner a = i.createInnerCached();
    String string = a.getString();
    String t = string;
  }



  private Inner createInnerCached() {
    if (inner == null)
      inner = new Inner();
    return inner;
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
