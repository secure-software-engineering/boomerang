package cases;

@SuppressWarnings("unused")
public class ObjectSensitiveComplex {
	static class A{
		String f;
	};
	public static void main(String[] args){
		unbalanced();
		multipleCalls();
		multipleQueries();
		doubleCallStack();
		recursion();
	}

	private static void multipleQueries() {
		A a,b,c,x,y,z;
		a = getSomething();
		x = getSomething();		
		b = a;
		c = identity(b);
		y = identity(x);
		z = y;
	}

	private static void multipleCalls() {
		A e, f, g;
		e = getSomething();
		f = getSomethingBranched();
		g = getSomething();		
	}

	private static void unbalanced() {
		int a = 1;
		int b = 1;
		A c,d,e,f,i,h;
		if(a > 1){
			e = getSomethingBranched();
			d = e;
			h = identity(e);
		} else {
			e = getSomethingBranched();
			i = identity(e);
		}
		f = e;
	}

	private static A identity(A param) {
		A mapped = param;
		return mapped;
	}


	private static A getSomething() {
		A ret = new A();
		return ret;
	}
	private static A getSomethingBranched() {
		int a = 1;
		if(a > 1){
			A ret1 = new A();
			return ret1;
		}
		A ret = new A();
		return ret;
	}
	
	private static void doubleCallStack(){
		A alias = getSomethingWrapped();
	}

	private static A getSomethingWrapped() {
		return identity(getSomething());
	}
	
	private static void recursion(){
		A alloc1 = new A();
		A alias = recursive(alloc1); 
	}
	private static A recursive(A n){
		if(n.f != null){
			return new A();
		}
		return recursive(n);
	}
}
