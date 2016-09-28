/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE. All rights reserved. This
 * program and the accompanying materials are made available under the terms of the GNU Lesser
 * Public License v2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric Bodden, and others.
 ******************************************************************************/
package boomerang.mock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import soot.Local;
import soot.Value;
import soot.jimple.Stmt;

public class DefaultNativeCallHandler extends NativeCallHandler {
	
	@Override
	public Set<AccessGraph> getForwardValues(Stmt call, AccessGraph source, Value[] params){
		//check some evaluated methods:
		
		//arraycopy:
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position,
		//to the specified position of the destination array.
		if(call.getInvokeExpr().getMethod().toString().contains("arraycopy"))
			if(params[0].equals(source.getBase()) && params[2] instanceof Local) {
				AccessGraph copied = source.deriveWithNewLocal((Local)params[2], source.getBaseType());
				Set<AccessGraph> out = new HashSet<>();
				out.add(source);
				out.add(copied);
				return out;
			}
		
		return Collections.emptySet();
	}
	@Override
	public Set<AccessGraph> getBackwardValues(Stmt call, AccessGraph source, Value[] params){
		//check some evaluated methods:
		
		//arraycopy:
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position,
		//to the specified position of the destination array.
		if(call.getInvokeExpr().getMethod().toString().contains("arraycopy"))
			if(params[2].equals(source.getBase()) && params[0] instanceof Local) {
				AccessGraph copied = source.deriveWithNewLocal((Local)params[0], source.getBaseType());
				Set<AccessGraph> out = new HashSet<>();
				out.add(copied);
				return out;
			}
		
		return Collections.emptySet();
	}
}
