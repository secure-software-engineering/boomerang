package cases;

@SuppressWarnings("unused")
public class PaperCase {
	private static class A{
		String f = new String("AllocationSite");
		String g;
	}
	
	public static void main(String[] args){
		bar();
	}

	private static void bar() {
		int x=0;
		A a = new A();
		A b = new A();
		A c = b;
		foo(a,b);
		if(x > 1){
			String h = b.f;
		}else{
			String e = b.f;
		}
	}

	private static void foo(A k, A l) {
		String x = k.f;
		l.f=x;
	}
}
