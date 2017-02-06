package cases;

@SuppressWarnings("unused")
public class SubclassTarget {
	
	public static void main(String[] args){
		Class1 cls1 = new SubclassTarget().new Class1();
		Class2 cls2 = new SubclassTarget().new Class2();
		method1(cls1);	
		method1(cls2);
		method2(cls2);
		method2(cls1);
		new SubclassTarget().baseWithField();
		new SubclassTarget().correlatedCalls(args);
	}


	public static void method1(IFace iface) {
		A e = iface.doSomething();
		//e = iface.doMore();
		A x = e.c;
	}

	public static void method2(IFace iface) {
		A e = iface.doSomething();
		e = iface.doMore();
		A x = e.c;
	}

	private interface IFace{
		public A doSomething();
		public A doMore();
	}
	
	private class Class1 implements IFace{
		A fieldClass1;

		@Override
		public A doSomething() {
			fieldClass1 = new A();
			return fieldClass1;
		}

		@Override
		public A doMore() {
			int SHOULDBECLASS1 = 1;
			return fieldClass1;
		}
		
	}
	
	private class Class2 implements IFace{
		A fieldClass2;

		@Override
		public A doSomething() {
			fieldClass2 = new A();
			return fieldClass2;
		}

		@Override
		public A doMore() {
			int SHOULDBECLASS2 = 2;
			return fieldClass2;
		}
		
	}
	private void baseWithField() {
		Subclass subclass = new Subclass();
		String v = new String();
		subclass.foo(v);
	}
	public class Base{
		String f;
		void foo(String a){
			
		}
		void bar(){
			
		}
	}
	public class Subclass extends Base{
		String field1;
		public void foo(String a){
			f = a;
		}
		void bar(){
			field1 = f;
		}
	}
	
	public class Subclass2 extends Base{
		String field2; 
		public void foo(String a){
			f = a;
		}

		void bar(){
			field2 = f;
		}
	}
	
	
	
	

	private void correlatedCalls(String[] args) {
		Base b = null;
		if(args == null) 
			b = new Subclass();
		else
			b = new Subclass2();
		String v = new String();
		b.foo(v);
		b.bar();
	}

	
	
}
