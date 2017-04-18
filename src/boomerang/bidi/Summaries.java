package boomerang.bidi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IPathEdge;
import boomerang.ifdssolver.ISummaries;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;


public class Summaries implements ISummaries<Unit, SootMethod, AccessGraph> {
	private BoomerangContext context;
	private Map<SootMethod, IPerMethodSummary> methodToStartFact = new HashMap<>();

	public Summaries(BoomerangContext c){
		this.context = c;
	}
	@Override
	public void addEndSummary(SootMethod m, IPathEdge<Unit, AccessGraph> edge) {
		if(!storeAsSummary(m,edge))
			return;
		IPerMethodSummary perMethodSummary = getOrCreate(m);
		perMethodSummary.addEndSummary(edge);
	}

	@Override
	public Collection<IPathEdge<Unit, AccessGraph>> endSummary(SootMethod m,
			Pair<Unit,AccessGraph> d3) {
		IPerMethodSummary perMethodSummary = methodToStartFact.get(m);
		if(perMethodSummary == null)
			return Collections.emptySet();
		return perMethodSummary.endSummary(d3);
	}


	public boolean storeAsSummary(SootMethod method,
			IPathEdge<Unit, AccessGraph> edge) {
		AccessGraph factAtTarget = edge.factAtTarget();
		if(factAtTarget.isStatic())
			return true;
    return BoomerangContext.isParameterOrThisValue(method, factAtTarget.getBase())
        || context.isReturnValue(method, factAtTarget.getBase());
	}
	
	private IPerMethodSummary getOrCreate(SootMethod m){
		IPerMethodSummary perStartFactSummary = methodToStartFact.get(m);
		if(perStartFactSummary == null){
      perStartFactSummary = new PerMethodSummary();
			methodToStartFact.put(m, perStartFactSummary);
		}
		return perStartFactSummary;
	}
	@Override
	public void clear() {
		methodToStartFact.clear();
	}
	
}
