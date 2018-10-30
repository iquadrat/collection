package org.povworld.collection.tree;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.mutable.ArrayList;

public class AvlTree<E, N extends MutableAvlTreeNode<N>> {
    
    private final Comparator<? super E> comparator;
    private final NodeElementTransformer<E, N> transformer;
    
    @CheckForNull
    private N root = null;
    
    public AvlTree(Comparator<? super E> comparator, NodeElementTransformer<E, N> manipulator) {
        this.comparator = comparator;
        this.transformer = manipulator;
    }
    
    public static <N extends MutableAvlTreeNode<N>> AvlTree<N,N> create(Comparator<? super N> comparator) {
        return new AvlTree<N,N>(comparator, NodeIsElementTransformer.getInstance());
    }
    
    @CheckForNull
    public N find(E element) {
        return find(element, comparator);
    }
    
    @CheckForNull
    // Performance of a recursive implementation is about the same.
    public N find(E element, Comparator<? super E> comparator) {
        N subTree = root;
        while (subTree != null) {
            int cmp = comparator.compare(element, transformer.getElement(subTree));
            if (cmp == 0) {
                return subTree;
            }
            if (cmp < 0) {
                subTree = subTree.getLeft();
            } else {
                subTree = subTree.getRight();
            }
        }
        return null;
    }
    
    private ArrayList<N> pathToFirst(N root, E element, Comparator<? super E> comparator) {
        ArrayList<N> result = new ArrayList<>(root.getHeight());
        N subTree = root;
        while (subTree != null) {
            result.add(subTree);
            int cmp = comparator.compare(transformer.getElement(subTree), element);
            if (cmp >= 0) {
                subTree = subTree.getLeft();
            } else {
                subTree = subTree.getRight();
            }
        }
        while (!comparator.equals(element, transformer.getElement(result.peek()))) {
            result.pop();
        }
        return result;
    }
    
    private class FindElementTreeIterator extends TreeIterator<N, N> {
        
        private final Comparator<? super E> comparator;
        private final E element;
        
        protected FindElementTreeIterator(ArrayList<N> stack, Comparator<? super E> comparator, E element) {
            super(stack);
            this.comparator = comparator;
            this.element = element;
        }
        
        @Override
        public boolean hasNext() {
            return super.hasNext() && comparator.equals(element, transformer.getElement(currentNode()));
        }
        
        @Override
        protected N getElement(N node) {
            return node;
        }
    }
    
    public Iterator<N> findAll(E element) {
        return findAll(element, comparator);
    }
    
    public Iterator<N> findAll(E element, Comparator<? super E> comparator) {
        N top = find(element);
        if (top == null) {
            return CollectionUtil.emptyIterator();
        }
        ArrayList<N> path = pathToFirst(top, element, comparator);
        return new FindElementTreeIterator(path, comparator, element);
    }
    
    @CheckForNull
    public E getMaxElement() {
        if (root == null) {
            return null;
        }
        return transformer.getElement(TreeUtil.getMaxNode(root));
    }
    
    @CheckForNull
    public E getMinElement() {
        if (root == null) {
            return null;
        }
        return transformer.getElement(TreeUtil.getMinNode(root));
    }
    
    public void insert(N node) {
        root = insert(root, node);
    }
    
    private N insert(@CheckForNull N subTree, N node) {
        if (subTree == null) {
            return node;
        }
        int cmp = comparator.compare(transformer.getElement(node), transformer.getElement(subTree));
        if (cmp <= 0) {
            N newSubTree = insert(subTree.getLeft(), node);
            subTree.setLeft(newSubTree);
            return AvlTreeUtil.balance(subTree);
        }
        // cmp > 0
        N newSubTree = insert(subTree.getRight(), node);
        subTree.setRight(newSubTree);
        return AvlTreeUtil.balance(subTree);
    }
    
    public boolean insertIfNotPresent(E element) {
        N newRoot = insertIfNotPresent(root, element);
        if (newRoot == null) {
            return false;
        }
        root = newRoot;
        return true;
    }
    
    @CheckForNull
    // This recursive formulation performs better than storing the path in arrays.
    private N insertIfNotPresent(@CheckForNull N subTree, E element) {
        if (subTree == null) {
            return transformer.createNode(element);
        }
        int cmp = comparator.compare(element, transformer.getElement(subTree));
        if (cmp == 0) {
            return null;
        }
        if (cmp < 0) {
            N newSubTree = insertIfNotPresent(subTree.getLeft(), element);
            if (newSubTree == null) {
                return null;
            }
            subTree.setLeft(newSubTree);
            return AvlTreeUtil.balance(subTree);
        }
        // cmp > 0
        N newSubTree = insertIfNotPresent(subTree.getRight(), element);
        if (newSubTree == null) {
            return null;
        }
        subTree.setRight(newSubTree);
        return AvlTreeUtil.balance(subTree);
    }
    
    @CheckForNull
    public N remove(E element) {
        return _remove(root, element);
    }
    
    @CheckForNull
    private N _remove(N subTree, E element) {
        if (subTree == null) {
            // Element not found
            return null;
        }
        int cmp = comparator.compare(element, transformer.getElement(subTree));
        if (cmp < 0) {
            N removed = _remove(subTree.getLeft(), element);
            if (removed == null) {
                return null;
            }
            subTree.setLeft(root);
            root = AvlTreeUtil.balance(subTree);
            return removed;
        }
        if (cmp > 0) {
            N removed = _remove(subTree.getRight(), element);
            if (removed == null) {
                return null;
            }
            subTree.setRight(root);
            root = AvlTreeUtil.balance(subTree);
            return removed;
        }
        
        // found the element to remove
        if (subTree.getRight() == null) {
            root = subTree.getLeft();
            return subTree;
        }
        
        // replace the root of the tree with the successor's element
        N successor = removeMinNode(subTree.getRight());
        successor.setLeft(subTree.getLeft());
        successor.setRight(root);
        root = AvlTreeUtil.balance(successor);
        return subTree;
    }
    
    private N removeMinNode(N subTree) {
        if (subTree.getLeft() == null) {
            // Found min node
            root = subTree.getRight();
            return subTree;
        } else {
            N successor = removeMinNode(subTree.getLeft());
            subTree.setLeft(root);
            root = AvlTreeUtil.balance(subTree);
            return successor;
        }
    }
    
    public void clear() {
        root = null;
    }
    
    @CheckForNull
    public N getRoot() {
        return root;
    }
    
}
