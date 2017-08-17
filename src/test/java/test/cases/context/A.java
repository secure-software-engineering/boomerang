package test.cases.context;

import test.core.selfrunning.AbstractBoomerangTest;

public  class A {
	Alloc a = new Alloc();
	public Object function1(){
		return this.a;
	}
	public void function2(){
		Object b = function1();
		AbstractBoomerangTest.queryFor(b);
	}
}