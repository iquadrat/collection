package org.povworld.collection.tree;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

/**
 * Base implementation for immutable AVL tree nodes.
 * @param <N> the concrete node type
 */
@Immutable
public abstract class AbstractImmutableAvlTreeNode<N extends AbstractImmutableAvlTreeNode<N>> implements ImmutableAvlTreeNode<N> {
    
    protected final int height;
    
    @CheckForNull
    protected final N left;
    
    @CheckForNull
    protected final N right;
    
    protected AbstractImmutableAvlTreeNode(@CheckForNull N left, @CheckForNull N right) {
        this.left = left;
        this.right = right;
        this.height = Math.max(AvlTreeNode.getHeight(left), AvlTreeNode.getHeight(right)) + 1;
    }
    
    @Override
    public N getLeft() {
        return left;
    }
    
    @Override
    public N getRight() {
        return right;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
    @Override
    public int getBalance() {
        return AvlTreeNode.getHeight(right) - AvlTreeNode.getHeight(left);
    }
    
    @Override
    public String toString() {
        return "[" + left + "," + right + "]";
    }
}
