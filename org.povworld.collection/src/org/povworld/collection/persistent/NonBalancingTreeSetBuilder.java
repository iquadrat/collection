package org.povworld.collection.persistent;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.tree.AbstractTreeBuilder;
import org.povworld.collection.tree.ImmutableTreeSetNode;

/**
 * A persistent tree set balancer that does not do any balancing at all. 
 *
 * @param <E> the tree set's element type
 */
class NonBalancingTreeSetBuilder<E> extends AbstractTreeBuilder<NonBalancingTreeSetBuilder.PlainTreeSetNode<E>>
        implements TreeSetBuilder<E, NonBalancingTreeSetBuilder.PlainTreeSetNode<E>> {
    
    private static final int ESTIMATED_PATH_LENGTH = 12;
    
    @Override
    protected int getEstimatedHeight(PlainTreeSetNode<E> node) {
        return ESTIMATED_PATH_LENGTH;
    }
    
    @Override
    public PlainTreeSetNode<E> balance(PlainTreeSetNode<E> left, PlainTreeSetNode<E> top, PlainTreeSetNode<E> right) {
        return createSubTree(left, top, right);
    }
    
    @Override
    public PlainTreeSetNode<E> createSubTree(PlainTreeSetNode<E> left, PlainTreeSetNode<E> top, PlainTreeSetNode<E> right) {
        return new PlainTreeSetNode<E>(left, right, top.getElement());
    }
    
    @Override
    public void checkInvariants(PlainTreeSetNode<E> tree) {
        // There are no balancing invariants.
    }
    
    @Override
    public PlainTreeSetNode<E> createNode(E element) {
        return new PlainTreeSetNode<E>(element);
    }
    
    @Immutable
    static class PlainTreeSetNode<E> implements ImmutableTreeSetNode<E, PlainTreeSetNode<E>> {
        
        @CheckForNull
        private final PlainTreeSetNode<E> left;
        
        @CheckForNull
        private final PlainTreeSetNode<E> right;
        
        private final E element;
        
        public PlainTreeSetNode(E element) {
            this(null, null, element);
        }
        
        public PlainTreeSetNode(PlainTreeSetNode<E> left, PlainTreeSetNode<E> right, E element) {
            this.left = left;
            this.right = right;
            this.element = element;
        }
        
        @Override
        public PlainTreeSetNode<E> getLeft() {
            return left;
        }
        
        @Override
        public PlainTreeSetNode<E> getRight() {
            return right;
        }
        
        @Override
        public E getElement() {
            return element;
        }
        
        @Override
        public String toString() {
            return "[" + left + "," + element + "," + right + "]";
        }
        
    }
}