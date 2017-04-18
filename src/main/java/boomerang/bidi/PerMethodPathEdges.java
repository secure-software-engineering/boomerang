package boomerang.bidi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.AliasCallback;
import boomerang.pointsofindirection.PointOfIndirection;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;

class PerMethodPathEdges {
	private Map<Unit, PerStatementPathEdges> stmtToPathEdges = new HashMap<>();
	private final BoomerangContext context;
	private Direction direction;

	public PerMethodPathEdges(BoomerangContext context, Direction direction) {
		this.context = context;
		this.direction = direction;
	}

	boolean hasAlreadyProcessed(IPathEdge<Unit, AccessGraph> pe) {
		PerStatementPathEdges pathEdges = getOrCreate(pe.getTarget());
		return pathEdges.hasAlreadyProcessed(pe);
	}

	private PerStatementPathEdges getOrCreate(Unit stmt) {
		PerStatementPathEdges perStatementPathEdges = stmtToPathEdges.get(stmt);
		if (perStatementPathEdges == null)
			perStatementPathEdges = new PerStatementPathEdges(context,direction);
		stmtToPathEdges.put(stmt, perStatementPathEdges);
		return perStatementPathEdges;
	}

	void register(IPathEdge<Unit, AccessGraph> pe) {
		Unit target = pe.getTarget();
		PerStatementPathEdges pathedges = getOrCreate(target);
		pathedges.register(pe);
	}

	public void registerPointOfIndirectionAt(Unit stmt, PointOfIndirection poi, AliasCallback callback) {
		PerStatementPathEdges pathedges = getOrCreate(stmt);
		pathedges.registerPointOfIndirectionAt(poi, callback);
	}

	public int size() {
		int c = 0;
		for (Unit u : this.stmtToPathEdges.keySet())
			c += this.stmtToPathEdges.get(u).size();
		return c;
	}

	Multimap<Pair<Unit, AccessGraph>, AccessGraph> getResultsAtStmtContainingValue(Unit stmt, AccessGraph fact, Set<Pair<Unit,AccessGraph>> visited) {
		PerStatementPathEdges pathedges = getOrCreate(stmt);
		return pathedges.getResultsAtStmtContainingValue(stmt, fact,visited);
	}

	public SootMethod reportStats() {
		return null;
	}

	public void printTopStmts() {
		TreeSet<Unit> set = new TreeSet<>(new Comparator<Unit>() {
			@Override
			public int compare(Unit o1, Unit o2) {
				PerStatementPathEdges m1 = stmtToPathEdges.get(o1);
				int size1 = m1.size();
				PerStatementPathEdges m2 = stmtToPathEdges.get(o2);
				int size2 = m2.size();
				return size1 > size2 ? -1 : 1;
			}

		});
		for (Unit unit : stmtToPathEdges.keySet())
			set.add(unit);
		for (Unit m : set) {
			System.out.println("\t " + m + " " + stmtToPathEdges.get(m).size());
			stmtToPathEdges.get(m).groupByStartUnit();
		}
	}

}
