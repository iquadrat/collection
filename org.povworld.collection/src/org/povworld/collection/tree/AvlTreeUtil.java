package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

import org.povworld.collection.common.Assert;
import org.povworld.collection.common.PreConditions;

public class AvlTreeUtil {
    /**
     * Re-balances the given subtree. It is assumed that the left and right sub-trees of the
     * {@code top} node are already balanced but the top node's balance is off.
     * <p>
     * As the balancing can involve rotations the root node of the subtree can change.
     * Therefore, the new root is returned from the method.
     */
    public static <N extends MutableAvlTreeNode<N>> N balance(N top) {
        N left = top.getLeft();
        N right = top.getRight();
        PreConditions.paramCheck(top, "left is not top's left", left == top.getLeft());
        PreConditions.paramCheck(top, "right is not top's right", right == top.getRight());
        int balance = height(right) - height(left);
        if (balance == 2) {
            
            if (right.getBalance() == 1) {
                return rotateLeft(top, right);
            } else if (right.getBalance() == -1) {
                return doubleRotateLeft(top, right);
            } else {
                // happens only after remove, balances must be 0
                Assert.assertTrue(right.getBalance() == 0, "Invalid balance!");
                Assert.assertTrue(left == null || left.getBalance() == 0, "Invalid balance!");
                return rotateLeft(top, right);
            }
            
        }
        if (balance == -2) {
            if (left.getBalance() == -1) {
                // happens only after add
                return rotateRight(left, top);
            } else if (left.getBalance() == 1) {
                // happens only after add
                return doubleRotateRight(left, top);
            } else {
                // happens only after remove, balances must be 0
                Assert.assertTrue(left.getBalance() == 0, "Invalid balance!");
                Assert.assertTrue(right == null || right.getBalance() == 0, "Invalid balance!");
                return rotateRight(left, top);
            }
        }
        
        return top;
    }
    
    private static <N extends MutableAvlTreeNode<N>> N rotateLeft(N top, N right) {
        top.setRight(right.getLeft());
        right.setLeft(top);
        return right;
    }
    
    private static <N extends MutableAvlTreeNode<N>> N doubleRotateLeft(N top, N right) {
        N rightLeft = right.getLeft();
        
        top.setRight(rightLeft.getLeft());
        right.setLeft(rightLeft.getRight());
        rightLeft.setLeft(top);
        rightLeft.setRight(right);
        
        return rightLeft;
    }
    
    private static <N extends MutableAvlTreeNode<N>> N rotateRight(N left, N top) {
        top.setLeft(left.getRight());
        left.setRight(top);
        return left;
    }
    
    private static <N extends MutableAvlTreeNode<N>> N doubleRotateRight(N left, N top) {
        N leftRight = left.getRight();
        
        top.setLeft(leftRight.getRight());
        left.setRight(leftRight.getLeft());
        leftRight.setLeft(left);
        leftRight.setRight(top);
        
        return leftRight;
    }
    
    private static <N extends MutableAvlTreeNode<N>> int height(@CheckForNull N node) {
        return (node == null) ? 0 : node.getHeight();
    }
}
