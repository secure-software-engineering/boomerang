package cases;

@SuppressWarnings("unused")
public class NullAliasingTarget {

	public static void main(String[] args){
		nullAliasing();
		new NullAliasingTarget().returnNull();
	}
	private void returnNull() {
		Object o = getNull();
		Object v = o;
	}
	private static void nullAliasing() {
		A a = getA();
		A b = a;
		b.c = new A(); 
	}
	private static A getA() {
		return null;
	}
	private Object getNull(){
		return null;
	}
}
