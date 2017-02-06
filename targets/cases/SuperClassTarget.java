package cases;

@SuppressWarnings("unused")
public class SuperClassTarget {
	private class C{
		
		public void alias(A a) {
			a.d = new A();
		}
	}
	private class B extends C{
		public void alias(A a){
			a.c = new A();
			super.alias(a);
		}
	}
	
	public void superTest(){
		B b = new B();
		A a = new A();
		a.c = new A();
		a.d = new A();
		a.f = new A();
		A e = a;
		b.alias(a);
		A x = a.c;
		A y = a.d;
		A z = a.f;
	}
	

	public static void main(String...args){
		SuperClassTarget superClassTarget = new SuperClassTarget();
		superClassTarget.superTest();
	}
}

