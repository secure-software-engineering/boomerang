package cases;

public class NotIntoCallTarget {
	public static void main(String[] args){
		outer();
	}

	private static void outer() {
		A h = new A();
		A d = new A();
		d.f = inner(h);
	}

	private static A inner(A a) {
		A z = a;
		return z;
	}
}
