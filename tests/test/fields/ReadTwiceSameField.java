package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ReadTwiceSameField extends AbstractBoomerangTest {
	@Test
	public void recursiveTest() {
		Container a = new Container();
		Container c = a.d;
		Container alias = c.d;
		queryFor(alias);
	}
	
	private class Container{
		Container d;
		Container(){
			if(staticallyUnknown())
				d = new Alloc();
			else 
				d = null;
		}
		
	}
	private class Alloc extends Container implements AllocatedObject{
		
	}
	 
}

