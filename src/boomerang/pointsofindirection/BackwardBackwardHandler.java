package boomerang.pointsofindirection;

import boomerang.BoomerangContext;
import boomerang.backward.BackwardSolver;

/**
 * This interface is for points of indirection which are discovered within the backward analysis but
 * will trigger a backward pass upon execution.
 * 
 * @author spaeth
 *
 */
public interface BackwardBackwardHandler extends PointOfIndirection{
  /**
   * Execute receives the backward solver as an argument. Normally, the appropriate edges are
   * generated propagated into the solver.
   * 
   * @param solver A backward solver for which the backward path edges are generated
   * @param context The overall context object.
   */
  public void execute(BackwardSolver solver, BoomerangContext context);
}
