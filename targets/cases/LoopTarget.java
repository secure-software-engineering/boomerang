package cases;

public class LoopTarget {
	static int field = 4;
	  public static void main(String... args) {
	    new LoopTarget().test1();
	  }

	private  void test1() {
		Node x = new Node();
		Node p = new Node();
		while(4 == field){
			if(4 == field){
				x.left.right = p;
				
			}else{
				x.right.left = p;
			}
			p = x;
		}
		Node t;
		if(4 == field){
			t = x.left.right;
			
		}else{
			t = x.right.left;
		}
		Node h = t;
	}
	
	private class Node{
		Node left = new Node();
		Node right = new Node();
	}
}
