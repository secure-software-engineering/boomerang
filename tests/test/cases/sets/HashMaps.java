package test.cases.sets;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class HashMaps extends AbstractBoomerangTest{
	@Test
	public void addAndRetrieve(){
		Map<Object,Object> set = new HashMap<>();
		AllocatedObject alias = new AllocatedObject(){};
		AllocatedObject alias3 = new AllocatedObject(){};
		set.put(alias,alias3);
		Object alias2 = null;
		for(Object o : set.values())
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
