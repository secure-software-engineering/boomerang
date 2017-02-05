package test.core.utils;

import boomerang.accessgraph.AccessGraph;
import heros.solver.Pair;
import soot.Unit;



public class Query {
	private int stmtNo;
	private String queriedValue;
	private ResultObject[] expectedresult;
	private Pair<Unit, AccessGraph> query;
	

	public Query(int stmtNumber, String queriedvalue, ResultObject... res){
		this.setStmtNo(stmtNumber);
		this.setQueriedValue(queriedvalue);
		this.setExpectedresult(res);
	}
	public String getQueriedValue() {
		return queriedValue;
	}

	public void setQueriedValue(String queriedvalue) {
		this.queriedValue = queriedvalue;
	}

	public int getStmtNo() {
		return stmtNo;
	}

	public void setStmtNo(int stmt) {
		this.stmtNo = stmt;
	}

	public ResultObject[] getExpectedResults() {
		return expectedresult;
	}

	public void setExpectedresult(ResultObject[] expectedresult) {
		this.expectedresult = expectedresult;
	}
	
	public void setQuery(Pair<Unit, AccessGraph> pair) {
		this.query= pair;
	}
	public Pair<Unit, AccessGraph> getQuery(){
		return this.query;
	}
}
