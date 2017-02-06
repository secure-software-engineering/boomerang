package contextrequestor;

@SuppressWarnings("unused")
public class ContextResolve {
	
	private class A{
		B b;
	}
	private class A1{
		B b = null;
	}
	private class B{
		C c;
	}
	private class C{
		String s = new String("t");
	}
	
	public static void main(String...args){
		ContextResolve c = new ContextResolve();
		c.test1();
		c.test2();
		c.test2Null();
		c.test3();
		c.test4();
		c.test5(2);
	}

	private void test4() {
		A a = new A();
		A b = a;
		level1test4(a,b);
	}

	private void level1test4(A a, A b) {
		A c = a;
		b.b = new B();
		level2test4(a,b,c);
	}
	private void level2test4(A a, A b, A c) {
		B x = a.b;
	}
	
	private void test3() {
		A a_l0 = new A();
		A b_l0 = a_l0;
		level1(a_l0,b_l0);
	}

	private void level1(A a_l1, A b_l1) {
		A c_l1 = a_l1;
		level2(a_l1,b_l1,c_l1);
	}

	private void level2(A a, A b, A c) {
		a.b = new B();
		B x = a.b;
	}

	private void test2() {
		A a1outer = new A();
		A a2outer = a1outer;
		fooRedefine(a1outer, a2outer);
	}

	private void fooRedefine(A a1, A a2) {
		a2.b = new B();
		B x = a1.b;
		
	}
	
	private void test2Null() {
		A1 a1 = new A1();
		A1 a2 = a1;
		fooRedefineNull(a1, a2);
	}

	private void fooRedefineNull(A1 a1, A1 a2) {
		a2.b = new B();
		B x = a1.b;
	}

	public void test1() {
		A a1 = new A();
		A a2 = new A();
		a1.b = new B();
		a2.b = a1.b;
		foo(a1, a2);
	}

	private void foo(A a1, A a2) {
		a1.b.c = new C();
		String x = a1.b.c.s;
	}	

	private void test5(int x) {

		X c = null;
		if(x > 1)
			c = new Y();
		else
			c = new Z();	
		inner(c);
	}

	private void inner(X x){
		A h = x.a;
	}
	private class Y extends X{
		
	}
	private class Z extends X{
		
	}
	private class X{
		A a = new A();
	}
}
