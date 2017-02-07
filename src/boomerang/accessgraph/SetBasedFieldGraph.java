package boomerang.accessgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetBasedFieldGraph implements IFieldGraph {

	private final Set<WrappedSootField> fields;
	public SetBasedFieldGraph(Set<WrappedSootField> fields) {
		this.fields = fields;
//		assert fields.size() > 1;
	}
	@Override
	public Set<IFieldGraph> popFirstField() {
		Set<IFieldGraph> out = new HashSet<>();
		out.add(this);
		out.add(FieldGraph.EMPTY_GRAPH);
		return out;
	}

	@Override
	public Set<IFieldGraph> popLastField() {
		return popFirstField();
	}

	@Override
	public Collection<WrappedSootField> getEntryNode() {
		return fields;
	}

	@Override
	public WrappedSootField[] getFields() {
		return new WrappedSootField[0];
	}

	@Override
	public IFieldGraph appendFields(WrappedSootField[] toAppend) {
		Set<WrappedSootField> overapprox = new HashSet<>(fields);
		for(WrappedSootField f: toAppend)
			overapprox.add(f);
		return new SetBasedFieldGraph(overapprox);
	}

	@Override
	public IFieldGraph append(IFieldGraph graph) {
		return appendFields(graph.getFields());
	}

	@Override
	public IFieldGraph prependField(WrappedSootField f) {
		Set<WrappedSootField> overapprox = new HashSet<>(fields);
		overapprox.add(f);
		return new SetBasedFieldGraph(overapprox);
	}

	@Override
	public Collection<WrappedSootField> getExitNode() {
		return fields;
	}

	@Override
	public boolean shouldOverApproximate() {
		return false;
	}

	@Override
	public IFieldGraph overapproximation() {
		throw new RuntimeException("Cannot overapproximate the approxmiation anymore");
	}
	
	public String toString(){
		return " **"+ fields.toString() + "**";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
		SetBasedFieldGraph other = (SetBasedFieldGraph) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}

}
