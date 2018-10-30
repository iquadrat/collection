package org.povworld.collection.tree;

import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.povworld.collection.common.Assert;
import org.povworld.collection.common.PreConditions;

/**
 * Base class for immutable AVL tree builders.
 * 
 * @param <N> the concrete node type
 */
public abstract class AbstractAvlTreeBuilder<N extends ImmutableAvlTreeNode<N>> extends AbstractTreeBuilder<N> implements TreeBuilder<N> {
    
    @Override
    protected int getEstimatedHeight(N node) {
        return node.getHeight();
    }
    
    @Override
    public N balance(@CheckForNull N left, N top, @CheckForNull N right) {
        int balance = height(right) - height(left);
        PreConditions.conditionCheck("Invalid tree balance!", (balance >= -2) && (balance <= 2));
        if (balance == 2) {
            
            if (right.getBalance() == 1) {
                return rotateLeft(left, top, right);
            } else if (right.getBalance() == -1) {
                return doubleRotateLeft(left, top, right);
            } else if (right.getBalance() == 0) {
                // happens only after remove, therefore left balance must be 0
                if (left != null) Assert.assertEquals(0, left.getBalance());
                return rotateLeft(left, top, right);
            } else {
                throw Assert.fail("unexpected balance: " + right.getBalance());
            }
            
        }
        if (balance == -2) {
            if (left.getBalance() == -1) {
                // happens only after add
                return rotateRight(left, top, right);
            } else if (left.getBalance() == 1) {
                // happens only after add
                return doubleRotateRight(left, top, right);
            } else if (left.getBalance() == 0) {
                // happens only after remove, therefore right balance must be 0
                if (right != null) Assert.assertEquals(0, right.getBalance());
                return rotateRight(left, top, right);
            } else {
                throw Assert.fail("unexpected balance: " + left.getBalance());
            }
        }
        
        return createSubTree(left, top, right);
    }
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public void checkInvariants(N tree) {
        if (tree == null) {
            return;
        }
        
        checkInvariants(tree.getLeft());
        checkInvariants(tree.getRight());
        
        // check for balance mismatch
        int lh = height(tree.getLeft());
        int rh = height(tree.getRight());
        Assert.assertTrue(Math.abs(lh - rh) <= 1, "Balance violation");
        
        // check height
        int rootHeight = 1 + Math.max(lh, rh);
        Assert.assertEquals(rootHeight, tree.getHeight());
    }
    
    private int height(N node) {
        return node == null ? 0 : node.getHeight();
    }
    
    private N rotateLeft(N left, N top, N right) {
        N newLeft = createSubTree(left, top, right.getLeft());
        return createSubTree(newLeft, right, right.getRight());
    }
    
    private N doubleRotateLeft(N left, N top, N right) {
        N rightLeft = right.getLeft();
        N newLeft = createSubTree(left, top, rightLeft.getLeft());
        N newRight = createSubTree(rightLeft.getRight(), right, right.getRight());
        return createSubTree(newLeft, rightLeft, newRight);
    }
    
    private N rotateRight(N left, N top, N right) {
        N newRight = createSubTree(left.getRight(), top, right);
        return createSubTree(left.getLeft(), left, newRight);
    }
    
    private N doubleRotateRight(N left, N top, N right) {
        N leftRight = left.getRight();
        N newLeft = createSubTree(left.getLeft(), left, leftRight.getLeft());
        N newRight = createSubTree(leftRight.getRight(), top, right);
        return createSubTree(newLeft, leftRight, newRight);
    }
}
