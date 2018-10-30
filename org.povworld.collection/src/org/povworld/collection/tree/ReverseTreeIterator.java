package org.povworld.collection.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.mutable.ArrayList;

/**
 * Traverses tree in reverse order. This is exactly the reverse order of {@link TreeIterator}. 
 *
 * @param <E> the element type
 * @param <N> the tree node type
 */
@NotThreadSafe
public abstract class ReverseTreeIterator<E, N extends TreeNode<N>> implements Iterator<E> {
    
    // Invariant: The right subtrees of the stack top has already been visited
    private final ArrayList<N> stack;
    
    protected ReverseTreeIterator(N root, int estimatedHeight) {
        stack = initStack(root, estimatedHeight);
    }
    
    protected abstract E getElement(N tree);
    
    private static <T extends TreeNode<T>> ArrayList<T> initStack(T root, int estimatedHeight) {
        ArrayList<T> result = new ArrayList<T>(estimatedHeight);
        pushRightSubTrees(root, result);
        return result;
    }
    
    private static <T extends TreeNode<T>> void pushRightSubTrees(T root, ArrayList<T> result) {
        T subTree = root;
        while (subTree != null) {
            result.push(subTree);
            subTree = subTree.getRight();
        }
    }
    
    private void iterate() {
        N subTree = stack.peek();
        if (subTree.getLeft() == null) {
            
            do {
                subTree = stack.pop();
            } while (!stack.isEmpty() && subTree == stack.peek().getLeft());
            
            return;
        }
        pushRightSubTrees(subTree.getLeft(), stack);
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
    
}