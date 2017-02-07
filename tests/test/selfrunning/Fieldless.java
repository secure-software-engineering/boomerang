package test.selfrunning;

import org.junit.Test;

import test.selfrunning.Fieldless.Allocation;

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
		else
			alias1 = create();
		Object query = alias1;
	}
	
	public Object create(){
		Object alloc1 = new AllocatedObject(){};
		return alloc1;
	}
	
	private static boolean staticallyUnknown(){
		return true;
	}
}
