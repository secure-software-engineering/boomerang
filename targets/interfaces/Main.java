package interfaces;

import cases.A;

@SuppressWarnings("unused")
public class Main {
	public static void main(String[] args){
		
		drive();
	}

	private static void drive() {
		Bicycle b = new Bicycle();
		b.drive(new A());
		MotorCycle motorCycle = new MotorCycle();
		int x =1;
		while(x < 10){
			motorCycle.drive(new A());
		}
		
		A y = motorCycle.kilometer;
	}
}
