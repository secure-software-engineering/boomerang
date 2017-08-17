package test.cases.context;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;

public class ExampleWithField  extends AbstractBoomerangTest {
	@Test
	public void main() {
		A container = new A();
		container.function2();
	}
}