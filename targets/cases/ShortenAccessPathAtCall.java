package cases;

@SuppressWarnings("unused")
public class ShortenAccessPathAtCall {
	public class A2{
		String f = new String("Alloc");
	}
	public class B2{
		A2 a;
	}
	public static void main(String...args){
		test1();
	}
	private static void test1() {
		A2 a = new ShortenAccessPathAtCall().new A2();
		B2 b = new ShortenAccessPathAtCall().new B2();
		inner(a,b);
		String x = a.f;
	}
	private static void inner(A2 a, B2 b) {
		b.a = a;
	}
	
}
