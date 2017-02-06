package arrays;

public class ArrayTarget {
  public static class A {
  }

  public static void method() {
    Object[] o = new Object[3];
    A i1 = new A();
    o[1] = i1;
    Object k = o[2];
  }

  public static void test1() {
    Object[] o = new Object[3];
    A i1 = new A();
    A i2 = new A();
    o[1] = i1;
    o[2] = i2;
    Object k = o[3];
  }

  public static void main(String... args) {
    test1();
    test2();
    arrayCopyTest();
    method();
  }

  private static void test2() {
    Object[][] o = new Object[3][3];
    o[1][2] = new A();
    o[2][3] = new A();
    Object k = o[3][3];

  }

  private static void arrayCopyTest() {
    Object[] o = new Object[3];
    Object[] v = new Object[3];
    A i1 = new A();
    A i2 = new A();
    v[1] = i1;
    System.arraycopy(v, 0, o, 0, 1);
    Object k = o[3];

  }
}
