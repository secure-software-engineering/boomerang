package test.core.utils;


public class ResultObject {
	private int stmtNo;
	private String[] expectedresult;
	private String accessPath;
	private String method;
	private boolean isEmpty = false;
	private boolean isNonEmpty = false;
	public ResultObject(boolean nonEmpty){
		if(nonEmpty){
			isNonEmpty = true;
		} else{
			isEmpty = true;
		}
	}
	public ResultObject(int stmtNo, String... expectedResults){
		this.setStmtNo(stmtNo);
		this.setExpectedresult(expectedResults);
	}
	public ResultObject(String accessPath,  String... expectedResults) {
		this.setAccessPath(accessPath);
		this.setExpectedresult(expectedResults);
	}
	public ResultObject(String method, int stmtNo, String... expectedResults){
		this.setStmtNo(stmtNo);
		this.setExpectedresult(expectedResults);
		this.setMethod(method);
	}
	public String[] getExpectedresult() {
		return expectedresult;
	}
	public void setExpectedresult(String[] expectedresult) {
		this.expectedresult = expectedresult;
	}
	public int getStmtNo() {
		return stmtNo;
	}
	public void setStmtNo(int stmtNo) {
		this.stmtNo = stmtNo;
	}
	public String getAccessPath() {
		return accessPath;
	}
	public void setAccessPath(String accessPath) {
		this.accessPath = accessPath;
	}
	
	public boolean hasAccessPath(){
		return this.accessPath != null;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public boolean isEmpty(){
		return isEmpty;
	}
	
	public boolean isNonEmpty(){
		return isNonEmpty;
	}
}
