package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

/**
 * Base classes for AVL tree nodes.
 * @param <N> the concrete node type
 */
public abstract class AbstractAvlTreeNode<N extends MutableAvlTreeNode<N>> implements MutableAvlTreeNode<N> {
    
    protected int height;
    
    @CheckForNull
    protected N left;
    
    @CheckForNull
    protected N right;
    
    protected AbstractAvlTreeNode() {
        this(null, null);
    }
    
    protected AbstractAvlTreeNode(@CheckForNull N left, @CheckForNull N right) {
        this.left = left;
        this.right = right;
        updateHeight();
    }
    
    private void updateHeight() {
        height = Math.max(AvlTreeNode.getHeight(left), AvlTreeNode.getHeight(right)) + 1;
    }
    
    @Override
    public N getLeft() {
        return left;
    }
    
    @Override
    public void setLeft(@CheckForNull N left) {
        this.left = left;
        updateHeight();
    }
    
    @Override
    @CheckForNull
    public N getRight() {
        return right;
    }
    
    @Override
    public void setRight(@CheckForNull N right) {
        this.right = right;
        updateHeight();
    }
    
    @Override
    public int getHeight() {
        return height;
    }
}
