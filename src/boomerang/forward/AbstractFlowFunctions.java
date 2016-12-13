package boomerang.forward;

import boomerang.accessgraph.AccessGraph;
import soot.ArrayType;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.Type;

public abstract class AbstractFlowFunctions {

	public static boolean hasCompatibleTypesForCall(AccessGraph apBase, SootClass dest) {
		// Cannot invoke a method on a primitive type
		if (apBase.getBaseType() instanceof PrimType)
			return false;
		// Cannot invoke a method on an array
		if (apBase.getBaseType() instanceof ArrayType)
			return dest.getName().equals("java.lang.Object");

		return typeCompatible(apBase.getBaseType(), dest.getType());
	}

	protected static boolean typeCompatible(Type a, Type b){
		return Scene.v().getOrMakeFastHierarchy().canStoreType(a,b)
				|| Scene.v().getOrMakeFastHierarchy().canStoreType(b,a);
	}
}
