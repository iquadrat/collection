package org.povworld.collection.persistent;

import org.povworld.collection.tree.ImmutableTreeSetNode;
import org.povworld.collection.tree.TreeBuilder;

public interface TreeSetBuilder<E, N extends ImmutableTreeSetNode<E, N>> extends TreeBuilder<N> {
    
    /**
     * Creates a new node with the given element and leaf children.
     */
    public N createNode(E element);
    
}