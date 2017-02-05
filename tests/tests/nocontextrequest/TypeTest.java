package tests.nocontextrequest;

import static test.core.utils.Helper.andExpect;
import static test.core.utils.Helper.inMethod;

import java.util.ArrayList;

import org.junit.Test;

import test.core.AliasTest;
import test.core.utils.IQueryHandler;
import test.core.utils.MethodQueries;

@SuppressWarnings("rawtypes")
public class TypeTest extends AliasTest {
	MethodQueries q1 = inMethod("<cases.TypeTarget$Subclass1: void bar()>").
	askForLocalAtStmt(4, "this", 
			andExpect("<cases.TypeTarget: void test1(java.lang.String[])>",5,"this"));
	MethodQueries q2 = inMethod("<cases.TypeTarget$Subclass2: void bar()>").
			askForLocalAtStmt(4, "this", 
					andExpect("<cases.TypeTarget: void test1(java.lang.String[])>",9,"this"));
	MethodQueries q3 = inMethod("<cases.TypeTarget$Base: void bar()>").
			askForLocalAtStmt(3, "this", 
					andExpect("<cases.TypeTarget: void test1(java.lang.String[])>",9,"this"),
					andExpect("<cases.TypeTarget: void test1(java.lang.String[])>",5,"this"));
					
	
	@Test
	public void test1(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(q1);

				return res;
			}
		});
	}
	@Test
	public void test2(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(q2);

				return res;
			}
		});
	}
	@Test
	public void test3(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(q3);

				return res;
			}
		});
	}
	@Test
	public void both(){
		runAnalysis(true, new IQueryHandler() {
			@Override
			public ArrayList<MethodQueries> queryAndResults() {
				ArrayList<MethodQueries> res = new ArrayList<>();
				res.add(q1);
				res.add(q2);
				res.add(q3);

				return res;
			}
		});
	}
	
	@Override
	public String getTargetClass() {
		return "cases.TypeTarget";
	}
}
