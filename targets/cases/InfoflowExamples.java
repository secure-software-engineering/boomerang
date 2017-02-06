package cases;

import other.ConnectionManager;
import other.TelephonyManager;


public class InfoflowExamples {
	private B b1;
	private B b2;
	class A{
		public String b = "Y";
		public String c = "X";
	}
	public class B{
		public A attr;
		
		public B() {
			attr = new A();
		}
		
		public void setAttr(A attr) {
			this.attr = attr;
		}
	}

	private A bar(A a) {
		this.b1.attr = a;
		return this.b2.attr;
	}
	private void foo(B b1, B b2) {
		this.b1 = b1;
		this.b2 = b2;
	}
	public void testAliases() {
		B b = new B();
		A a = new A();
		a.b = TelephonyManager.getDeviceId();
		
		// Create the alias
		foo(b, b);
		String tainted = bar(a).b;
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(tainted);
	}
}
