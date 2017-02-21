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
}
