package tests.contextrequestor;

import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class SCCParameterTest extends AliasTest{
	
	@Test
	public void sccTest1(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<contextrequestor.Loop1Parameter: void loop(contextrequestor.ILoopParameter)>").
							askForLocalAtStmt(7, "h", 
 andExpectNonEmpty()));

				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "contextrequestor.SCCParameterTest";
	}
}
