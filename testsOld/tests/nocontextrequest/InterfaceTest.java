package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class InterfaceTest  extends AliasTest{
	@Test
	public void interfaceTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<interfaces.Main: void drive()>").
							askForLocalAtStmt(18, "y", 
									andExpect(13,"y","motorCycle[kilometer]","$r4","$r3[kilometer]")));

		
				return res;
			}
		});
	}

	@Override
	public String getTargetClass() {
		// TODO Auto-generated method stub
		return "interfaces.Main";
	}
	
}
