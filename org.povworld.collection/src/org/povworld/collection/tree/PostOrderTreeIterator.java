package org.povworld.collection.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.mutable.ArrayList;

/**
 * Post-order tree iterator. 
 *
 * @param <E> the element type
 * @param <N> the tree node type
 * 
 * @see TreeIterator
 */
@NotThreadSafe
public abstract class PostOrderTreeIterator<E, N extends TreeNode<N>> implements Iterator<E> {
    
    private final ArrayList<N> stack;
    
    public PostOrderTreeIterator(N root) {
        stack = new ArrayList<>();
        pushSubTrees(root);
    }
    
    protected abstract E getElement(N tree);
    
    private void pushSubTrees(N root) {
        N subTree = root;
        while (subTree != null) {
            stack.push(subTree);
            subTree = subTree.getLeft();
        }
        N last = stack.peek();
        if (last.getRight() != null) {
            pushSubTrees(last.getRight());
        }
    }
    
    private void iterate() {
        N subTree = stack.pop();
        if (stack.isEmpty()) {
            // The root as last node has been visited, we are done.
            return;
        }
        N next = stack.peek();
        if ((next.getRight() != null) && (next.getLeft() == subTree)) {
            // Right subtree has not yet been visited.
            pushSubTrees(next.getRight());
        }
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