/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 ******************************************************************************/
package boomerang.ifdssolver;

import heros.solver.Pair;
import heros.InterproceduralCFG;

/**
 * A path edge as described in the IFDS/IDE algorithms.
 * The source node is implicit: it can be computed from the target by using the {@link InterproceduralCFG}.
 * Hence, we don't store it.
 *
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 */
public interface IPathEdge<N,D> {

	public N getTarget();
	public N getStart();
	public D factAtSource();
	public D factAtTarget();

	public Pair<N,D> getStartNode();
	public Pair<N,D> getTargetNode();
}
