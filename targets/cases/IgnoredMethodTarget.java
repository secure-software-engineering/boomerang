package cases;

@SuppressWarnings("unused")
public class IgnoredMethodTarget {
	public static void main(String...args){
		equalsTest1();
		equalsTest2();
	}

	private static void equalsTest1() {
		A a1 = new A();
		A b1 = new A();
		
		a1.equals(b1);
		A b2 = b1;
		A a2 = a1;
		String a = "";
		a += "aa";
	}
	private static void equalsTest2() {
		A a1 = new A();
		A b1 = new A();
		
		a1.equals(b1);
		A b2 = b1.f;
		A a2 = a1.d;
	}
}
