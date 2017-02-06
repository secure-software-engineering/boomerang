package cases;

@SuppressWarnings("unused")
public class StaredAccessPathTarget {
	public class A{
		B b;
	}
	public class B{
		C c1;
		C c2;
	}
	public class C{}
	public static void main(String... args){
		StaredAccessPathTarget t = new StaredAccessPathTarget();
		t.test1();
	}

	private void test1() {
		C c1 = new C();
		C c2 = new C();
		B b = new B();
		b.c1 = c1;
		b.c2 = c2;
		A a = new A();
		a.b = b;
		
		B e = a.b;
		C q = e.c1;
	}
}
