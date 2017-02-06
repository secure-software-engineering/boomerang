package cases;

@SuppressWarnings("unused")
public class FieldReadFieldWriteTarget {
	public static void main(String[] args){
		outer();
		fieldRead();
		fieldWrite();
		longAccessPath();
		accessPathLength3();
	}

	private static class D{
		D b;
		String c;
	}
	private static void outer() {
		D d = new D();
		D e = new D();
		e.b = new D();
		
		D a = e;
		
		a.b = d;
		
		D r = a.b;
		
		String s = new String("taint");
		r.c = s;
		String x = r.c;
	}
	
	private static void fieldRead(){
		A e = new A();
		A x = e.c;
	}
	private static void fieldWrite(){
		A e = new A();
		e.c = new A();
	}
	
	private static void longAccessPath(){
		A a = new A();
		a.c = new A();
		A e = new A();
		e.c = a.c;
		a.c.d = new A();
		a.c.d.f = new A();
		A x = a.c.d.f;
	}
	private static void accessPathLength3(){
		A a = new A();
		a.c = new A();
		A e = new A();
		e.c = a.c;
		a.c.d = new A();
		A x = a.c.d;
	}
}
