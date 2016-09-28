package boomerang.pointsofindirection;

import boomerang.BoomerangContext;
import boomerang.forward.ForwardSolver;

/**
 * This interface is for points of indirection which are discovered within the backward analysis but
 * will trigger forward passes. {@see Alloc}
 * 
 * @author spaeth
 *
 */
public interface BackwardForwardHandler extends PointOfIndirection{
  /**
   * Gets an instance of the forward solver which will be used to do the appropriate forward
   * propagation.
   * 
   * @param solver The forward solver to be used.
   * @param context The general context object
   */
  public void execute(ForwardSolver solver, BoomerangContext context);
}
