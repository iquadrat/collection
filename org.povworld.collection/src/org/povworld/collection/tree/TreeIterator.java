package org.povworld.collection.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.mutable.ArrayList;

/**
 * In-order traversal of the tree nodes extracting some element from each node.
 * @param E the type of the extracted element
 * @param N the tree node type
 */
@NotThreadSafe
public abstract class TreeIterator<E, N extends TreeNode<N>> implements Iterator<E> {
    
    public static final int DEFAULT_HEIGHT = 12;
    
    // Invariant: The left subtrees of the stack top has already been visited
    private final ArrayList<N> stack;
    
    protected TreeIterator(@CheckForNull N root, int estimatedHeight) {
        this(initStack(root, estimatedHeight));
    }
    
    protected TreeIterator(ArrayList<N> stack) {
        this.stack = stack;
    }
    
    protected abstract E getElement(N node);
    
    protected N currentNode() {
        return stack.peek();
    }
    
    private static <N extends TreeNode<N>> ArrayList<N> initStack(@CheckForNull N root, int estimatedLength) {
        ArrayList<N> result = new ArrayList<N>(estimatedLength);
        pushLeftSubTrees(root, result);
        return result;
    }
    
    private static <N extends TreeNode<N>> void pushLeftSubTrees(@CheckForNull N root, ArrayList<N> result) {
        N subTree = root;
        while (subTree != null) {
            result.push(subTree);
            subTree = subTree.getLeft();
        }
    }
    
    private void iterate() {
        N subTree = stack.peek();
        if (subTree.getRight() == null) {
            
            do {
                subTree = stack.pop();
            } while (!stack.isEmpty() && subTree == stack.peek().getRight());
            
            return;
        }
        pushLeftSubTrees(subTree.getRight(), stack);
    }
    
    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }
    
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        N current = stack.peek();
        iterate();
        return getElement(current);
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}