package org.povworld.collection.tree;

import javax.annotation.concurrent.Immutable;

/**
 * A binary tree node that is immutable.
 *
 * @param <N> The node type itself, this is the concrete type implementing the interface.
 **/
@Immutable
public interface ImmutableTreeNode<N extends ImmutableTreeNode<N>> extends TreeNode<N> {
    
}
