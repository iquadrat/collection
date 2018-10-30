package org.povworld.collection.tree;

import javax.annotation.concurrent.Immutable;

@Immutable
public interface ImmutableTreeMapNode<K, V, N extends ImmutableTreeMapNode<K, V, N>> extends ImmutableTreeNode<N> {
    
    /**
     * Gets the key associated with this node.
     */
    public K getKey();
    
    /**
     * Gets the value associated with this node.
     */
    public V getValue();
    
}