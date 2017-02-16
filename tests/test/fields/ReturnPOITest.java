package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ReturnPOITest extends AbstractBoomerangTest {
	private class A{
		B b;
	}
	private class B implements AllocatedObject{
	}
	
	
	@Test
	public void indirectAllocationSite(){
		A a = new A();
		A e = a;
		allocation(a);
		B alias = e.b;
		B query = a.b;
		queryFor(query);
	}


	private void allocation(A a) {
		B d = new B();
		a.b = d;
	}
}
