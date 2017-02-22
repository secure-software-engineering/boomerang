package boomerang.pointsofindirection;

import java.util.Arrays;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.ifdssolver.IFDSSolver.PropagationType;
import boomerang.ifdssolver.PathEdge;
import soot.Unit;

public class BackwardAliasCallback extends AliasCallback{
	private AccessGraph source;
	private WrappedSootField[] toAppend;
	private Unit target;
	public BackwardAliasCallback(AccessGraph source, Unit target, WrappedSootField[] toAppend, BoomerangContext context) {
		super(context);
		assert toAppend.length > 0;
		this.source = source;
		this.target = target;
		this.toAppend = toAppend;
	}
	public void newAliasEncountered(PointOfIndirection poi,AccessGraph alias){
		if(alias.hasSetBasedFieldGraph())
			return;
		if(!executed.add(alias))
			return;
		if(!alias.canAppend(toAppend[0]))
			return;
		PathEdge<Unit, AccessGraph> edge = new PathEdge<Unit,AccessGraph>(null,source,target,alias.appendFields(toAppend));
		context.getBackwardSolver().inject(edge, PropagationType.Normal);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + Arrays.hashCode(toAppend);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BackwardAliasCallback other = (BackwardAliasCallback) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (!Arrays.equals(toAppend, other.toAppend))
			return false;
		return true;
	}
}
