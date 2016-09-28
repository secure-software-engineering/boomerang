package example;

public class Program3 {

  public static void main(String[] args) {
    Object allocated = new Object();
    Object alias = allocated;
    context(allocated, alias);
  }

  public static void context(Object allocated, Object alias) {
    Object andAnother = alias;
  }
}
