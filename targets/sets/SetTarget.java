package sets;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class SetTarget {
  public static void main(String... args) {
    linkedHashSetTest1();
    hashSetTest1();
    test2();
    new SetTarget().iteratorTest();
  }

  private static void linkedHashSetTest1() {
    String a = new String("A");
    String b = a;
    Set<String> set = new LinkedHashSet<>();
    set.add(a);

    String t = set.iterator().next();
  }

  private static void hashSetTest1() {
    String a = new String("A");
    String b = a;
    Set<String> set = new HashSet<>();
    set.add(a);

    String t = set.iterator().next();
  }

  private static void test2() {
    String a = new String("A");
    String b = new String("B");
    Set<String> set = new LinkedHashSet<>();
    set.add(a);

    String t = set.iterator().next();
  }

  public void iteratorTest() {
    HashSet<String> set = new HashSet<String>();
    String a = new String();
    String c = null;
    String b = new String();
    set.add(a);
    set.add(b);
    for (String i : set) {
      c = i;
      break;
    }

    System.out.println(c);
  }

}
