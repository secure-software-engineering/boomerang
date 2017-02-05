package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;
import test.core.utils.Query;
import test.core.utils.ResultObject;

@SuppressWarnings("rawtypes")
public class ParameterContinueTest  extends AliasTest{
	
	@Test
	public void parameterContinueTestAndAllContextRequester(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ParameterContinue: cases.A inner(cases.A)>")
						.askForLocalAtStmt(4, "z", andExpect("<cases.ParameterContinue: void outer()>", 2, "z","a"),
								andExpect("<cases.ParameterContinue: void simpleMeetingPointOuter()>", 2, "z","a")));
				

				res.add(inMethod("<cases.ParameterContinue: void outer()>")
						.askForLocalAtStmt(11, "u", andExpect(2, "u", "d[f]","$r1[f]", "$r0","h", "$r2")));

				//				andExpect(6, "u","$r1[f]")
				return res;
			}
		});
	}
	@Test
	public void meetingPointInsideMethod(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.ParameterContinue: cases.A inner(cases.A)>").
						askForLocalAtStmt(4,  "z", andExpect(1, "z","a")));
			

				res.add(inMethod("<cases.ParameterContinue: void outer()>").
						askForLocalAtStmt(11,  "u", andExpect(2, "u", "d[f]","$r1[f]", "$r0","h", "$r2")));

				//				andExpect(6, "u","$r1[f]")
				return res;
			}
		});
	}
	
	@Test
	public void simpleMeetingPointInsideMethod(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ParameterContinue: cases.A inner(cases.A)>");
				query.addQuery(new Query(4,  "z", new ResultObject(1, "z","a")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
	
				query = new MethodQueries("<cases.ParameterContinue: void simpleMeetingPointOuter()>");
				query.addQuery(new Query(7,  "f",
						new ResultObject(2, "d", "f", "e", "$r0")));
				res.add(query);
				return res;
			}
		});
	}
	@Test
	public void simpleMeetingPointInsideMethodOtherVariable(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				MethodQueries query = new MethodQueries("<cases.ParameterContinue: cases.A inner(cases.A)>");
				query.addQuery(new Query(4,  "a", new ResultObject(1, "z","a")));
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(query);
	
				query = new MethodQueries("<cases.ParameterContinue: void simpleMeetingPointOuter()>");
				query.addQuery(new Query(7,  "f",
						new ResultObject(2, "d", "f", "e", "$r0")));
				res.add(query);
				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.ParameterContinue";
	}
	
}
