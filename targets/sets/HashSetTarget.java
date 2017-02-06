package sets;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class HashSetTarget {
	public static void main(String... args){
		hashSetTest1();
	}
	private static void hashSetTest1() {
		String a = new String("A");
		String b = a;
		Set<String> set = new HashSet<>();
		set.add(a);
		
		String target = set.iterator().next();
	}
	
}
