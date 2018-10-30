package org.povworld.collection.tree;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.povworld.collection.Comparator;
import org.povworld.collection.tree.Path.Builder;

public class TreeUtil {
    
    private TreeUtil() {}
    
    // TODO test vs non-recursive implementation
    @CheckForNull
    public static <N extends TreeNode<N>> N getMaxNode(@CheckForNull N subTree) {
        if (subTree == null) {
            return null;
        }
        if (subTree.getRight() == null) {
            return subTree;
        }
        return getMaxNode(subTree.getRight());
    }
    
    @CheckForNull
    public static <N extends TreeNode<N>> N getMinNode(@CheckForNull N subTree) {
        if (subTree == null) {
            return null;
        }
        if (subTree.getLeft() == null) {
            return subTree;
        }
        return getMinNode(subTree.getLeft());
    }
    
    // TODO test vs non-recursive implementation
    @CheckForNull
    public static <N extends TreeNode<N>> N findNode(@CheckForNull N subTree, N node, Comparator<? super N> comparator) {
        while (subTree != null) {
            int cmp = comparator.compare(subTree, node);
            if (cmp < 0) {
                subTree = subTree.getRight();
            } else if (cmp > 0) {
                subTree = subTree.getLeft();
            } else {
                return subTree;
            }
        }
        return null;
    }
    
    public static <N extends TreeNode<N>> Path<N> pathToMin(@CheckForNull N tree, int estimatedLength) {
        Builder<N> path = Path.newBuilder(tree, estimatedLength);
        for (N node = tree.getLeft(); node != null; node = node.getLeft()) {
            path.append(true, node);
        }
        return path.build();
    }
    
    public static <N extends TreeNode<N>> Path<N> pathToMax(@CheckForNull N tree, int estimatedLength) {
        Builder<N> path = Path.newBuilder(tree, estimatedLength);
        for (N node = tree.getRight(); node != null; node = node.getRight()) {
            path.append(false, node);
        }
        return path.build();
    }
    
    public static <N extends TreeNode<N>> Path<N> pathTo(@CheckForNull N tree, N node, Comparator<? super N> comparator, int estimatedHeight) {
        N subTree = tree;
        Path.Builder<N> builder = Path.newBuilder(subTree, estimatedHeight);
        int matchLength = -1;
        while (subTree != null) {
            int cmp = comparator.compare(subTree, node);
            boolean left;
            if (cmp >= 0) {
                subTree = subTree.getLeft();
                left = true;
                if (cmp == 0) {
                    matchLength = builder.length();
                }
            } else { // cmp <= 0
                subTree = subTree.getRight();
                left = false;
            }
            builder.append(left, subTree);
        }
        if (matchLength != -1) {
            builder.limitLength(matchLength);
        }
        return builder.build();
    }
    
    public static <N extends TreeNode<N>> boolean validateOrder(@CheckForNull N tree, Comparator<? super N> comparator) {
        if (tree == null) {
            return true;
        }
        // check for invalid binary search tree
        if ((tree.getLeft() == null || comparator.compare(tree.getLeft(), tree) <= 0) &&
                (tree.getRight() == null || comparator.compare(tree.getRight(), tree) >= 0)) {
            return validateOrder(tree.getLeft(), comparator) && validateOrder(tree.getRight(), comparator);
        } else {
            return false;
        }
    }
    
    public static <N extends TreeNode<N>> Iterator<N> iterateNodes(@CheckForNull N root) {
        return iterateNodes(root, TreeIterator.DEFAULT_HEIGHT);
    }
    
    public static <N extends AvlTreeNode<N>> Iterator<N> iterateNodes(@CheckForNull N root) {
        return iterateNodes(root, AvlTreeNode.getHeight(root));
    }
    
    public static <N extends TreeNode<N>> Iterator<N> iterateNodes(@CheckForNull N root, int estimatedHeight) {
        return new TreeIterator<N, N>(root, estimatedHeight) {
            @Override
            protected N getElement(N node) {
                return node;
            }
        };
    }
    
    public static <N extends TreeNode<N>> Iterator<N> iterateNodesPostOrder(@CheckForNull N root) {
        return new PostOrderTreeIterator<N, N>(root) {
            @Override
            protected N getElement(N node) {
                return node;
            }
        };
    }
    
    public static <N extends TreeNode<N>> Iterator<N> reverseIterateNodes(@CheckForNull N root) {
        return reverseIterateNodes(root, TreeIterator.DEFAULT_HEIGHT);
    }
    
    public static <N extends AvlTreeNode<N>> Iterator<N> reverseIterateNodes(@CheckForNull N root) {
        return reverseIterateNodes(root, AvlTreeNode.getHeight(root));
    }
    
    public static <N extends TreeNode<N>> Iterator<N> reverseIterateNodes(@CheckForNull N root, int estimatedHeight) {
        return new ReverseTreeIterator<N, N>(root, estimatedHeight) {
            @Override
            protected N getElement(N node) {
                return node;
            }
        };
    }
    
}
