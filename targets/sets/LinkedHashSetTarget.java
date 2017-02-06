package sets;

import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class LinkedHashSetTarget {
	public static void main(String... args){
		linkedHashSetTest1();
	}
	private static void linkedHashSetTest1() {
		String a = new String("A");
		String b = a;
		Set<String> set = new LinkedHashSet<>();
		set.add(a);
		
		String t = set.iterator().next();
	}
	
}
