package string;

@SuppressWarnings("unused")
public class StringBuilder {
	public static void main(String...args){
		test1();
		test2();
		test3();
		test4();
		appendTest();
	}

	private static void test1() {
		String a = "a";
		String b = a;
	}
	private static void test2() {
		H a = new H();
		String b = a.toString();
		String c = b;
	}
	private static void test3() {
		H a = new H();
		String b = java.lang.String.valueOf(a);
		String c = b;
	}
	private static void test4() {
		String a = new String("A");
		java.lang.StringBuilder b = new java.lang.StringBuilder(a);
		java.lang.StringBuilder c = b;
	}
	
	private static void appendTest() {
		String a = "a";
		String b = a + ":";
	}
	private static class H{
		public String toString(){
			return "A";
		}
	}
}
