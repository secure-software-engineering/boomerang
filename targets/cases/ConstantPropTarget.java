package cases;

public class ConstantPropTarget {
  public static void main(String... args) {
    int s = getInput();
    for (int i = 0; i < s; i++) {
      if (i == 100)
        error();
    }
  }

  private static void error() {
    // TODO Auto-generated method stub

  }

  private static int getInput() {
    // TODO Auto-generated method stub
    return 1;
  }
}
