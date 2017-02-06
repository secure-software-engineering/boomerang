package cases;

@SuppressWarnings("unused")
public class FieldInstanceCheckTarget {
	public static void main(String...args){
		FieldInstanceCheckTarget fieldInstanceCheckTarget = new FieldInstanceCheckTarget();
		fieldInstanceCheckTarget.paramTransferTest();
	}
	public L1 l;
	public void paramTransferTest(){
		String tainted = new String("test");
		l = new L1();
		//taint(tainted, l);
		String x = l.f;
	}
	
	public void taint(String e, L1 m){
		m.f = e;
	}
	
	class L1{
		String f = new String("test");
	}
}
