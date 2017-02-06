package sets;

import java.util.TreeSet;

@SuppressWarnings("unused")
public class TreeSetTarget {
  public static void main(String... args) {
    treeSet1();
    // new TreeSetTarget().fixAfterInsertion(new Entry());
    new TreeSetTarget().fixAfterInsertion(new Entry());
    new TreeSetTarget().myRotateRight(new Entry());
    new TreeSetTarget().callMyRotateRightTwice(new Entry());
    new TreeSetTarget().callMyRotateRightOnce(new Entry());
    new TreeSetTarget().callMyRotateRightWithLoop(new Entry());
    new TreeSetTarget().callMyRotateRightWithLoopTwice(new Entry());
    new TreeSetTarget().explosion(new Entry());
    new TreeSetTarget().explosion2(new Entry());
    new TreeSetTarget().loopExplosion(new Entry());
  }


  private static void treeSet1() {
    String tainted = new String("");
    TreeSet<String> set = new TreeSet<String>();
    set.add("neutral");
    set.add(tainted);

    String x = set.last();
  }

  private static class Entry {
    Entry parent;
    Entry left;
    Entry right;
  }

  /** From CLR */
  private void fixAfterInsertionORIG(Entry x) {

    while (x != null) {
      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
        Entry y = rightOf(parentOf(parentOf(x)));
        if (y != null) {
          x = parentOf(parentOf(x));
        } else {
          if (x == rightOf(parentOf(x))) {
            x = parentOf(x);
            // rotateLeft(x);
          }
          // rotateRight(parentOf(parentOf(x)));
        }
      } else {
        Entry y = leftOf(parentOf(parentOf(x)));
        if (y != null) {
          x = parentOf(parentOf(x));
        } else {
          if (x == leftOf(parentOf(x))) {
            x = parentOf(x);
            rotateRight(x);
          }
          // rotateLeft(parentOf(parentOf(x)));
        }
      }
    }
  }

  private void fixAfterInsertion(Entry x) {

    while (x != null) {
      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
        x = parentOf(parentOf(x));
        rotateRight(x);
      } else {
        Entry y = leftOf(parentOf(parentOf(x)));
        if (y != null) {
          x = parentOf(parentOf(x));
        } else {
          x = parentOf(x);
          rotateRight(x);
        }
      }
    }
  }

  /** From CLR */
  private void rotateRight(Entry p) {
    if (p != null) {
      Entry l = p.left;
      p.left = l.right;
      if (l.right != null)
        l.right.parent = p;
      l.parent = p.parent;
      if (p.parent.right == p)
        p.parent.right = l;
      else
        p.parent.left = l;
      l.right = p;
      p.parent = l;
    }
  }

  /** From CLR */
  private void explosion(Entry p) {
    Entry c;
    if (p != null) {
      c = p.left;
    }
    if (p != null) {
      c = p.right;
    }
    if (p != null) {
      c = p.parent;
    }
  }

  private void explosion2(Entry p) {
    Entry c = p;
    if (p != null) {
      c.left = p;
    }
    if (p != null) {
      c.right = p;
    }
    if (p != null) {
      c = p.left;
    }
    if (p != null) {
      c = p.right;
    }
  }


  private void loopExplosion(Entry entry) {
    while (entry.parent != null)
      explosion2(entry);

  }

  private void callMyRotateRightTwice(Entry entry) {

    myRotateRight(entry);
    myRotateRight(entry);
    Entry x = entry.parent;
  }

  private void callMyRotateRightOnce(Entry entry) {
    myRotateRight(entry);
  }

  private void callMyRotateRightWithLoop(Entry entry) {
    while (entry.parent != null)
      myRotateRight(entry);
  }

  private void callMyRotateRightWithLoopTwice(Entry entry) {
    while (entry.parent != null) {
      myRotateRight(entry);
    }
    while (entry.parent != null) {
      myRotateRight(entry);
    }
  }

  private void myRotateRight(Entry p) {
    if (p != null) {
      Entry l = p.left;
      p.left = l.right;
      if (l.right != null)
        l.right.parent = p;
    }
    int x = 0;
  }

  private static Entry parentOf(Entry p) {
    return (p == null ? null : p.parent);
  }

  private static Entry leftOf(Entry p) {
    return (p == null) ? null : p.left;
  }

  private static Entry rightOf(Entry p) {
    return (p == null) ? null : p.right;
  }
}
