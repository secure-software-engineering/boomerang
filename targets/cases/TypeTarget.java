package cases;

public class TypeTarget {
	public static void main(String...args){
		new TypeTarget().test1(args);
	}
	private void test1(String[] args) {
		Base el1 = args != null ? new Subclass1() : new Subclass2();
		el1.foo();
	}
	public class Base{
		void foo(){
			bar();
		}
		void bar(){
			
		};
	}
	public class Subclass1 extends Base{
		void foo(){
			super.foo();
		}
		void bar(){
			super.bar();
		};
	}
	public class Subclass2 extends Base{
		void foo(){
			super.foo();
		}		
		void bar(){
			super.bar();
		};
	}
}
