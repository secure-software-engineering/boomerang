package cases;

@SuppressWarnings("unused")
public class StrongUpdate {
	public static void main(String... args){
		new StrongUpdate().test1();
	}
	public class H{
		J b = new J();
		H c;
	}
	public class J{}
	private void test1() {
		H a = new H();
		H c = new H();
		c.c = a;
		a.b = new J();
		J x = c.c.b;
	}
}
