package cases;

@SuppressWarnings("unused")
public class RequestContextTarget {
	public static void main(String...args){
		doGet();
		c1();
		a1();
		outerObject();
		definition();
		definition2();
		definitionTriple();
		def();
	}
    public static void doGet(){
       	StringBuffer buf = new StringBuffer("abc"); 
       	foo(buf, buf);
    }
    
    public static void foo(StringBuffer buf, StringBuffer buf2){
    	StringBuffer x = buf2;
    	System.out.println(x);
	}
    
    
    
    public static void c1(){
    	A aInC1 = new A();
    	A bInC1 = aInC1;
    	
    	c2(aInC1,bInC1);
    }

	private static void c2(A aInC2, A bInC2) {
		c3(aInC2,aInC2);
	}

	private static void c3(A aInC3, A bInC3) {
		A x = aInC3.f;
	}
	
	public static void a1(){
    	A eInA1 = new A();
    	A fInA1 = eInA1;
    	
    	a2(fInA1,eInA1);
    }

	private static void a2(A fInA2, A gInA2) {
		c2(fInA2,gInA2);
	}

	public static void definition() {
		A alias1 = new A();
		A alias2 = alias1;
		rec(alias1, alias2);
	}

	private static void rec(A alias1, A alias2) {
		int x = 1;
		if(x>1){
			A e = alias1;
			rec(alias1,alias2);
		} else{
			recTermination(alias1,alias2);
		}
	}
	

	private static void recTermination(A alias1Final, A alias2Final) {
		A x = alias1Final.f;
	}

	public static void definition2() {
		A alias1 = new A();
		A alias2 = new A();
		rec2(alias1, alias2);
	}

	private static void rec2(A alias1, A alias2) {
		int x = 1;
		if(x>1){
			A aliased = alias1;
			rec2(alias1,aliased);
		} else{
			recTermination2(alias1,alias2);
		}
	}
	

	private static void recTermination2(A alias1Final, A alias2Final) {
		A x = alias1Final.f;
	}
	

	public static void definitionTriple() {
		A alias1 = new A();
		A alias2 = alias1;
		call(alias1, alias2);
	}
	private static void call(A alias1, A alias2) {
		A alias3 = alias2;
		recTerminationTriple(alias1, alias2, alias3);
	}

	private static void recTerminationTriple(A alias1Final, A alias2Final, A alias3Final) {
		A x = alias1Final.f;
	}
	

	public static void outerObject() {
		A d = new A();
		inner(d);
	}
	private static A inner(A a) {
		A z = a;
		return z;
	}

	public static void def() {
		A alias1 = new A();
		A alias2 = alias1;
		recTerminationDouble(alias1, alias2);
	}

	
	private static void recTerminationDouble(A alias1Final, A alias2Final) {
		alias1Final.f = new A();
	}
}
