package example;

public class Program2 {

  public static void main(String[] args) {
    ClassWithField classWithField = new ClassWithField();
    classWithField.field = new Object();
    ClassWithField alias = classWithField;
  }

  private static class ClassWithField {
    private Object field;
  }

}
