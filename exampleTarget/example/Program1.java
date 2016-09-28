package example;
public class Program1 {

  public static void main(String[] args) {
    Object allocated = new Object();
    Object oneAlias = identity(allocated);
    Object andAnother = oneAlias;
  }

  public static Object identity(Object o) {
    return o;
  }
}
