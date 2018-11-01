package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

public interface MutableTreeNode<N extends MutableTreeNode<N>> extends TreeNode<N> {
    
    /**
     * Changes the left child.
     * @param left the new left node
     */
    public void setLeft(@CheckForNull N left);
    
    /**
     * Changes the right child.
     * @param right the new right node
     */
    public void setRight(@CheckForNull N right);
    
}