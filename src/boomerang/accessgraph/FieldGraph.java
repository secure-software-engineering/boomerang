package boomerang.accessgraph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Scene;
import soot.SootField;
import soot.Type;
import toools.collections.Collections;

/**
 * A field graph represents only the of the access graph field accesses. It is a
 * directed graph. Two nodes of the graph are special, the entry and exit node.
 * One can also see the field graph as a Finite State Machine. The inital state
 * is the entry node and the accepting state is the target node. As the Grph
 * Library represents nodes within the graph as integers, we keep a mapping from
 * fields to integer.
 * 
 * @author spaeth
 *
 */
public class FieldGraph implements IFieldGraph {

	final LinkedList<WrappedSootField> fields;
	static FieldGraph EMPTY_GRAPH = new FieldGraph() {
		public String toString() {
			return "EMPTY_GRAPH";
		};
	};

	FieldGraph(WrappedSootField[] fields) {
		assert fields != null && fields.length > 0;
		this.fields = new LinkedList<>(Arrays.asList(fields));
	}

	FieldGraph(WrappedSootField f) {
		assert f != null;
		this.fields = new LinkedList<>();
		this.fields.add(f);
	}

	private FieldGraph(LinkedList<WrappedSootField> fields) {
		this.fields = fields;
	}

	private FieldGraph() {
		this.fields = new LinkedList<>();
	}

	/**
	 * 
	 * @return
	 */
	public Set<IFieldGraph> popFirstField() {
		if (fields.isEmpty())
			return new HashSet<>();
		Set<IFieldGraph> out = new HashSet<>();
		LinkedList<WrappedSootField> newFields = new LinkedList<>(fields);
		newFields.removeFirst();
		if(newFields.isEmpty())
			out.add(FieldGraph.EMPTY_GRAPH);
		else
			out.add(new FieldGraph(newFields));
		return out;
	}

	public WrappedSootField[] getFields() {
		return fields.toArray(new WrappedSootField[] {});
	}

	public IFieldGraph prependField(WrappedSootField f) {
		LinkedList<WrappedSootField> newFields = new LinkedList<>(fields);
		newFields.addFirst(f);
		return new FieldGraph(newFields);
	}

	public Set<IFieldGraph> popLastField() {
		Set<IFieldGraph> out = new HashSet<>();
		if (fields.isEmpty())
			return out;
		LinkedList<WrappedSootField> newFields = new LinkedList<>(fields);
		newFields.removeLast();
		if(newFields.isEmpty())
			out.add(FieldGraph.EMPTY_GRAPH);
		else
			out.add(new FieldGraph(newFields));
		return out;
	}

	public IFieldGraph append(IFieldGraph o) {
		if (o instanceof SetBasedFieldGraph) {
			SetBasedFieldGraph setBasedFieldGraph = (SetBasedFieldGraph) o;
			return setBasedFieldGraph.append(this);
		} else if (o instanceof FieldGraph) {
			FieldGraph other = (FieldGraph) o;
			LinkedList<WrappedSootField> fields2 = other.fields;
			LinkedList<WrappedSootField> newFields = new LinkedList<>(fields);
			newFields.addAll(fields2);
			return new FieldGraph(newFields);
		}
		throw new RuntimeException("Not yet implemented!");
	}

	public IFieldGraph appendFields(WrappedSootField[] toAppend) {
		return append(new FieldGraph(toAppend));
	}

	public Set<WrappedSootField> getEntryNode() {
		Set<WrappedSootField> out = new HashSet<>();
		out.add(fields.get(0));
		return out;
	}

	boolean hasLoops() {
		Set<SootField> sootFields = new HashSet<>();
		for (WrappedSootField f : this.fields) {
			if (sootFields.contains(f.getField()))
				return true;
			sootFields.add(f.getField());
		}
		return false;
	}

	public Collection<WrappedSootField> getExitNode() {
		return Collections.singleton(fields.getLast());
	}

	public String toString() {
		String str = "";
		str += fields.toString();
		return str;
	}

	@Override
	public boolean shouldOverApproximate() {
		return hasLoops();
	}

	@Override
	public IFieldGraph overapproximation() {
		return new SetBasedFieldGraph(new HashSet<>(fields));
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
		FieldGraph other = (FieldGraph) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}

	@Override
	public IFieldGraph noType() {
		LinkedList<WrappedSootField> list = new LinkedList<>();
		for(WrappedSootField f: fields){
			list.add(new WrappedSootField(f.getField(), null, null));
		}
		return new FieldGraph(list);
	}

}
