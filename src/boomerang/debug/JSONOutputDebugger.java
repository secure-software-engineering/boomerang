package boomerang.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.base.Joiner;

import boomerang.BoomerangContext;
import boomerang.SubQueryContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.Query;
import boomerang.ifdssolver.DefaultIFDSTabulationProblem.Direction;
import boomerang.ifdssolver.IPathEdge;
import boomerang.pointsofindirection.Alloc;
import boomerang.pointsofindirection.BackwardParameterTurnHandler;
import boomerang.pointsofindirection.Call;
import boomerang.pointsofindirection.Meeting;
import boomerang.pointsofindirection.Read;
import boomerang.pointsofindirection.Return;
import boomerang.pointsofindirection.Unbalanced;
import boomerang.pointsofindirection.Write;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class JSONOutputDebugger implements IBoomerangDebugger{
	private BoomerangContext context;
	private File jsonFile;
	private Map<SootMethod,ExplodedSuperGraph> methodToCfg = new HashMap<>();
	private Map<Object,Integer> objectToInteger = new HashMap<>();
	private IInfoflowCFG icfg;
	private List<Subquery> subQueries = new LinkedList<>();
	private Set<SubqueryEdge> subQueryEdges = new HashSet<>();
	private Map<Subquery,AliasResults> queries = new HashMap<>();
	
	public JSONOutputDebugger(File jsonFile) {
		this.jsonFile = jsonFile;
		
	}
	@Override
	public void addIncoming(Direction direction, SootMethod callee, Pair<Unit, AccessGraph> pair,
			IPathEdge<Unit, AccessGraph> pe) {
	}

	@Override
	public void addSummary(Direction direction, SootMethod methodToSummary, IPathEdge<Unit, AccessGraph> summary) {
	}

	@Override
	public void normalFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(new ESGEdge(new ESGNode(start,startFact,dir,context.getSubQuery()),new ESGNode(target,targetFact,dir,context.getSubQuery()),"normalFlow"));
	}
	private ExplodedSuperGraph generateCFG(SootMethod sootMethod) {
		ExplodedSuperGraph cfg = methodToCfg.get(sootMethod);
		if(cfg == null){
			cfg = new ExplodedSuperGraph(sootMethod);
			methodToCfg.put(sootMethod, cfg);
		}
		return cfg;
	}

	@Override
	public void callFlow(Direction dir, Unit start, AccessGraph startFact, Unit target,
			AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start,startFact,dir,context.getSubQuery());
		cfg.addEdge(new ESGEdge(callSiteNode,new CalleeESGNode(target,targetFact,dir,context.getSubQuery(),callSiteNode),"callFlow"));
	}

	@Override
	public void callToReturn(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(new ESGEdge(new ESGNode(start,startFact,dir,context.getSubQuery()),new ESGNode(target,targetFact,dir,context.getSubQuery()),"call2ReturnFlow"));
	}

	@Override
	public void returnFlow(Direction dir, Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(target));
		ESGNode nodeInMethod = new ESGNode(target,targetFact,dir,context.getSubQuery());
		cfg.addEdge(new ESGEdge(new CalleeESGNode(start,startFact,dir,context.getSubQuery(),nodeInMethod),nodeInMethod,"returnFlow"));
	}

	@Override
	public void indirectFlowEdgeAtRead(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start,startFact,Direction.BACKWARD,context.getSubQuery());
		cfg.addEdge(new ESGEdge(callSiteNode,new ESGNode(target,targetFact,Direction.BACKWARD,context.getSubQuery()),"indirectReadFlow"));
	}
	@Override
	public void indirectFlowEdgeAtWrite(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start,startFact,Direction.FORWARD,context.getSubQuery());
		cfg.addEdge(new ESGEdge(callSiteNode,new ESGNode(target,targetFact,Direction.FORWARD,context.getSubQuery()),"indirectWriteFlow"));
	}
	@Override
	public void indirectFlowEdgeAtReturn(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start,startFact,Direction.FORWARD,context.getSubQuery());
		cfg.addEdge(new ESGEdge(callSiteNode,new ESGNode(target,targetFact,Direction.FORWARD,context.getSubQuery()),"indirectReturnFlow"));	
	}
	@Override
	public void indirectFlowEdgeAtCall(AccessGraph startFact, Unit start, AccessGraph targetFact, Unit target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start,startFact,Direction.BACKWARD,context.getSubQuery());
		cfg.addEdge(new ESGEdge(callSiteNode,new ESGNode(target,targetFact,Direction.BACKWARD,context.getSubQuery()),"indirectCallFlow"));
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
		queries.put(new Subquery(context.getSubQuery(),0),res);
	}

	@Override
	public void startQuery(Query q) {
		int level = context.size();
		Subquery subquery = new Subquery(context.getSubQuery(),level);
		if(!subQueries.add(subquery))
			return;
		if(context.size() > 1){
			Subquery parent = new Subquery(context.get(1),level-1);
			if(!subQueries.contains(parent))
				throw new AssertionError("Query Hierarchy wrong");
			subQueryEdges.add(new SubqueryEdge(parent,subquery));
		}
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
	public void onProcessingMeetingPOI(Meeting meeting) {
	}

	@Override
	public void onProcessingFieldReadPOI(Read read) {
	}

	@Override
	public void continuePausedEdges(Collection<IPathEdge<Unit, AccessGraph>> pauseEdges) {
	}

	@Override
	public void onProcessAllocationPOI(Alloc alloc) {
	}

	@Override
	public void onProcessCallPOI(Call call) {
	}

	@Override
	public void onProcessReturnPOI(Return return1) {
	}

	@Override
	public void onProcessWritePOI(Write write) {
	}

	@Override
	public void onProcessUnbalancedReturnPOI(Unbalanced unbalanced) {
	}

	@Override
	public void onProcessingParamPOI(BackwardParameterTurnHandler backwardParameterTurnHandler) {
	}

	@Override
	public void onAliasQueryFinished(Query q, AliasResults res) {
		try (FileWriter file = new FileWriter(jsonFile)) {
			List<String> stringList = new LinkedList<String>();
			List<String> methods = new LinkedList<String>();
			for(ExplodedSuperGraph c :methodToCfg.values()){
				stringList.add(c.toJSONObject().toJSONString());
				methods.add(new Method(c.method).toJSONString());
			}
			file.write( "var methods = [");
			file.write(Joiner.on(",\n").join(stringList));
			file.write( "];\n");
			file.write( "var methodList = [");
			file.write(Joiner.on(",\n").join(methods));
			file.write( "];\n");
			file.write( "var subQueries = [");
			stringList = new LinkedList<String>();
			for(Subquery c :subQueries){
				c.setResults(queries.get(c));
				stringList.add(c.toJSONString());
			}
			for(SubqueryEdge c :subQueryEdges){
				stringList.add(c.toJSONString());
			}
			file.write(Joiner.on(",\n").join(stringList));
			file.write( "];\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAliasTimeout(Query q) {
	}

	private class CalleeESGNode extends ESGNode{

		private ESGNode linkedNode;

		CalleeESGNode(Unit u, AccessGraph a, Direction dir, SubQueryContext q, ESGNode linkedNode) {
			super(u, a, dir, q);
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

		
	}
	private class ESGNode {
		private Unit u;
		private AccessGraph a;
		private SubQueryContext subquery;
		private Direction dir;
		ESGNode(Unit u, AccessGraph a, Direction dir, SubQueryContext q){
			this.u = u;
			this.a = a;
			this.dir = dir;
			this.subquery = q;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((dir == null) ? 0 : dir.hashCode());
			result = prime * result + ((subquery == null) ? 0 : subquery.hashCode());
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
			if (subquery == null) {
				if (other.subquery != null)
					return false;
			} else if (!subquery.equals(other.subquery))
				return false;
			if (dir == null) {
				if (other.dir != null)
					return false;
			} else if (!dir.equals(other.dir))
				return false;
			return true;
		}
	}
	
	private class ESGEdge{
		private ESGNode start;
		private ESGNode target;
		private String type;
		public ESGEdge(ESGNode start, ESGNode target, String type){
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
		if(objectToInteger.get(u) != null)
			return objectToInteger.get(u);
		int size = objectToInteger.size()+1;
		objectToInteger.put(u, size);
		return size;
	}
	private class ExplodedSuperGraph{
		private SootMethod method;
		private LinkedList<AccessGraph> facts = new LinkedList<>();
		private LinkedList<ESGNode> nodes = new LinkedList<>();
		private LinkedList<ESGEdge> edges = new LinkedList<>();
		
		ExplodedSuperGraph(SootMethod m){
			this.method = m;
		}
		
		void addNode(ESGNode g){
			if(!nodes.contains(g))
				nodes.add(g);
			if(!facts.contains(g.a))
				facts.add(g.a);
		}
		void addEdge(ESGEdge g){
			addNode(g.start);
			addNode(g.target);
			if(!edges.contains(g))
				edges.add(g);
		}
		
		private JSONObject toJSONObject(){
			JSONObject o = new JSONObject();
			o.put("methodName", method.toString());
			o.put("methodId", id(method));
			JSONArray data = new JSONArray();
			LinkedList<Unit> stmtsList = new LinkedList<>();
			int offset = 0;
			int labelYOffset = 0;
			int charSize=8;
			for(AccessGraph g : facts){
				labelYOffset = Math.max(labelYOffset, charSize*g.toString().length());
			}
			for(Unit u: method.getActiveBody().getUnits()){
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				stmtsList.add(u);
				pos.put("x", 10);
				pos.put("y", stmtsList.size()*30 + labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("shortLabel", getShortLabel(u));
				if(icfg.isCallStmt(u)){
					label.put("callSite", icfg.isCallStmt(u));
					JSONArray callees = new JSONArray();
					for(SootMethod callee : icfg.getCalleesOfCallAt(u))
						callees.add(new Method(callee));
					label.put("callees", callees);	
				}
				label.put("stmtId",id(u));
				
				nodeObj.put("data", label);
				nodeObj.put("classes", "stmt label");
				data.add(nodeObj);
				offset = Math.max(offset,  u.toString().length());
			}
			
			LinkedList<AccessGraph> factsList = new LinkedList<>();
			
			for(AccessGraph u: facts){
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				factsList.add(u);
				pos.put("x", factsList.size()*30 + offset*charSize);
				pos.put("y", labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("factId",id(u));
				nodeObj.put("classes", "fact label");
				nodeObj.put("data", label);
				data.add(nodeObj);
			}
			
			for(ESGNode node: nodes){
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				if(node instanceof CalleeESGNode){
					CalleeESGNode calleeESGNode = (CalleeESGNode) node;
					pos.put("x", (factsList.indexOf(calleeESGNode.linkedNode.a)+1)*30 + 10 + offset*charSize);
					pos.put("y", (stmtsList.indexOf(calleeESGNode.linkedNode.u)+(calleeESGNode.linkedNode.dir == Direction.FORWARD ? 0 : 1))*30+labelYOffset);
				} else{
					assert stmtsList.indexOf(node.u) != -1;
					pos.put("x", (factsList.indexOf(node.a)+1)*30 + offset*charSize);
					pos.put("y", (stmtsList.indexOf(node.u)+(node.dir==Direction.FORWARD ? 0 : 1))*30+ labelYOffset);
				}
				
				nodeObj.put("position", pos);
				String classes = "esgNode method"+id(method)+" sq"+id(node.subquery)+" " +node.dir;

				JSONObject additionalData = new JSONObject();
				additionalData.put("id", "n"+id(node));
				additionalData.put("stmtId", id(node.u));
				additionalData.put("factId", id(node.a));
				for(Subquery q : queries.keySet()){
					if(node.a.equals(q.sq.getAccessPath()) && node.u.equals(q.sq.getStmt()))
						classes += " queryNode query"+id(q) +" ";
				}
				for(Entry<Subquery, AliasResults> q : queries.entrySet()){
					if(!node.u.equals(q.getKey().sq.getStmt()))
						continue;
					if(node.a.equals(q.getKey().sq.getAccessPath())){
						classes += " queryNode queryId"+id(q) +" ";
						additionalData.put("queryId", id(q));
					}
					for(AccessGraph g : q.getValue().values()){
						if(node.a.equals(g)){
							classes +=  " queryId"+id(q) +" ";
							additionalData.put("queryId", id(q));
						}
					}
				}
				nodeObj.put("classes", classes);
				nodeObj.put("group", "nodes");
				nodeObj.put("data", additionalData);
				data.add(nodeObj);
			}
			for(ESGEdge edge: edges){
				JSONObject nodeObj = new JSONObject();
				JSONObject dataEntry = new JSONObject();
				dataEntry.put("id", "e"+id(edge));
				dataEntry.put("source", "n"+id(edge.start));
				dataEntry.put("target", "n"+id(edge.target));
				dataEntry.put("directed", "true");
				dataEntry.put("direction", edge.start.dir.toString());
				nodeObj.put("data", dataEntry);
				nodeObj.put("classes", "esgEdge method"+id(method)+" sq"+id(edge.start.subquery)+" " +edge.start.dir +" "+ edge.type);
				nodeObj.put("group", "edges");
				data.add(nodeObj);
			}
			o.put("data", data);
			return o;
		}

	}

	private String getShortLabel(Unit u) {
		if(u instanceof AssignStmt){
			AssignStmt assignStmt = (AssignStmt) u;
			if(assignStmt.getRightOp() instanceof InstanceFieldRef){
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getRightOp();
				return assignStmt.getLeftOp() + " = "+ fr.getBase() +"."+fr.getField().getName();
			}
			if(assignStmt.getLeftOp() instanceof InstanceFieldRef){
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getLeftOp();
				return fr.getBase() +"."+fr.getField().getName() + " = "+ assignStmt.getRightOp();
			}
		}
		if(u instanceof Stmt && ((Stmt) u).containsInvokeExpr()){
			InvokeExpr invokeExpr =  ((Stmt) u).getInvokeExpr();
			if(invokeExpr instanceof StaticInvokeExpr)
				return (u instanceof AssignStmt ? ((AssignStmt)u).getLeftOp() + " = " : "") + invokeExpr.getMethod().getName()+"(" +invokeExpr.getArgs().toString()+")";
			if(invokeExpr instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr iie = (InstanceInvokeExpr) invokeExpr;
				return (u instanceof AssignStmt ? ((AssignStmt)u).getLeftOp() + " = " : "") + iie.getBase() +"."+ invokeExpr.getMethod().getName() +"(" +invokeExpr.getArgs().toString()+")";
			}
		}
		return u.toString();
	}
	
	private class Method extends JSONObject{
		Method(SootMethod m){
			this.put("name",StringEscapeUtils.escapeHtml4(m.toString()));
			this.put("id", id(m));
		}
	}

	private String escapeHTML(String s){
		return StringEscapeUtils.escapeHtml4(s);
	}
	private class Subquery extends JSONObject{
		private SubQueryContext sq;

		Subquery(SubQueryContext sq, int level){
			this.sq = sq;
			JSONObject data = new JSONObject();
			data.put("stmt", escapeHTML(getShortLabel(sq.getStmt())));
			data.put("method", escapeHTML(sq.getMethod().toString()));
			data.put("fact", escapeHTML(sq.getAccessPath().toString()));
			data.put("factId", id(sq.getAccessPath()));
			data.put("methodId", id(sq.getMethod()));
			data.put("stmtId", id(sq.getStmt()));
			data.put("level", level);
			data.put("label", sq.getAccessPath().toString());
			data.put("id","sq"+id(this));
			this.put("data", data);
			this.put("group", "nodes");
			this.put("subquery", true);
		}


		public void setResults(AliasResults aliasResults) {
			JSONObject obj = (JSONObject)this.get("data");
			JSONObject res = new JSONObject();
			res.put("plain", escapeHTML(aliasResults.toString()));
			JSONArray allocSites = new JSONArray();
			for(Pair<Unit, AccessGraph> key : aliasResults.keySet()){
				JSONObject alloc = new JSONObject();
				alloc.put("stmt", escapeHTML(getShortLabel(key.getO1())));
				alloc.put("stmtId", id(key.getO1()));
				JSONArray values = new JSONArray();
				for(AccessGraph a : aliasResults.get(key)){
					JSONObject o = new JSONObject();
					o.put("fact", escapeHTML(a.toString()));
					o.put("factId", id(a));
					values.add(o);
				}
				alloc.put("values", values);
				allocSites.add(alloc);
			}
			res.put("allocSites",allocSites);
			obj.put("results", res);
		}


		@Override
		public int hashCode() {
			int result =  ((sq == null) ? 0 : sq.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (getClass() != obj.getClass())
				return false;
			Subquery other = (Subquery) obj;
			if (sq == null) {
				if (other.sq != null)
					return false;
			} else if (!sq.equals(other.sq))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "wrapped"+sq.toString();
		}
			}
	
	private class SubqueryEdge extends JSONObject{
		SubqueryEdge(Subquery start, Subquery target){
			JSONObject data = new JSONObject();
			data.put("source","sq"+ id(start));
			data.put("target","sq"+ id(target));
			this.put("data", data);
		}
	}
	@Override
	public void setContext(BoomerangContext boomerangContext) {
		this.context = boomerangContext;		
		this.icfg = context.icfg;
	}
}
