package test.sets;


import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class LinkedLists extends AbstractBoomerangTest{
	@Test
	public void addAndRetrieve(){
		List<Object> set = new LinkedList<Object>();
		AllocatedObject alias = new AllocatedObject();
		set.add(alias);
		Object alias2 = null;
		for(Object o : set)
			alias2 = o;
		Object ir = alias2;
		Object query2 = ir;
		queryFor(query2);
	}
	
	@Override
	protected boolean includeJDK() {
		return true;
	}
}
