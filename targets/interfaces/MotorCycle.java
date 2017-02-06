package interfaces;

import cases.A;

public class MotorCycle implements Cycle{
	public A kilometer;
	@Override
	public A drive(A km) {
		this.kilometer = km;
		return this.kilometer;
	}

}
