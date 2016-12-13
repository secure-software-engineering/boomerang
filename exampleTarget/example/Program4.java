package example;

public class Program4 {

  public static void main(String[] args) {
    Object allocated = new Object();
    Object alias = allocated;
    context(allocated, alias);
    Object nonAliasedObject = new Object();
    context(allocated, nonAliasedObject);
  }

  public static void context(Object obj1, Object obj2) {
    Object queryVariable = obj1;
  }
}
