package boomerang.accessgraph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import heros.solver.Pair;
import toools.collections.Collections;
import toools.set.IntSet;

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
public class FieldGraph implements IFieldGraph{
	/**
	 * The integer representing the first field of the field graph (initial
	 * state in term of FSM)
	 */
	private int entryNode;

	/**
	 * The actual graph. (Nodes are labeled with field accesses.)
	 */
	private Grph graph;

	/**
	 * The integer representing the exit node (accepting state) of the graph
	 * (FSM).
	 */
	private int targetNode;
	private boolean immutable;
	private Map<Pair<Integer, Integer>, Boolean> reachableCache = new HashMap<>();

	/**
	 * As the Grph library uses integers to represent nodes in the Graphs, this
	 * list holds the mapping from fields to integers.
	 */
	private final static List<WrappedSootField> fieldToInteger = new LinkedList<>();

	enum Loop {
		FALSE, TRUE
	}

	/**
	 * Determining if the access graph has a loop or not. Is computed lazily.
	 */
	private Loop loopState;
	private Map<Pair<Integer, Integer>, IntArrayList> shortestPathCache = new HashMap<>();

	static FieldGraph EMPTY_GRAPH = new FieldGraph(){public String toString() {return "EMPTY_GRAPH";};};

	FieldGraph(WrappedSootField[] fields) {
		assert fields != null && fields.length > 0;
		entryNode = fieldToInt(fields[0]);
		WrappedSootField from = fields[0];
		if (graph == null)
			graph = createGraph();
		for (int i = 1; i < fields.length; i++) {
			WrappedSootField to = fields[i];
			int tail = fieldToInt(from);
			int head = fieldToInt(to);
			// if(!graph.containsVertex(tail))
			// graph.addVertex(tail);
			// if(!graph.containsVertex(head))
			// graph.addVertex(head);
			addEdge(tail, head, graph);
			from = to;
		}
		targetNode = fieldToInt(fields[fields.length - 1]);
		minimize();
		sanity();
	}

	FieldGraph(int entry, int exit, Grph grph) {
		this.entryNode = entry;
		this.targetNode = exit;
		this.graph = grph;
		minimize();
		sanity();
	}

	FieldGraph(WrappedSootField f) {
		assert f != null;
		entryNode = fieldToInt(f);
		targetNode = fieldToInt(f);
		minimize();
		sanity();
	}

	private FieldGraph() {
	}

	private Grph createGraph() {
		return new InMemoryGrph();
	}

	private int fieldToInt(WrappedSootField from) {
		if (fieldToInteger.contains(from)) {
			return fieldToInteger.indexOf(from);
		}
		;
		fieldToInteger.add(from);
		return fieldToInteger.size() - 1;
	}

	private WrappedSootField intToField(int hash) {
		return fieldToInteger.get(hash);
	}

	private boolean containsEdge(int tail, int head, Grph g) {
		if (!g.containsVertex(tail))
			return false;
		IntSet outEdges = g.getOutEdges(tail);
		for (IntCursor e : outEdges) {
			int t = g.getDirectedSimpleEdgeHead(e.value);
			if (t == head)
				return true;
		}
		return false;
	}

	private void minimize() {
		if (graph == null || isImmutable())
			return;

		IntArrayList verticesToRemove = new IntArrayList();
		IntSet vertices = graph.getVertices();
		if (!vertices.contains(entryNode) || !vertices.contains(targetNode)) {
			graph = null;
			return;
		}

		IntArrayList visitedFromStart = getVisited(entryNode, true);
		IntArrayList visitedFromExit = getVisited(targetNode, false);

		for (IntCursor nodeCur : vertices) {
			int node = nodeCur.value;
			if (!visitedFromStart.contains(node) || !visitedFromExit.contains(node)) {
				verticesToRemove.add(node);
			}
		}
		graph.removeVertices(verticesToRemove.toArray());
		if (graph.getVertices().size() == 0 || graph.getEdges().size() == 0)
			graph = null;
		if (graph != null)
			setImmutable();
	}

