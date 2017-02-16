package boomerang.pointsofindirection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import soot.Unit;

public class Return implements ForwardPointOfIndirection {

  private Unit callSite;
  private AccessGraph outcomings;

  public Return(Unit callSite, AccessGraph outcomings) {
    assert callSite != null;
    this.callSite = callSite;
    this.outcomings = outcomings;
  }

  @Override
  public String toString() {
    return "ForwardExitQuery(" + callSite + "," + outcomings + ")";
  }

  @Override
  public Set<AccessGraph> process(BoomerangContext context) {
	  context.debugger.onProcessReturnPOI(this);
    assert context.bwicfg.isCallStmt(callSite);
    Collection<WrappedSootField> lastFields = outcomings.getLastField();
    Set<AccessGraph> newAliases = new HashSet<>();
    Set<AccessGraph> withOutLastFields = outcomings.popLastField();
    for(WrappedSootField lastField :lastFields){
	    
	    for (AccessGraph withOutLastField : withOutLastFields) {
	      AliasFinder dart = new AliasFinder(context);
	      Collection<AccessGraph> irRes = dart.findAliasAtStmtRec(withOutLastField, callSite);
	      newAliases.addAll(AliasResults.appendField(irRes, lastField, context));
	    }
    }
    return newAliases;
    
  }

  public boolean isValid(BoomerangContext context) {
    if (outcomings.getFieldCount() == 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((callSite == null) ? 0 : callSite.hashCode());
    result = prime * result + ((outcomings == null) ? 0 : outcomings.hashCode());
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
    Return other = (Return) obj;
    if (callSite == null) {
      if (other.callSite != null)
        return false;
    } else if (!callSite.equals(other.callSite))
      return false;
    if (outcomings == null) {
      if (other.outcomings != null)
        return false;
    } else if (!outcomings.equals(other.outcomings))
      return false;
    return true;
  }

  @Override
  public Unit getStmt() {
    return callSite;
  }
}
