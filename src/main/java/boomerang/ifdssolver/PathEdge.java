/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1 which accompanies this
 * distribution, and is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Eric Bodden - initial API and implementation
 ******************************************************************************/
package boomerang.ifdssolver;

import heros.InterproceduralCFG;
import heros.solver.Pair;
import soot.Unit;

/**
 * A path edge as described in the IFDS/IDE algorithms. The source node is implicit: it can be
 * computed from the target by using the {@link InterproceduralCFG}. Hence, we don't store it.
 *
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 */
public class PathEdge<N, D> implements IPathEdge<N, D> {

  private final N target;
  private final N start;
  private final D dSource, dTarget;
  private final int hashCode;

  /**
   * @param dSource The fact at the source.
   * @param target The target statement.
   * @param dTarget The fact at the target.
   */
  public PathEdge(N start, D dSource, N target, D dTarget) {
	super();
	assert target != null;
    this.target = target;
    this.start = start;
    this.dSource = dSource;
    this.dTarget = dTarget;

    final int prime = 31;
    int result = 1;
    result = prime * result + ((dSource == null) ? 0 : dSource.hashCode());
    result = prime * result + ((dTarget == null) ? 0 : dTarget.hashCode());
    result = prime * result + ((start == null) ? 0 : start.hashCode());
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    this.hashCode = result;
    assert target != null;
  }

  public N getTarget() {
    return target;
  }

  public N getStart() {
    return start;
  }


  public D factAtSource() {
    return dSource;
  }

  public D factAtTarget() {
    return dTarget;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    PathEdge other = (PathEdge) obj;
    if (dSource == null) {
      if (other.dSource != null)
        return false;
    } else if (!dSource.equals(other.dSource))
      return false;
    if (dTarget == null) {
      if (other.dTarget != null)
        return false;
    } else if (!dTarget.equals(other.dTarget))
      return false;
    if (target == null) {
      if (other.target != null)
        return false;
    } else if (!target.equals(other.target))
      return false;
    if (start == null) {
      if (other.start != null)
        return false;
    } else if (!start.equals(other.start))
      return false;
    assert this.hashCode() == other.hashCode();
    return true;
  }

  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("<");
    result.append(dSource);
    result.append("@");
    result.append(start);
    result.append("> -> <");
    result.append(dTarget);
    result.append("@");
    result.append(target.toString());
    result.append(">");
    return result.toString();
  }

  @Override
  public Pair<N, D> getStartNode() {
    return new Pair<N, D>(start, dSource);
  }

  @Override
  public Pair<N, D> getTargetNode() {
    return new Pair<N, D>(target, dTarget);
  }

}
