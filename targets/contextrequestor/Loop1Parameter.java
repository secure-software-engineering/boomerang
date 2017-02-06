package contextrequestor;

import cases.A;

@SuppressWarnings("unused")
public class Loop1Parameter implements ILoopParameter {
	A a = new A();
	@Override
	public void loop(ILoopParameter loop) {
		loop.loop(loop);
		A h = a.c;
	}

}
