package example;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.context.Context;
import boomerang.context.IContextRequester;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;

public class ClientDrivenContextResolution implements IContextRequester {

	private InfoflowCFG icfg;

	public ClientDrivenContextResolution(InfoflowCFG icfg) {
		this.icfg = icfg;
	}

	@Override
	public Collection<Context> getCallSiteOf(Context child) {
		SootMethod m = icfg.getMethodOf(child.getStmt());
		Collection<Unit> callersOf = icfg.getCallersOf(m);
		Set<Context> res = new HashSet<>();
		for (Unit callSite : callersOf) {
			// Select the call sites the analysis shall continue in.
			if (!callSite.toString().contains("nonAliasedObject"))
				continue;
			res.add(new ClientContext(callSite));
		}
		return res;
	}

	@Override
	public Context initialContext(Unit stmt) {
		return new ClientContext(stmt);
	}

	private class ClientContext implements Context {
		private Unit stmt;

		public ClientContext(Unit stmt) {
			this.stmt = stmt;
		}

		@Override
		public Unit getStmt() {
			return stmt;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
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
			ClientContext other = (ClientContext) obj;
			if (stmt == null) {
				if (other.stmt != null)
					return false;
			} else if (!stmt.equals(other.stmt))
				return false;
			return true;
		}

	}
}
