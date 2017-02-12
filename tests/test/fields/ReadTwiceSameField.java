package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ReadTwiceSameField extends AbstractBoomerangTest {
	@Test
	public void recursiveTest() {
		Alloc a = new Alloc();
		Alloc c = a.d;
		Alloc y = c.d;
		queryFor(y);
	}
	
	private class Alloc extends AllocatedObject{
		Alloc d = new Alloc();
	}
	 
}

