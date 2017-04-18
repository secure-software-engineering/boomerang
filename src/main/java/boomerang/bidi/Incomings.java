package boomerang.bidi;

import heros.solver.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IIncomings;
import boomerang.ifdssolver.IPathEdge;
import soot.SootMethod;
import soot.Unit;

public class Incomings implements IIncomings<Unit, SootMethod, AccessGraph> {
	
	private Map<SootMethod, IPerMethodIncomings> methodToInc = new HashMap<>();
	@Override
	public Collection<IPathEdge<Unit, AccessGraph>> incoming(Pair<Unit,AccessGraph> startNode, SootMethod m) {
		IPerMethodIncomings perMethodIncomings = methodToInc.get(m);
		if(perMethodIncomings == null)
			return Collections.emptySet();
		return perMethodIncomings.getIncomings(startNode);
	}

	@Override
	public boolean addIncoming(SootMethod callee, Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
		IPerMethodIncomings perMethod = getOrCreate(callee);
		return perMethod.addIncoming(pair,pe);
	}

	private IPerMethodIncomings getOrCreate(SootMethod m){
		IPerMethodIncomings perMethodIncomings = methodToInc.get(m);
		if(perMethodIncomings == null){
			perMethodIncomings = new PerMethodIncomings();
			methodToInc.put(m, perMethodIncomings);
		}
		return perMethodIncomings;
	}
	
	@Override
	public String toString() {
		return methodToInc.toString();
	}

	@Override
	public void clear() {		
//		methodToInc.clear();
//		methodToInc = null;
	}
}
