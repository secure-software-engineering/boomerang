package cases;

@SuppressWarnings("unused")
public class NonDistributive {
	private class B {
		C f = new C();
	}
	private class C{}
	private void outer() {
		B b1 = new B();
		B b2 = b1;
		b1.f =  new C();
		C x = b2.f;
	}
	
	public static void main(String...args){
		NonDistributive nonDistributive = new NonDistributive();
		nonDistributive.outer();
	}
}
