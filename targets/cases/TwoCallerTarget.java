package cases;

@SuppressWarnings("unused")
public class TwoCallerTarget {
	public static void inner(A a, A b, A c){
		A d = a;
		A h = a;
	}
	
	public static void outer1(){
		A param1 = new A();
		A param2 = param1;
		A param3 = new A();
		inner(param1, param2, param3);
		A query1 = param3;
		A query2 = param1;
	}
	
	public static void outer2(){
		A param3 = new A();
		A param2 = param3;
		A param1 = new A();
		inner(param1, param2, param3);
		A query1 = param3;
		A query2 = param1;
	}
	
	public static void main(String...args){
		outer1();
		outer2();
	}
}