	private void setImmutable() {
		immutable = true;
	}

	private boolean isImmutable() {
		return immutable;
	}

	private void sanity() {
		if (graph == null) {
			assert entryNode == targetNode;
			return;
		}
		assert graph.getVertices().contains(entryNode);
		assert graph.getVertices().contains(targetNode);
		assert isReachable(entryNode, targetNode);
		if (!graph.getVertices().isEmpty()) {
			assert graph.getVertices().contains(entryNode);
			assert graph.getVertices().contains(targetNode);
		}
		for (IntCursor sCur : graph.getVertices()) {
			int s = sCur.value;
			assert s == entryNode || isReachable(entryNode, s);
			assert s == targetNode || isReachable(s, targetNode);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Set<IFieldGraph> popFirstField() {
		if (graph == null || graph.getVertices().size() == 0)
			return new HashSet<>();
		IntSet outEdges = graph.getOutEdges(entryNode);
		Set<IFieldGraph> out = new HashSet<>();
		for (IntCursor newEntryCur : outEdges) {
			int newEntry = newEntryCur.value;
			int newHead = graph.getDirectedSimpleEdgeHead(newEntry);
			boolean isInCycle = isReachable(newHead, entryNode);
			if (isInCycle) {
				out.add(EMPTY_GRAPH);
			}
			if (isInCycle) {
				out.add(new FieldGraph(newHead, targetNode, myClone(graph)));
			} 
		
			Grph newGraph = createGraph();
			// newGraph.addVertices(graph.getVertices());
			for (IntCursor eCur : graph.getEdges()) {
				int e = eCur.value;
				if (e != newEntry) {
					int t = graph.getDirectedSimpleEdgeTail(e);
					int h = graph.getDirectedSimpleEdgeHead(e);
					addEdge(t, h, newGraph);
				}
			}
			FieldGraph derived = new FieldGraph(newHead, targetNode, newGraph);
			out.add(derived);
		}
		return out;
	}

	private boolean isReachable(int source, int destination) {
		if (graph == null)
			return false;
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(source, destination);
		if (isImmutable() && reachableCache.containsKey(key)) {
			return reachableCache.get(key);
		}
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		IntArrayList visited = new IntArrayList();
		boolean isReachable = false;
		while (!queue.isEmpty()) {
			Integer next = queue.poll();
			if (!visited.contains(next)) {
				visited.add(next);
				IntSet edges = graph.getOutEdges(next);
				for (IntCursor eCur : edges) {
					int e = eCur.value;
					int succNode = graph.getDirectedSimpleEdgeHead(e);
					if (succNode == destination) {
						isReachable = true;
						break;
					}
					queue.add(succNode);
				}
			}
		}
		if (isImmutable()) {
			reachableCache.put(key, isReachable);
		}
		return isReachable;
	}

	private IntArrayList getShortestPath(int source, int destination) {
		if (graph == null)
			return new IntArrayList();

		Pair<Integer, Integer> key = new Pair<Integer, Integer>(source, destination);
		if (isImmutable() && shortestPathCache.containsKey(key)) {
			return shortestPathCache.get(key);
		}
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		IntArrayList visited = new IntArrayList();
		while (!queue.isEmpty()) {
			Integer next = queue.poll();
			if (!visited.contains(next)) {
				visited.add(next);
				IntSet edges = graph.getOutEdges(next);
				for (IntCursor eCur : edges) {
					int e = eCur.value;
					int succNode = graph.getDirectedSimpleEdgeHead(e);
					if (succNode == destination) {
						if (!visited.contains(destination))
							visited.add(destination);
						shortestPathCache.put(key, visited);
						return visited;
					}
					queue.add(succNode);
				}
			}
		}

		throw new RuntimeException("No path found");
	}

	private IntArrayList getVisited(int source, boolean forward) {
		if (graph == null)
			return new IntArrayList();

		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(source);
		IntArrayList visited = new IntArrayList();
		while (!queue.isEmpty()) {
			Integer next = queue.poll();
			if (!visited.contains(next)) {
				visited.add(next);
				IntSet edges = forward ? graph.getOutEdges(next) : graph.getInEdges(next);
				for (IntCursor eCur : edges) {
					int e = eCur.value;
					int succNode;
					if (forward) {
						succNode = graph.getDirectedSimpleEdgeHead(e);
					} else {
						succNode = graph.getDirectedSimpleEdgeTail(e);
					}
					queue.add(succNode);
				}
			}
		}
		return visited;
	}

	public WrappedSootField[] getFields() {
		if (graph == null || graph.getVertices().size() == 0) {
			return new WrappedSootField[] { intToField(entryNode) };
		}

		IntArrayList shortestPath = getShortestPath(entryNode, targetNode);
		WrappedSootField[] out = new WrappedSootField[shortestPath.size()];
		int i = 0;
		for (IntCursor p : shortestPath) {
			out[i] = intToField(p.value);
			i++;
		}
		return out;
	}

	public IFieldGraph prependField(WrappedSootField f) {
		Grph newGraph = (graph == null ? createGraph() : myClone(graph));
		int tail = fieldToInt(f);
		addEdge(tail, entryNode, newGraph);
		return new FieldGraph(tail, targetNode, newGraph);
	}

	public Set<IFieldGraph> popLastField() {
		if (graph == null || graph.getVertices().size() == 0)
			return new HashSet<>();
		IntSet inEdges = graph.getInEdges(targetNode);

		Set<IFieldGraph> out = new HashSet<>();
		for (IntCursor newExitCur : inEdges) {
			int newExit = newExitCur.value;
			int newTail = graph.getDirectedSimpleEdgeTail(newExit);
			boolean isInCycle = isReachable(targetNode, newTail);
			if (isInCycle && newExit != targetNode) {
				out.add(new FieldGraph(entryNode, newTail, myClone(graph)));
			} else {
				Grph newGraph = createGraph();
				// newGraph.addVertices(graph.getVertices());
				for (IntCursor eCur : graph.getEdges()) {
					int e = eCur.value;
					if (e != newExit) {
						int t = graph.getDirectedSimpleEdgeTail(e);
						int h = graph.getDirectedSimpleEdgeHead(e);
						addEdge(t, h, newGraph);
					}
				}
				FieldGraph derived = new FieldGraph(entryNode, newTail, newGraph);
				out.add(derived);
			}
		}
		return out;
	}

	public IFieldGraph append(IFieldGraph o) {
		if(o instanceof SetBasedFieldGraph){
			SetBasedFieldGraph setBasedFieldGraph = (SetBasedFieldGraph) o;
			return setBasedFieldGraph.append(this);
		}
		else if(o instanceof FieldGraph){
			FieldGraph other = (FieldGraph) o;
			Grph newGraph = (graph == null ? createGraph() : myClone(graph));
			if (other.graph != null) {
				for (IntCursor aCur : other.graph.getVertices()) {
					int a = aCur.value;
					if (!newGraph.containsVertex(a))
						newGraph.addVertex(a);
				}
				for (IntCursor eCur : other.graph.getEdges()) {
					int e = eCur.value;
					int t = other.graph.getDirectedSimpleEdgeTail(e);
					int h = other.graph.getDirectedSimpleEdgeHead(e);
					addEdge(t, h, newGraph);
				}
			}
			addEdge(targetNode, other.entryNode, newGraph);
			return new FieldGraph(entryNode, other.targetNode, newGraph);
		}
		throw new RuntimeException("Not yet implemented!");
	}

	public IFieldGraph appendFields(WrappedSootField[] toAppend) {
		return append(new FieldGraph(toAppend));
	}

	private void addEdge(int tail, int head, Grph newGraph) {
		if (!containsEdge(tail, head, newGraph))
			newGraph.addDirectedSimpleEdge(tail, head);
	}

	public Set<WrappedSootField> getEntryNode() {
		Set<WrappedSootField> out = new HashSet<>();
		out.add(intToField(entryNode));
		return out;
	}

	boolean hasLoops() {
		if (graph == null)
			return false;
		if (isImmutable() && loopState == Loop.FALSE)
			return false;
		if (isImmutable() && loopState == Loop.TRUE)
			return true;

		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(entryNode);
		IntArrayList visited = new IntArrayList();

		while (!queue.isEmpty()) {
			Integer next = queue.poll();
			if (visited.contains(next)) {
				loopState = Loop.TRUE;
				return true;
			}
			visited.add(next);
			IntSet edges = graph.getOutEdges(next);
			for (IntCursor e : edges) {
				int succNode = graph.getDirectedSimpleEdgeHead(e.value);
				queue.add(succNode);
			}
		}
		loopState = Loop.FALSE;
		return false;
	}

	public Collection<WrappedSootField> getExitNode() {
		return Collections.singleton(intToField(targetNode));
	}

	public String toString() {
		String str = "";
		if (!hasLoops())
			str += Arrays.toString(getFields());
		else {
			str += getNodesString();
			str += getEdgeString();
		}
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + entryNode;
		result = prime * result + targetNode;
		if (graph != null) {
			result = prime * result + graph.getVertices().hashCode();
			result = prime * result + graph.getEdges().hashCode();
		}
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
		if (entryNode != other.entryNode)
			return false;
		if (targetNode != other.targetNode)
			return false;
		if (graph == null) {
			if (other.graph != null)
				return false;
		} else if (!graph.equals((Object) other.graph))
			return false;
		return true;
	}

	int getEdgeSize() {
		return (graph == null ? 0 : graph.getEdges().size());
	}

	int getNodesSize() {
		return (graph == null ? 0 : graph.getVertices().size());
	}

	private Grph myClone(Grph other) {
		Grph clone = createGraph();

		for (IntCursor v : other.getVertices()) {
			clone.addVertex(v.value);
		}

		for (IntCursor c : other.getEdges()) {
			int e = c.value;

			int t = other.getDirectedSimpleEdgeTail(e);
			int h = other.getDirectedSimpleEdgeHead(e);
			clone.addDirectedSimpleEdge(t, e, h);
		}

		assert other.equals(clone);
		assert other.getVertices().hashCode() == clone.getVertices().hashCode();
		assert other.getEdges().hashCode() == clone.getEdges().hashCode();
		return clone;
	}

	String getNodesString() {
		String s = "(" + intToField(entryNode) + "{";
		for (int a : graph.getVertices().toIntArray()) {
			if (a == entryNode || a == targetNode) {
				continue;
			}
			s += intToField(a).toString() + ", ";
		}
		return s + "}" + intToField(targetNode);
	}

	String getEdgeString() {
		String s = "";
		for (int a : graph.getEdges().toIntArray()) {
			int t = graph.getDirectedSimpleEdgeTail(a);
			int h = graph.getDirectedSimpleEdgeHead(a);
			s += "(" + intToField(t) + ">" + intToField(h) + ")";
		}
		return s + "";
	}

	@Override
	public boolean shouldOverApproximate() {
		if(graph == null)
			return false;
		return hasLoops();
	}

	@Override
	public IFieldGraph overapproximation() {
		return new SetBasedFieldGraph(getAllFields());
	}

	private Set<WrappedSootField> getAllFields() {
		Set<WrappedSootField> fields = new HashSet<>();
		for (int a : graph.getVertices().toIntArray()) {
			fields.add(intToField(a));
		}
		fields.add(intToField(entryNode));
		fields.add(intToField(targetNode));
		return fields;
	}


}
