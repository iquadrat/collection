package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

import org.povworld.collection.Comparator;
import org.povworld.collection.common.PreConditions;

public abstract class AbstractTreeBuilder<N extends ImmutableTreeNode<N>> implements TreeBuilder<N> {
    
    protected abstract int getEstimatedHeight(N node);
    
    protected abstract N balance(@CheckForNull N left, N top, @CheckForNull N right);
    
    @Override
    @CheckForNull
    public N remove(Path<N> path) {
        PreConditions.paramCheck(path, "Path must not end at a leaf!", path.getEnd() != null);
        int estimatedLength = getEstimatedHeight(path.getStart());
        N subTree = path.getEnd();
        if (subTree.getRight() != null) {
            // replace the root of the tree with the successor node
            Path<N> successorPath = TreeUtil.pathToMin(subTree.getRight(), Math.max(4, estimatedLength - path.length()));
            N newRight = remove(successorPath);
            subTree = balance(subTree.getLeft(), successorPath.getEnd(), newRight);
        } else {
            subTree = subTree.getLeft();
        }
        return replace(path, subTree);
    }
    
    @Override
    public N replace(Path<N> path, N subTree) {
        for (int i = path.length() - 1; i >= 0; i--) {
            N parent = path.getNode(i);
            if (path.isLeft(i)) {
                subTree = balance(subTree, parent, parent.getRight());
            } else {
                subTree = balance(parent.getLeft(), parent, subTree);
            }
        }
        return subTree;
    }
    
    @Override
    public Path<N> pathTo(@CheckForNull N tree, N node, Comparator<? super N> comparator) {
        return TreeUtil.pathTo(tree, node, comparator, getEstimatedHeight(tree));
    }
    
}
