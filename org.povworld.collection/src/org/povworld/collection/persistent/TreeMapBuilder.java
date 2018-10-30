package org.povworld.collection.persistent;

import javax.annotation.CheckForNull;

import org.povworld.collection.tree.ImmutableTreeMapNode;
import org.povworld.collection.tree.TreeBuilder;

public interface TreeMapBuilder<K, V, N extends ImmutableTreeMapNode<K, V, N>> extends TreeBuilder<N> {
    public N createNode(@CheckForNull N left, @CheckForNull N right, K key, V value);
}