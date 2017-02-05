package test.core.utils;

import java.util.ArrayList;


public class MethodQueries {
	private String method;
	private ArrayList<Query> queries = new ArrayList<Query> ();
	
	public MethodQueries (String method){
		this.setMethod(method);
	}

	public void addQuery(Query query){
		this.queries.add(query);
	}
	
	public ArrayList<Query> getQueries() {
		return queries;
	}

	public void setQueries(ArrayList<Query> queries) {
		this.queries = queries;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public MethodQueries askForLocalAtStmt(int stmtNumber, String queriedvalue, ResultObject... res){
		this.addQuery(new Query(stmtNumber, queriedvalue, res));
		return this;
	}
}
