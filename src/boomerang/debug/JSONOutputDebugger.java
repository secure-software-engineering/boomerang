package boomerang.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Joiner;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class JSONOutputDebugger implements IBoomerangDebugger {
	private BoomerangContext context;
	private File jsonFile;
	private Map<SootMethod, ExplodedSuperGraph> methodToCfg = new HashMap<>();
	private Map<Object, Integer> objectToInteger = new HashMap<>();
	private IInfoflowCFG icfg;
	private Integer mainMethodId;
	private static int esgNodeCounter = 0;

	public JSONOutputDebugger(File jsonFile) {
		this.jsonFile = jsonFile;
	}

	@Override
	public void addIncoming(Direction direction, SootMethod callee, Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
	}

	@Override
	public void addSummary(Direction direction, SootMethod methodToSummary, IPathEdge<Unit, AccessGraph> summary) {
		for (Unit callSite : context.icfg.getCallersOf(methodToSummary)) {
			ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(callSite));
			cfg.addSummary(new ESGNode(summary.getStart(), summary.factAtSource(), direction),
					new ESGNode(summary.getTarget(), summary.factAtTarget(), direction));
		}
	}

	@Override
	public void normalFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(
				new ESGEdge(new ESGNode(start, startFact, dir), new ESGNode(target, targetFact, dir), "normalFlow"));
	}

	private ExplodedSuperGraph generateCFG(SootMethod sootMethod) {
		ExplodedSuperGraph cfg = methodToCfg.get(sootMethod);
		if (cfg == null) {
			cfg = new ExplodedSuperGraph(sootMethod);
			methodToCfg.put(sootMethod, cfg);
		}
		return cfg;
	}

	@Override
	public void callFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact, dir);
		CalleeESGNode calleeNode = new CalleeESGNode( dir == Direction.BACKWARD ? null : target,targetFact, dir, callSiteNode);
		cfg.addEdge(new ESGEdge(callSiteNode, calleeNode, "callFlow"));
	}

	@Override
	public void callToReturn(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(new ESGEdge(new ESGNode(start, startFact, dir), new ESGNode(target, targetFact, dir),
				"call2ReturnFlow"));
	}

	@Override
	public void returnFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(target));
		ESGNode nodeInMethod = new ESGNode(target, targetFact, dir);
		cfg.addEdge(new ESGEdge(new CalleeESGNode(start, startFact, dir, nodeInMethod), nodeInMethod, "returnFlow"));
	}

	@Override
	public void indirectFlowEdgeAtRead(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact, Direction.BACKWARD);
		cfg.addEdge(new ESGEdge(callSiteNode, new ESGNode(target, targetFact, Direction.BACKWARD), "indirectReadFlow"));
	}

	@Override
	public void indirectFlowEdgeAtWrite(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact, Direction.FORWARD);
		cfg.addEdge(new ESGEdge(callSiteNode, new ESGNode(target, targetFact, Direction.FORWARD), "indirectWriteFlow"));
	}

	@Override
	public void indirectFlowEdgeAtReturn(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact, Direction.FORWARD);
		cfg.addEdge(
				new ESGEdge(callSiteNode, new ESGNode(target, targetFact, Direction.FORWARD), "indirectReturnFlow"));
	}

	@Override
	public void indirectFlowEdgeAtCall(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact, Direction.BACKWARD);
		cfg.addEdge(new ESGEdge(callSiteNode, new ESGNode(target, targetFact, Direction.BACKWARD), "indirectCallFlow"));
	}

	@Override
	public void backwardStart(Direction backward, Unit stmt, AccessGraph d1, Unit s) {
	}

	@Override
	public void onEnterCall(Unit n, Collection<? extends IPathEdge<Unit, AccessGraph>> nextCallEdges,
			IPathEdge<Unit, AccessGraph> incEdge) {
	}

	@Override
	public void onProcessCall(IPathEdge<Unit, AccessGraph> edge) {
	}

	@Override
	public void onProcessExit(IPathEdge<Unit, AccessGraph> edge) {
	}

	@Override
	public void onProcessNormal(IPathEdge<Unit, AccessGraph> edge) {
	}

	@Override
	public void finishedQuery(Query q, AliasResults res) {
		writeToFile();
	}

	@Override
	public void startQuery(Query q) {
		mainMethodId = id(icfg.getMethodOf(q.getStmt()));
	}

	@Override
	public void onCurrentlyProcessingRecursiveQuery(Query q) {
	}

	@Override
	public void onLoadingQueryFromCache(Query q, AliasResults aliasResults) {
	}

	@Override
	public void onAllocationSiteReached(AssignStmt as, IPathEdge<Unit, AccessGraph> pe) {
	}

	@Override
	public void onAliasQueryFinished(Query q, AliasResults res) {
		writeToFile();
	}

	private void writeToFile() {
		try (FileWriter file = new FileWriter(jsonFile)) {
			List<String> stringList = new LinkedList<String>();
			List<String> methods = new LinkedList<String>();
			for (ExplodedSuperGraph c : methodToCfg.values()) {
				stringList.add(c.toJSONObject().toJSONString());
				methods.add(new Method(c.method).toJSONString());
			}
			file.write("var methods = [");
			file.write(Joiner.on(",\n").join(stringList));
			file.write("];\n");
			file.write("var methodList = [");
			file.write(Joiner.on(",\n").join(methods));
			file.write("];\n");

			file.write("var activeMethod = \"" + mainMethodId + "\";");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAliasTimeout(Query q) {
	}

	private class CalleeESGNode extends ESGNode {

		private ESGNode linkedNode;

		CalleeESGNode(Unit u, AccessGraph a, Direction dir, ESGNode linkedNode) {
			super(u, a, dir);
			this.linkedNode = linkedNode;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((linkedNode == null) ? 0 : linkedNode.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CalleeESGNode other = (CalleeESGNode) obj;
			if (linkedNode == null) {
				if (other.linkedNode != null)
					return false;
			} else if (!linkedNode.equals(other.linkedNode))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "CalleeESGNode "+ super.toString() + " linked to "+ linkedNode;
		}
	}

	private class ESGNode {
		Unit u;
		AccessGraph a;
		Direction dir;

		ESGNode(Unit u, AccessGraph a, Direction dir) {
			this.u = u;
			this.a = a;
			this.dir = dir;
			esgNodeCounter++;
			if (esgNodeCounter % 1000 == 0) {
				System.err.println("Warning: Using JSONOutputDebugger, might slow down performance.");
				writeToFile();
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((dir == null) ? 0 : dir.hashCode());
			result = prime * result + ((u == null) ? 0 : u.hashCode());

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
			ESGNode other = (ESGNode) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (u == null) {
				if (other.u != null)
					return false;
			} else if (!u.equals(other.u))
				return false;
			if (dir == null) {
				if (other.dir != null)
					return false;
			} else if (!dir.equals(other.dir))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return a +" @ "+ u + " " + dir ;
		}
	}

	private class ESGEdge {
		private ESGNode start;
		private ESGNode target;
		private String type;

		public ESGEdge(ESGNode start, ESGNode target, String type) {
			this.start = start;
			this.target = target;
			this.type = type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
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
			ESGEdge other = (ESGEdge) obj;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}

	public Integer id(Object u) {
		if (objectToInteger.get(u) != null)
			return objectToInteger.get(u);
		int size = objectToInteger.size() + 1;
		objectToInteger.put(u, size);
		return size;
	}

	private class ExplodedSuperGraph {
		private SootMethod method;
		private LinkedList<AccessGraph> facts = new LinkedList<>();
		private LinkedList<ESGNode> nodes = new LinkedList<>();
		private LinkedList<CalleeESGNode> calleeNodes = new LinkedList<>();
		private LinkedList<ESGEdge> edges = new LinkedList<>();
		private Set<Pair<ESGNode, ESGNode>> summaries = new HashSet<>();

		ExplodedSuperGraph(SootMethod m) {
			this.method = m;
		}

		public void addSummary(ESGNode start, ESGNode target) {
			summaries.add(new Pair<ESGNode, ESGNode>(start, target));
		}

		void addNode(ESGNode g) {
			if (!nodes.contains(g))
				nodes.add(g);
			if (!facts.contains(g.a) && !(g instanceof CalleeESGNode))
				facts.add(g.a);
			if (g instanceof CalleeESGNode)
				calleeNodes.add((CalleeESGNode) g);
		}

		void addEdge(ESGEdge g) {
			addNode(g.start);
			addNode(g.target);
			if (!edges.contains(g))
				edges.add(g);
		}

		private JSONObject toJSONObject() {
			linkSummaries();
			JSONObject o = new JSONObject();
			o.put("methodName", StringEscapeUtils.escapeHtml4(method.toString()));
			o.put("methodId", id(method));
			JSONArray data = new JSONArray();
			LinkedList<Unit> stmtsList = new LinkedList<>();
			int offset = 0;
			int labelYOffset = 0;
			int charSize = 8;
			for (AccessGraph g : facts) {
				labelYOffset = Math.max(labelYOffset, charSize * g.toString().length());
			}
			int index = 0;
			for (Unit u : method.getActiveBody().getUnits()) {
				
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				stmtsList.add(u);
				pos.put("x", 10);
				pos.put("y", stmtsList.size() * 30 + labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("shortLabel", getShortLabel(u));
				if (icfg.isCallStmt(u)) {
					label.put("callSite", icfg.isCallStmt(u));
					JSONArray callees = new JSONArray();
					for (SootMethod callee : icfg.getCalleesOfCallAt(u))
						callees.add(new Method(callee));
					label.put("callees", callees);
				}
				if (icfg.isExitStmt(u)) {
					label.put("returnSite", icfg.isExitStmt(u));
					JSONArray callees = new JSONArray();
					Set<SootMethod> callers = new HashSet<>();
					for (Unit callsite : icfg.getCallersOf(context.icfg.getMethodOf(u)))
						callers.add(context.icfg.getMethodOf(callsite));

					for (SootMethod caller : callers)
						callees.add(new Method(caller));
					label.put("callers", callees);
				}
				label.put("stmtId", id(u));
				label.put("id", "stmt" + id(u));

				label.put("stmtIndex", index);
				index++;

				nodeObj.put("data", label);
				nodeObj.put("classes", "stmt label " + (icfg.isExitStmt(u) ? " returnSite " :" ")+ (icfg.isCallStmt(u) ? " callSite " :" "));
				data.add(nodeObj);
				offset = Math.max(offset, getShortLabel(u).toString().length());
				
				for(Unit succ : context.icfg.getSuccsOf(u)){
					JSONObject cfgEdgeObj = new JSONObject();
					JSONObject dataEntry = new JSONObject();
					dataEntry.put("source", "stmt" + id(u));
					dataEntry.put("target", "stmt" + id(succ));
					dataEntry.put("directed", "true");
					cfgEdgeObj.put("data", dataEntry);
					cfgEdgeObj.put("classes", "cfgEdge label method" + id(method));
					data.add(cfgEdgeObj);
				}
			}

			LinkedList<AccessGraph> factsList = new LinkedList<>();

			for (AccessGraph u : facts) {
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				factsList.add(u);
				pos.put("x", factsList.size() * 30 + offset * charSize);
				pos.put("y", labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("factId", id(u));
				nodeObj.put("classes", "fact label");
				nodeObj.put("data", label);
				data.add(nodeObj);
			}

			for (ESGNode node : nodes) {
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				if (node instanceof CalleeESGNode) {
					CalleeESGNode calleeESGNode = (CalleeESGNode) node;
					pos.put("x", (factsList.indexOf(calleeESGNode.linkedNode.a) + 1) * 30 + 10 + offset * charSize);
					pos.put("y", (stmtsList.indexOf(calleeESGNode.linkedNode.u)
							+ (calleeESGNode.linkedNode.dir == Direction.FORWARD ? 0 : 1)) * 30 + labelYOffset);
				} else {
					assert stmtsList.indexOf(node.u) != -1;
					pos.put("x", (factsList.indexOf(node.a) + 1) * 30 + offset * charSize);
					pos.put("y",
							(stmtsList.indexOf(node.u) + (node.dir == Direction.FORWARD ? 0 : 1)) * 30 + labelYOffset);
				}

				nodeObj.put("position", pos);
				String classes = "esgNode method" + id(method) + "  " + node.dir;

				JSONObject additionalData = new JSONObject();
				additionalData.put("id", "n" + id(node));
				additionalData.put("stmtId", id(node.u));
				additionalData.put("factId", id(node.a));
				nodeObj.put("classes", classes);
				nodeObj.put("group", "nodes");
				nodeObj.put("data", additionalData);

				data.add(nodeObj);
			}
			for (ESGEdge edge : edges) {
				JSONObject nodeObj = new JSONObject();
				JSONObject dataEntry = new JSONObject();
				dataEntry.put("id", "e" + id(edge));
				dataEntry.put("source", "n" + id(edge.start));
				dataEntry.put("target", "n" + id(edge.target));
				dataEntry.put("directed", "true");
				dataEntry.put("direction", edge.start.dir.toString());
				nodeObj.put("data", dataEntry);
				nodeObj.put("classes", "esgEdge method" + id(method) + " " + edge.start.dir + " " + edge.type);
				nodeObj.put("group", "edges");
				data.add(nodeObj);
			}
			o.put("data", data);
			return o;
		}

		private void linkSummaries() {
			for (Pair<ESGNode, ESGNode> p : summaries) {
				ESGNode start = p.getO1();
				ESGNode target = p.getO2();
				Set<CalleeESGNode> starts = new HashSet<>();
				Set<CalleeESGNode> targets = new HashSet<>();
				for (CalleeESGNode n : calleeNodes) {
					if (n.a.equals(start.a) && (n.u == null || n.u.equals(start.u)) && n.dir.equals(start.dir))
						starts.add(n);
					if (n.a.equals(target.a) &&(n.u == null || n.u.equals(target.u)) && n.dir.equals(target.dir))
						targets.add(n);
				}
				for (CalleeESGNode summaryStart : starts) {
					for (CalleeESGNode summaryTarget : targets) {
						if (summaryStart.dir == Direction.FORWARD) {
							if (context.icfg.getSuccsOf(summaryStart.linkedNode.u).contains(summaryTarget.linkedNode.u))
								addEdge(new ESGEdge(summaryStart, summaryTarget, "summaryFlow"));
						} else if (summaryStart.dir == Direction.BACKWARD) {
							if (context.icfg.getPredsOf(summaryStart.linkedNode.u).contains(summaryTarget.linkedNode.u))
								addEdge(new ESGEdge(summaryStart, summaryTarget, "summaryFlow"));
						}
					}
				}
			}
		}

	}

	private String getShortLabel(Unit u) {
		if (u instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) u;
			if (assignStmt.getRightOp() instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getRightOp();
				return assignStmt.getLeftOp() + " = " + fr.getBase() + "." + fr.getField().getName();
			}
			if (assignStmt.getLeftOp() instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getLeftOp();
				return fr.getBase() + "." + fr.getField().getName() + " = " + assignStmt.getRightOp();
			}
		}
		if (u instanceof Stmt && ((Stmt) u).containsInvokeExpr()) {
			InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
			if (invokeExpr instanceof StaticInvokeExpr)
				return (u instanceof AssignStmt ? ((AssignStmt) u).getLeftOp() + " = " : "")
						+ invokeExpr.getMethod().getName() + "("
						+ invokeExpr.getArgs().toString().replace("[", "").replace("]", "") + ")";
			if (invokeExpr instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr iie = (InstanceInvokeExpr) invokeExpr;
				return (u instanceof AssignStmt ? ((AssignStmt) u).getLeftOp() + " = " : "") + iie.getBase() + "."
						+ invokeExpr.getMethod().getName() + "("
						+ invokeExpr.getArgs().toString().replace("[", "").replace("]", "") + ")";
			}
		}
		return u.toString();
	}

	private class Method extends JSONObject {

		Method(SootMethod m) {
			this.put("name", StringEscapeUtils.escapeHtml4(m.toString()));
			this.put("id", id(m));
		}

	}

	@Override
	public void setContext(BoomerangContext boomerangContext) {
		this.context = boomerangContext;
		this.icfg = context.icfg;
	}
}
