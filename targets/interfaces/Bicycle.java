package interfaces;

import cases.A;

public class Bicycle implements Cycle{
	private A kilometer = new A();
	@Override
	public A drive(A km) {
		return this.kilometer;
	}

}
