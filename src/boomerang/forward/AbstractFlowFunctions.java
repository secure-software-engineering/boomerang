package boomerang.forward;

import java.util.Collection;
import java.util.Set;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import soot.ArrayType;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

public abstract class AbstractFlowFunctions {
	protected BoomerangContext context;
	public static boolean hasCompatibleTypesForCall(AccessGraph apBase, SootClass dest) {
		if(!WrappedSootField.TRACK_TYPE)
			return true;
		// Cannot invoke a method on a primitive type
		if (apBase.getBaseType() instanceof PrimType)
			return false;
		// Cannot invoke a method on an array
		if (apBase.getBaseType() instanceof ArrayType)
			return dest.getName().equals("java.lang.Object");

		return typeCompatible(apBase.getBaseType(), dest.getType());
	}

	protected static boolean typeCompatible(Type a, Type b){
		if(!WrappedSootField.TRACK_TYPE)
			return true;
		return Scene.v().getOrMakeFastHierarchy().canStoreType(a,b)
				|| Scene.v().getOrMakeFastHierarchy().canStoreType(b,a);
	}
	protected boolean isFirstFieldUsedTransitivelyInMethod(AccessGraph source, final SootMethod callee) {
        for(WrappedSootField wrappedField :  source.getFirstField()){
      	  if(context.icfg.isStaticFieldUsed(callee, wrappedField.getField()))
      		  return true;
        }
		return false;
	}
	protected boolean isFirstFieldUsedTransitivelyInMethod(AccessGraph source,  Collection<SootMethod> callees) {
        for(SootMethod callee:  callees){
      	  if(isFirstFieldUsedTransitivelyInMethod(source,callee))
      		  return true;
        }
		return false;
	}
}
