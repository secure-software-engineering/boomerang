package boomerang.cache;

import boomerang.accessgraph.AccessGraph;
import soot.SootMethod;
import soot.Type;
import soot.Unit;

public class Query {

  private AccessGraph accessPath;
  private Unit stmt;
  private SootMethod method;

  public Query(AccessGraph accessPath, Unit stmt, SootMethod method) {
    this.accessPath = accessPath;
    this.stmt = stmt;
    this.method = method;
  }

  public Query(AccessGraph accessPath, Unit stmt) {
    this(accessPath, stmt, null);
  }

  public Unit getStmt() {
    return stmt;
  }

  public AccessGraph getAp() {
    return accessPath;
  }

  public SootMethod getMethod() {
    return method;
  }

  @Override
  public String toString() {
    return accessPath + "@" + stmt + "€" + method;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
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
    Query other = (Query) obj;
    if (accessPath == null) {
      if (other.accessPath != null)
        return false;
    } else if (!accessPath.equals(other.accessPath))
      return false;
    if (stmt == null) {
      if (other.stmt != null)
        return false;
    } else if (!stmt.equals(other.stmt))
      return false;
    return true;
  }

  public Type getType() {
    return accessPath.getType();
  }
}
