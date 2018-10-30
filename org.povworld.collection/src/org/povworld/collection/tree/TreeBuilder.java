package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

import org.povworld.collection.Comparator;

/**
 * Allows to build new trees based off from existing immutable trees.
 * 
 * @param <N> the node type
 * @see TreeManipulator
 */
public interface TreeBuilder<N extends ImmutableTreeNode<N>> {
    
    public Path<N> pathTo(N tree, N node, Comparator<? super N> comparator);
    
    @CheckForNull
    public N remove(Path<N> path);
    
    public N replace(Path<N> path, N node);
    
    /**
     * Creates a new subtree equivalent to the tree constructed from the given {@code top} node
     * with {@code left} and {@code right} children.
     * 
     * @return the new top node
     */
    public N createSubTree(@CheckForNull N left, N top, @CheckForNull N right);
    
    public void checkInvariants(@CheckForNull N tree);
    
}
