package tests.fields.loops;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class LoopsWithFieldsIntra extends AbstractBoomerangTest{
	@Test
	public void twoFields() {
		Node x = new Node();
		Node p = null;
		while(staticallyUnknown()){
			if(staticallyUnknown()){
				x.left.right = p;
				
			}else{
				x.right.left = p;
			}
			p = x;
		}
		Node t;
		if(staticallyUnknown()){
			t = x.left.right;
			
		}else{
			t = x.right.left;
		}
		Node h = t;
		queryFor(h);
	}
	@Test
	public void threeFields() {
		TreeNode x = new TreeNode();
		TreeNode p = null;
		while(staticallyUnknown()){
			if(staticallyUnknown()){
				x.left.right = p;
				
			}else if(staticallyUnknown()){
				x.right.left = p;
			} else{
				TreeNode u = x.parent;
				x = u;
			}
			p = x;
		}
		TreeNode t;
		if(staticallyUnknown()){
			t = x.left.right;
			
		}else{
			t = x.right.left;
		}
		TreeNode h = t;
		queryFor(h);
	}
	private class Node extends AllocatedObject{
		Node left = new Node();
		Node right = new Node();
	}

	private class TreeNode extends AllocatedObject{
		TreeNode left = new TreeNode();
		TreeNode right = new TreeNode();
		TreeNode parent = new TreeNode();
	}
}
