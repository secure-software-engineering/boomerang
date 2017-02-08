package tests.basic;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class Fieldless extends AbstractBoomerangTest {

	public class Allocation extends AllocatedObject {

	}

	@Test
	public void simpleAssignents() {
		Object alloc1 = new Allocation();
		Object alias1 = alloc1;
		Object query = alias1;
		queryFor(query);
	}

	@Test
	public void branchedObjectCreation() {
		Object alias1;
		if (staticallyUnknown())
			alias1 = create();
		else {
			AllocatedObject intermediate = create();
			alias1 = intermediate;
		}
		Object query = alias1;
		queryFor(query);
	}

	@Test
	public void branchWithOverwrite() {
		Object alias1 = new Object();
		Object alias2 = new Allocation();
		if (staticallyUnknown()) {
			alias1 = alias2;
			alias2 = new Allocation();
		}

		queryFor(alias2);
	}

	@Test
	public void branchWithOverwriteSwapped() {
		Object alias2 = new Allocation();
		Object alias1 = new Allocation();
		if (staticallyUnknown()) {
			alias2 = alias1;
			alias1 = new Object();
		}

		queryFor(alias2);
	}

	@Test
	public void cast() {
		Allocation alias1 = new Subclass();
		Subclass alias2 = (Subclass) alias1;
		queryFor(alias2);
	}
	
	private class Subclass extends Allocation{}
	
	public AllocatedObject create() {
		AllocatedObject alloc1 = new AllocatedObject() {
		};
		return alloc1;
	}

}
