package cases;


@SuppressWarnings("unused")
public class RecursiveFieldAccess {

  public static void main(String... args) {
    RecursiveFieldAccess recursiveFieldAccess = new RecursiveFieldAccess();
    test1();
    test2(Integer.parseInt(args[0]));
    test3();
    test4();
    new RecursiveFieldAccess().test5();
    recursiveFieldAccess.treeTest();
    recursiveFieldAccess.separatedTreeTest();
    recursiveFieldAccess.treeTest2();
  }

  private void test5() {
    Tree d = new Tree();
    while (d != null) {
      if (d != null)
        d.left = d;
      if (d != null)
        d.right = d;
    }
    Tree h = d;
  }

  public class C {
    D left = new D();

    public void setLeft(D d) {
      this.left = d;
    }
  }
  public class D {
    C right = new C();
    D inner;

    public void setRight(C c) {
      this.right = c;
    }

    public D getPrev() {
      return right.left;
    }
  }

  public static void test1() {
    C c = new RecursiveFieldAccess().new C();
    D d = new RecursiveFieldAccess().new D();
    d.setRight(c);
    c.setLeft(d);

    D x = c.left;
  }

  public static void test2(int y) {
    C c = new RecursiveFieldAccess().new C();
    D d = new RecursiveFieldAccess().new D();
    while (y < 2) {
      d.setRight(c);
      c.setLeft(d);
      break;
    }

    D x = c.left;
  }

  public static D test3(D d, int y) {
    D h = null;
    if (y < 2) {
      h = new RecursiveFieldAccess().new D();
    } else {
      h = d.getPrev();
    }
    return h;
  }

  public static void test3() {
    D outer = new RecursiveFieldAccess().new D();
    D test3 = test3(outer, 1);
    D x = test3;
  }



  private static void test4() {
    RecursiveFieldAccess scc = new RecursiveFieldAccess();
    A a = new A();
    scc.wrapper(a);
    A x = a.c;
  }


  public void enter(A a) {
    while (true) {
      if (a.c != null) {
        A b = a.c;
        wrapper(b);
      }
      if (a.d != null) {
        a.f = a;
        wrapper(a);
      }
      if (a.f != null)
        break;
    }
  }

  public void wrapper(A a) {
    enter(a);
  }


  public class Tree {
    public Tree left;
    public Tree right;
    public String data;
  }

  public class SeparatedTree {
    public TreeElement left;
    public TreeElement right;
  }

  public class TreeElement {
    public SeparatedTree child;
    public A data;
  }


  public void treeTest() {
    Tree myTree = new Tree();
    myTree.left = new Tree();
    myTree.left.right = new Tree();
    myTree.left.right.left = myTree;
    myTree.data = "tree";
    String x = myTree.data;
  }

  public A separatedTreeTest() {
    SeparatedTree myTree = new SeparatedTree();
    myTree.left = new TreeElement();
    myTree.left.child = new SeparatedTree();
    myTree.left.child.right = new TreeElement();
    myTree.left.child.right.data = new A();
    A x = myTree.left.child.right.data;
    return x;
  }

  public void treeTest2() {
    Tree myTree = new Tree();
    myTree.left = new Tree();
    myTree.left.data = "Test";
    while (true) {
      rotateLeft(myTree);
      if (myTree != null)
        break;
    }
    while (true) {
      rotateRight(myTree);
      if (myTree != null)
        break;
    }
    String x = myTree.right.data;
  }

  private void rotateLeft(Tree myTree) {
    myTree.left = myTree.right;
  }

  private void rotateRight(Tree myTree) {
    myTree.right = myTree.left;
  }
}
