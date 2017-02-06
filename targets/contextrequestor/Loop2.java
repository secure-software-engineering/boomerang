package contextrequestor;

import cases.A;

@SuppressWarnings("unused")
public class Loop2 implements ILoop{
	A d = new A();
	int y = 3;
	@Override
	public void loop() {
		if(y > 3)
			loop();
		A h = d.f;
	}
	
}
