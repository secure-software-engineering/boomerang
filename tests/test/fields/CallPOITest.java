package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class CallPOITest extends AbstractBoomerangTest {
	private class A{
		//TODO Test fails if we remove "= null" here.
		B b = null;
	}
	private class B{
		C c;
	}
	private class C extends AllocatedObject{
		
	}
	
	
	@Test
	public void indirectAllocationSite(){
		A a = new A();
		allocation(a);
		C alias = a.b.c;
		queryFor(alias);
	}


	private void allocation(A a) {
		B intermediate = a.b;
		C d = new C();
		intermediate.c = d;
	}
}
