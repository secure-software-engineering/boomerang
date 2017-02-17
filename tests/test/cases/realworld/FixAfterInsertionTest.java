package test.cases.realworld;

import org.junit.Test;

import test.cases.realworld.FixAfterInsertion.Entry;
import test.core.selfrunning.AbstractBoomerangTest;

public class FixAfterInsertionTest  extends AbstractBoomerangTest{

	@Test
	public void main(){
		Entry<Object, Object> entry = new Entry<Object,Object>(null,null,null);
		new FixAfterInsertion<>().fixAfterInsertion(entry);
		Entry<Object, Object> query = entry.parent;
		queryFor(query);
	}
}
