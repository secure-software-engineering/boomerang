package boomerang;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import boomerang.bidi.Incomings;
import boomerang.cache.Query;
import boomerang.forward.ForwardSolver;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Alloc;
import boomerang.pointsofindirection.BackwardParameterTurnHandler;
import boomerang.pointsofindirection.Call;
import boomerang.pointsofindirection.Meeting;
import boomerang.pointsofindirection.PointOfIndirection;
import boomerang.pointsofindirection.Read;
import boomerang.pointsofindirection.Return;
import boomerang.pointsofindirection.Unbalanced;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Type;
import soot.Unit;

public class SubQueryContext {
	private Query query;
	private Incomings incomings = new Incomings();
	private PriorityQueue<PointOfIndirection> worklist = new PriorityQueue<PointOfIndirection>(300, new Comparator<PointOfIndirection>() {

		@Override
		public int compare(PointOfIndirection o1, PointOfIndirection o2) {
			if(o1 instanceof Alloc)
				return -1;

			if(o1 instanceof Call && o2 instanceof Alloc)
				return 1;

			if(o1 instanceof Call && o2 instanceof BackwardParameterTurnHandler)
				return -1;
			if(o1 instanceof Call && o2 instanceof Meeting)
				return 1;

			if(o1 instanceof Call && o2 instanceof Read)
				return -1;

			if(o1 instanceof BackwardParameterTurnHandler && o2 instanceof Alloc)
				return 1;

			if(o1 instanceof BackwardParameterTurnHandler && o2 instanceof Call)
				return 1;
			if(o1 instanceof BackwardParameterTurnHandler && o2 instanceof Meeting)
				return 1;
			if(o1 instanceof BackwardParameterTurnHandler && o2 instanceof Read)
				return -1;

			if(o1 instanceof Meeting && o2 instanceof Alloc)
				return 1;
			if(o1 instanceof Meeting && o2 instanceof Call)
				return -1;
			if(o1 instanceof BackwardParameterTurnHandler && o2 instanceof Read)
				return -1;
			if(o1 instanceof Read)
				return 1;

			return 0;
		}
	});
	private ForwardSolver forwardSolver;
	private BoomerangContext context;

	SubQueryContext(Query q, BoomerangContext c, SubQueryContext parent) {
		this.query = q;
		this.context = c;
	}

	/**
	 * The statement at which the query has been triggered.
	 * 
	 * @return the statement of the query
	 */
	public Unit getStmt() {
		return query.getStmt();
	}

	/**
	 * The {@link AccessGraph} for which the query was started
	 * 
	 * @return the initial access graph of the query.
	 */
	public AccessGraph getAccessPath() {
		return query.getAp();
	}

	/**
	 * Returns the method in which the query was originally started.
	 * 
	 * @return the method of the statement at which the query was triggered.
	 */
	public SootMethod getMethod() {
		return query.getMethod();
	}

	/**
	 * Each SubQuery has its own backward incoming set, such that the forward
	 * analysis only returns along the paths the backward analysis previously
	 * took. Therefore, an incoming edge, {@link IPathEdge}, is added when the
	 * backward analysis enters via a call flow a method.
	 * 
	 * @param callee
	 *            The method being entered
	 * @param factAtCalleeStart
	 *            The fact at the startpoint of the method.
	 * @param callsiteEdge
	 *            The edge at the callsite of the callee (the callsite is stored
	 *            within the edge)
	 */
	public void addBackwardIncoming(SootMethod callee, Pair<Unit, AccessGraph> factAtCalleeStart,
			IPathEdge<Unit, AccessGraph> callsiteEdge) {
		incomings.addIncoming(callee, factAtCalleeStart, callsiteEdge);
	}

	/**
	 * Returns the set of incoming edges {@link IPathEdge} which hold for the
	 * callee method with the appropriate start pair
	 * 
	 * @param startStmtAndFact
	 *            The statement and the fact which enter the call.
	 * @param callee
	 *            The method being entered
	 * @return
	 */
	public Collection<IPathEdge<Unit, AccessGraph>> backwardIncoming(Pair<Unit, AccessGraph> startStmtAndFact,
			SootMethod callee) {
		return incomings.incoming(startStmtAndFact, callee);
	}

	void cleanup() {
		incomings.clear();
		incomings = null;
	}

	/**
	 * Adds a {@link PointOfIndirection} to the worklist. Beforehand a check is
	 * performed, whether this specific POI has already been (globally)
	 * processed before. If so it will not be processed again.
	 * 
	 * @param poi
	 *            The {@link PointOfIndirection} to be added.
	 * @return Returns <code>true</code> if the POI has been added, otherwise
	 *         <code>false</code>.
	 */
	HashSet<PointOfIndirection> instantiated = new HashSet<>();
	public boolean add(PointOfIndirection poi) {
		return worklist.add(poi);
	}

	public String toString() {
		return query.toString();
	}

	public Collection<Type> getType() {
		return query.getType();
	}

	public boolean isEmpty() {
		return worklist.isEmpty();
	}

	PointOfIndirection removeFirst() {PointOfIndirection el = worklist.poll();

		instantiated.add(el);
		return el;
	}

	public ForwardSolver getCurrentForwardSolver() {
		return forwardSolver;
	}

	public void setCurrentForwardSolver(ForwardSolver solver) {
		this.forwardSolver = solver;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((query == null) ? 0 : query.hashCode());
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
		SubQueryContext other = (SubQueryContext) obj;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		return true;
	}

}
