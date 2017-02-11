package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ReadPOITest extends AbstractBoomerangTest {
	private class A{
		AllocatedObject b;
	}
	
	
	@Test
	public void indirectAllocationSite(){
		A a = new A();
		A e = a;
		e.b = new AllocatedObject();
		AllocatedObject query = a.b;
		queryFor(query);
	}
}
