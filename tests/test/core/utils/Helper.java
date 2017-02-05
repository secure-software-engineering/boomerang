package test.core.utils;


public class Helper {
	public static MethodQueries inMethod(String m){
		return new MethodQueries(m);
	}
	public static ResultObject andExpect(int stmtNo, String... res){
		return new ResultObject(stmtNo, res);
	}
	public static ResultObject andExpect(String method, int stmtNo, String... res){
		return new ResultObject(method,stmtNo, res);
	}
	public static ResultObject andExpect(String ap, String... res){
		return new ResultObject(ap, res);
	}
	public static ResultObject andExpectEmpty(){
		return new ResultObject(false);
	}
	public static ResultObject andExpectNonEmpty(){
		return new ResultObject(true);
	}
}
