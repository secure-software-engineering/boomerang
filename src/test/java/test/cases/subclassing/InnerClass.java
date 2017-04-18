package test.cases.subclassing;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class InnerClass extends AbstractBoomerangTest{

	private static class Instance{
		private Object o = new AllocatedObject(){};
		private class Inner{
			private Object getOuter(){
				return Instance.this.o;
			}
		}
	}
	
	@Test
	public void getFromInnerClass(){
		Instance instance = new Instance();
		test.cases.subclassing.InnerClass.Instance.Inner inner = instance.new Inner();
		Object outer = inner.getOuter();
		queryFor(outer);
	}
	

	@Test
	public void getFromInnerClass2(){
		Instance2 instance = new Instance2();
		test.cases.subclassing.InnerClass.Instance2.Inner inner = instance.new Inner();
		inner.setOuter();
		Object outer = inner.getOuter();
		queryFor(outer);
	}
	
	private static class Instance2{
		private Object o;
		private class Inner{
			private Object getOuter(){
				return Instance2.this.o;
			}
			private void setOuter(){
				Instance2.this.o = new AllocatedObject() {
				};
			}
		}
	}
}
