package test.fields;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class StrongUpdate extends AbstractBoomerangTest{
	@Test
	public void strongUpdateWithField(){
		A a = new A();
		a.field = new Object();
		A b = a;
		b.field = new AllocatedObject();
		Object alias = a.field;
		Object query = alias; 
	}
	
	private class A{
		Object field;
	}
}
