package contextrequestor;

import cases.A;

@SuppressWarnings("unused")
public class Loop1 implements ILoop {
	A a = new A();
	int x = 3;
	@Override
	public void loop() {
		if(x>0)
			loop();
		A x = a.d;
	}

}
