package contextrequestor;

public class SCCThisTarget {

  public static void main(String... args) {
    ILoop c;

    if (args[0].contains("0")) {
      c = new Loop1();
    } else {
      c = new Loop2();
    }
    c.loop();
  }
}
