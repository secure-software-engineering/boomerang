package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.andExpectNonEmpty;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class FieldReadFieldWriteTest extends AliasTest {
	
	@Test
	public void fieldReadFieldWriteTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldReadFieldWriteTarget: void outer()>").
							askForLocalAtStmt(20, "x", 
									andExpect(14,"x", "a[b,c]", "e[b,c]", "r[c]","d[c]","$r1[b,c]", "$r0[c]", "s","$r3")));

				return res;
			}
		});
	}
	@Test
	public void intraLongAccessPathTest(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldReadFieldWriteTarget: void longAccessPath()>").
askForLocalAtStmt(25, "x", andExpectNonEmpty()
					/*				andExpect(15,"$r1[d,f]","$r2[c,d,f]","$r10[f]","$r6[d,f]","a[c,d,f]","e[c,d,f]","$r4[f]","$r3[d,f]","$r0[c,d,f]","$r5[d,f]","x","$r9[d,f]"),*/
        /*
         * GT andExpect(19,"$r1[d,f]","$r10[f]","$r6[d,f]","$r4[f]","e[c,d,f]","$r3[d,f]","$r7","x",
         * "$r8[f]","$r2[c,d,f]","a[c,d,f]","$r0[c,d,f]","$r5[d,f]","$r9[d,f]") /*
         * andExpect(6,"$r2[c,d,f]","$r10[f]","$r1[d,f]","a[c,d,f]","$r6[d,f]","e[c,d,f]","$r3[d,f]"
         * ,"$r0[c,d,f]","x","$r9[d,f]")
         */));

				return res;
			}
		});
	}
	@Test
	public void intraAccessPathLength3Test(){
		runAnalysis(false, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(inMethod("<cases.FieldReadFieldWriteTarget: void accessPathLength3()>").
askForLocalAtStmt(19, "x", andExpectNonEmpty()
        /*
         * GT andExpect(14,"x","$r6[d]","$r4",
         * "$r5[d]","a[c,d]","$r3[d]","e[c,d]","$r2[c,d]","$r1[d]","$r0[c,d]")
         * /*andExpect(6,"x","$r6[d]","a[c,d]","$r3[d]","e[c,d]","$r2[c,d]","$r1[d]","$r0[c,d]")
         */));

				return res;
			}
		});
	}
	@Override
	public String getTargetClass() {
		return "cases.FieldReadFieldWriteTarget";
	}
}
