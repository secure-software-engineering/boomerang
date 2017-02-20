package boomerang.accessgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import soot.Scene;
import soot.SootField;
import soot.Type;

public class SetBasedFieldGraph implements IFieldGraph {

	private Set<WrappedSootField> fields;
	public static Set<WrappedSootField> allFields;

	public SetBasedFieldGraph(Set<WrappedSootField> fields) {
		this(fields, true);
	}

	public SetBasedFieldGraph(Set<WrappedSootField> fields, boolean type) {
		if (!type) {
			this.fields = new HashSet<>();
			for (WrappedSootField f : fields) {
				this.fields.add(new WrappedSootField(f.getField(), null, null));
			}
		} else {
			if (allFields == null)
				allFields = new HashSet<>();
			allFields.addAll(fields);
			this.fields = minimize(allFields);
		}
		// assert fields.size() > 1;
	}

	private Set<WrappedSootField> minimize(Set<WrappedSootField> fields) {
		Map<SootField, Type> fieldsWithTypes = new HashMap<>();
		for (WrappedSootField f : fields) {
			SootField unwrappedField = f.getField();
			if (!fieldsWithTypes.containsKey(f.getField()))
				fieldsWithTypes.put(unwrappedField, unwrappedField.getType());
			else {
				Type a = fieldsWithTypes.get(unwrappedField);
				Type b = f.getType();
				Type commonSuperClass = superType(a, b);
				fieldsWithTypes.put(unwrappedField, commonSuperClass);
			}
		}
		Set<WrappedSootField> out = new HashSet<>();
		for (Entry<SootField, Type> e : fieldsWithTypes.entrySet())
			out.add(new WrappedSootField(e.getKey(), e.getValue(), null));
		return out;
	}

	private Type superType(Type a, Type b) {
		if (a.equals(b))
			return a;
		if (Scene.v().getOrMakeFastHierarchy().canStoreType(a, b)) {
			return b;
		} else if (Scene.v().getOrMakeFastHierarchy().canStoreType(b, a)) {
			return a;
		}
		return a;
		// throw new RuntimeException("Type mismatch?" + a +" and " + b);
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
		for (WrappedSootField f : toAppend)
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
		return this;
		// throw new RuntimeException("Cannot overapproximate the approxmiation
		// anymore");
	}

	public String toString() {
		return " **" + fields.toString() + "**";
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
		} else if (fields.size() != other.fields.size() || !fields.equals(other.fields))
			return false;
		return true;
	}

	@Override
	public IFieldGraph noType() {
		return new SetBasedFieldGraph(fields,false);
	}

}
