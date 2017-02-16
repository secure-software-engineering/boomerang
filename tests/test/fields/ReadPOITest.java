package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ReadPOITest extends AbstractBoomerangTest {
	private class A{
		Alloc b;
	}
	
	
	@Test
	public void indirectAllocationSite(){
		A a = new A();
		A e = a;
		e.b = new Alloc();
		Alloc query = a.b;
		queryFor(query);
	}
	private class Alloc implements AllocatedObject{};
}
