package cases;

@SuppressWarnings("unused")
public class ThreeLevelFieldTarget {
  public class Level1 {
    Level2 l1 = new Level2();
    Level2 left;
    Level2 right;
  }
  public class Level2 {
    Level3 l2 = new Level3();
    Level3 right;
    Level3 left;
  }
  public class Level3 {
    Level4 l3;
  }
  public class Level4 {

  }

  public static void main(String... args) {
    ThreeLevelFieldTarget t = new ThreeLevelFieldTarget();
    t.test();
    t.loop();
    t.test2();
    t.test3();
  }

  private void test2() {
    Level1 level1 = new Level1();
    defineL1Left(level1);
    Level3 x = level1.l1.left;
  }

  private void test3() {
    Level1 level1 = new Level1();
    level1.l1 = null;
    defineL1Left(level1);
    Level3 x = level1.l1.left;
  }

  private void defineL1Left(Level1 level1) {
    level1.l1.left = new Level3();
  }

  private void loop() {
    Level1 level1 = new Level1();
    level1.l1 = new Level2();
    loop(level1);
    Level3 x = level1.l1.left;
  }

  private void loop(Level1 level1) {
    if (level1 != null) {
      level1.l1.left = new Level3();
    }
    // if(level1 != null)
    // level1.l1.right = new Level3();
    if (level1 == null)
      loop(level1);
  }

  public void test() {
    Level1 l = new Level1();
    Level2 x = l.l1;
    getField(l);
    Level4 h = l.l1.l2.l3;
    Level4 a = x.l2.l3;
  }

  public void getField(Level1 l) {
    l.l1.l2.l3 = new Level4();
  }
}
