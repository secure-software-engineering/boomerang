package boomerang.pointsofindirection;

import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.PathEdge;
import heros.solver.Pair;
import soot.Unit;

public class FieldReadCallback extends AliasCallback {

	private AccessGraph mustArrive;
	private Pair<Unit, AccessGraph> startNode;
	private Unit succ;
	private Set<AccessGraph> toContinue;
	private boolean triggered;

	public FieldReadCallback(AccessGraph mustArrive, Pair<Unit, AccessGraph> startNode, Unit succ,
			Set<AccessGraph> toContinue, BoomerangContext context) {
		super(context);
				this.mustArrive = mustArrive;
				this.startNode = startNode;
				this.succ = succ;
				this.toContinue = toContinue;
	}

	@Override
	public void newAliasEncountered(PointOfIndirection poi, AccessGraph alias, AccessGraph aliasOrigin) {
		if(!triggered && alias.equals(mustArrive)){
			triggered = true;
			for(AccessGraph target : toContinue){
				context.getForwardSolver().inject(new PathEdge<Unit,AccessGraph>(startNode.getO2(),succ,target), PropagationType.Normal);
			}
		}

	}

}
