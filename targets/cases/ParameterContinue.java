package cases;

@SuppressWarnings("unused")
public class ParameterContinue {

	public static void main(String[] args){
		outer();
		simpleMeetingPointOuter();
	}

	private static void outer() {
		A h = new A();
		A d = new A();
		d.f = inner(h);
		A u = d.f;
	}

	private static A inner(A a) {
		A z = a;
		int x = 1;
		x++;
		return z;
	}
	private static void simpleMeetingPointOuter() {
		A d = new A();
		A e = inner(d);
		A f = e;
	}
}
