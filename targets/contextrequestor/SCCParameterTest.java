package contextrequestor;

public class SCCParameterTest {
	
	public static void main(String...args){
		ILoopParameter c = (args[0].contains("0") ? new Loop1Parameter() : new Loop2Parameter());
		loop(c);
	}

	private static void loop(ILoopParameter c) {
		c.loop(c);
	}
}
