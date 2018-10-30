package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

/**
 * Represents a single node of a binary tree. 
 *
 * @param <N> The node type itself, this is the concrete type implementing the interface.
 */
public interface TreeNode<N extends TreeNode<N>> {
    /**
     * Gets the node's left sub-tree or returns {@code null} if it is a leaf. 
     */
    @CheckForNull
    public N getLeft();
    
    /**
     * Gets the node's right sub-tree or returns {@code null} if it is a leaf.
     */
    @CheckForNull
    public N getRight();
}
