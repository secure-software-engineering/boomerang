package cases;

import java.util.Random;
import java.util.Vector;



public class ComplexNextListTarget {

	public static void main(String[] args) {
		simple();
		complex();
		complex2();
	}

	private static void complex2() {
	    try {
	      Vector v1 = new Vector();
	      Vector v2 = new Vector();

	      init(v1, v2);

	      Cell head = new Cell();
	      Cell tail = head;
	      for (int i = 0; i < 100; i++) {
	        tail.next = new Cell();
	        Cell ir = tail.next;
	        tail = ir;
	      }

	      boolean b = (new Random().nextBoolean());
	      if (b) {
	        head.next.v = v1;
	      } else {
	        head.next.v = v2;
	      }
	      head.next.v.firstElement();

	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}

	private static void simple() {
		Cell head = new Cell();
		Cell tail = head;
		for (int i = 0; i < 100; i++) {
			tail.next = new Cell();
			tail = tail.next;
		}	
		Cell test = head.next;
		Cell g = head.next;
	}
	
	private static void complex() {
		try {
			Vector v1 = new Vector();
			Vector v2 = new Vector();

			init(v1, v2);

			Cell head = new Cell();
			Cell tail = head;
			for (int i = 0; i < 100; i++) {
				tail.next = new Cell();
		        Cell ir = tail.next;
		        tail = ir;
			}
			head.next.next.next.next.v = v1;
			head.next.next.next.next.v = v2;
			head.next.next.next.next.v.firstElement();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param v1
	 * @param v2
	 */
	private static void init(Vector v1, Vector v2) {
		v1.add(new Object());
		v2.removeAllElements();
	}

	private static class Cell {
		Cell next = null;

		Vector v;
	}
}
