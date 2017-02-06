package contextrequestor;

import cases.A;

@SuppressWarnings("unused")
public class Loop2Parameter implements ILoopParameter {
	A d = new A();
	@Override
	public void loop(ILoopParameter loop) {
		loop.loop(loop);
		A h = d.f;
	}

}
