package arrays;


public class ArrayTarget1 {
  public static void main(String... args) {
    File[] files = new File[3];
    for (int i = 0; i < 3; i++) {
      files[i] = new File();
      files[i].open();
    }
    File a = files[1];
    a.close();
  }
}
