package contextrequestor;

@SuppressWarnings("unused")
public class ThisContextResolve {
	
	public static void main(String...args){
		ThisContextResolve t = new ThisContextResolve();
		t.test1();
		t.recursion();
		t.baseAlias();
	}

	private void baseAlias() {
		Inner1 a = new Inner1();
		Inner2 b = new Inner2();
		a.f = b.h;
		level1(a,b);
	}
	

	private void level1(Inner1 a, Inner2 b) {
		A h = a.f.g;
		level2(a,b);
	}
	private void level2(Inner1 a, Inner2 b) {
		A h = a.f.g;
	}

	private class Inner1{
		Inner3 f = new Inner3();
	}
	private class Inner2{
		Inner3 h = new Inner3();
	}
	private class Inner3{
		A g = new A();
	}
	
	private void recursion() {
		A a = new A();
		foo(a,1);
	}

	private void foo(A a, int x) {
		if(x > 0){
			a.f = new A();
		}
		A b = a.f;
		foo(b, x);
		A c = b;
	}

	private void test1() {
		B b = new B();
		b.foo();
	} 
	
	private class A{
		A f;
	}
	private class B{
		String field = new String("alloc");
		public void foo(){
			String c = this.field;
		}
	}
}
