package org.povworld.collection.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface ImmutableTreeSetNode<E, N extends ImmutableTreeSetNode<E, N>> extends ImmutableTreeNode<N> {
    
    /**
     * Gets the element associated with this node.
     */
    public E getElement();
    
}