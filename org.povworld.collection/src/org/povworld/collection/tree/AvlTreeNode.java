package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

public interface AvlTreeNode<N extends AvlTreeNode<N>> extends TreeNode<N> {
    
    public int getHeight();
    
    public default int getBalance() {
        return getHeight(getRight()) - getHeight(getLeft());
    }
    
    public static int getHeight(@CheckForNull AvlTreeNode<?> tree) {
        return tree == null ? 0 : tree.getHeight();
    }
    
}
