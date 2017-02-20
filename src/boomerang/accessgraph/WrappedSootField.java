package boomerang.accessgraph;

import soot.SootField;
import soot.Type;
import soot.Unit;

/**
 * A wrapped SootField. It can have a more precise type information than the original SootField.
 * 
 * @author spaeth
 *
 */
public class WrappedSootField {
  private SootField field;
  private Type type;
  private Unit stmt;
  public static boolean TRACK_TYPE = true;
  public static boolean TRACK_STMT = true;

  public WrappedSootField(SootField f, Type t, Unit s) {
    this.field = f;
    this.type = (TRACK_TYPE ? t : null);
    this.stmt = (TRACK_STMT ? s : null);
  }

  public SootField getField() {
    return field;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    if(TRACK_TYPE){
    	result = prime * result + ((type == null) ? 0 : type.hashCode());
    }
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
    WrappedSootField other = (WrappedSootField) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if(TRACK_TYPE){
	    if (type == null) {
	      if (other.type != null)
	        return false;
	    } else if (!type.equals(other.type))
	      return false;
    }
    if (stmt == null) {
      if (other.stmt != null)
        return false;
    } else if (!stmt.equals(other.stmt))
      return false;
    return true;
  }

  public String toString() {
    return field.getName().toString() ;//+ "(" + type + ")";// + (stmt != null ? "@" + stmt
  }

  public Type getType() {
    return type;
  }
}
