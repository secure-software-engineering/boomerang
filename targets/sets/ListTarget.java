package sets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class ListTarget {
	public static void main(String... args){
		test1();
		oneElementAndGetTest();
		twoElementAndGetTest();
		iteratorTest();
		treeSetTest();
		arrayListTest();
		linkedListTest();
	}

	private static void test1() {
		String a = new String("A");
		String b = a;
		List<String> list = new LinkedList<>();
		list.add(a);
		Iterator<String> iterator = list.iterator();
		
		String t = iterator.next();
	}
	private static void iteratorTest() {
		List<String> list = new LinkedList<>();
		Iterator<String> iterator = list.iterator();
		Iterator<String> t = iterator;
	}
	private static void oneElementAndGetTest() {
		String a = new String("A");
		String b = a;
		List<String> list = new LinkedList<>();
		list.add(a);
		
		String t = list.get(0);
	}
	private static void twoElementAndGetTest() {
		String a = new String("A");
		String b = new String("B");
		List<String> list = new LinkedList<>();
		list.add(a);
		list.add(b);
		
		String t = list.get(1);
	}
	private static void treeSetTest() {
		String a = new String("A");
		String b = new String("B");
		Set<String> list = new TreeSet<>();
		list.add(a);
		list.add(b);
		String x = list.iterator().next();
	}
	
	private static void arrayListTest(){
		ArrayList<String> list = new ArrayList<String>();
		String a = new String();
		list.add(a);
		String c = list.get(0);

	}
	
	private static void linkedListTest(){
		LinkedList<A> list = new LinkedList<A>();
		A a = new A();
		A b = new A();
		list.add(a);
		list.add(b);
		A c = list.get(1);
	}
	
}
