package cases;

@SuppressWarnings("unused")
public class TwoCallerFieldTarget {
	public static void inner(A a, A b, A c){
		A innerParam1 = a;
		A innerParam2 = b;
		A innerParam3 = c;
		b.f = a.f;
		int x = 0;
		 x++;
	}
	
	public static void outer1(){
		A param1 = new A();
		A param2 = param1;
		A param3 = new A();
		inner(param1, param2, param3);
		A query1 = param3.f;
		//param3.f ~ param3.f
		A query2 = param1;
	}
	
	public static void outer2(){
		A param3 = new A();
		A param2 = param3;
		A param1 = new A();
		inner(param1, param2, param3);
		A query1 = param3.f;
		//param3.f ~ param2.f ~ param1.f
		A query2 = param1;
	}
	public static void main(String...args){
		outer1();
		outer2();
	}
}
