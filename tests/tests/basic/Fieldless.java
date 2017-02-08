package tests.basic;

import org.junit.Test;

import test.core.selfrunning.AbstractTest;
import test.core.selfrunning.AllocatedObject;
import tests.basic.Fieldless.Allocation;

@SuppressWarnings("unused")
public class Fieldless extends AbstractTest{
	
	public class Allocation extends AllocatedObject{

	}

	@Test
	public void simpleAssignents(){
		Object alloc1 = new Allocation();
		Object alias1 = alloc1;
		Object query = alias1;
	}
	@Test
	public void branchedObjectCreation(){
		Object alias1;
		if(staticallyUnknown())
			alias1 = create();
		else{
			AllocatedObject intermediate = create();
			alias1 = intermediate;
		}
		Object query = alias1;
		Object query1 = query;
	}
	
	public AllocatedObject create(){
		AllocatedObject alloc1 = new AllocatedObject(){};
		return alloc1;
	}
	
	private static boolean staticallyUnknown(){
		return true;
	}
}
