package org.povworld.collection.persistent;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AbstractAvlTreeBuilder;
import org.povworld.collection.tree.AbstractImmutableAvlTreeNode;
import org.povworld.collection.tree.ImmutableTreeSetNode;

/**
 * Persistent tree set balancer that uses AVL tree nodes.
 * 
 * @param <E> the tree set's element type
 */
class AvlTreeSetBuilder<E> extends AbstractAvlTreeBuilder<AvlTreeSetBuilder.AvlTreeSetNode<E>> implements
        TreeSetBuilder<E, AvlTreeSetBuilder.AvlTreeSetNode<E>> {
    
    @Override
    public AvlTreeSetNode<E> createSubTree(AvlTreeSetNode<E> left, AvlTreeSetNode<E> top, AvlTreeSetNode<E> right) {
        return new AvlTreeSetNode<E>(left, right, top.getElement());
    }
    
    @Override
    public AvlTreeSetNode<E> createNode(E element) {
        return new AvlTreeSetNode<E>(element);
    }
    
    @Immutable
    static class AvlTreeSetNode<E> extends AbstractImmutableAvlTreeNode<AvlTreeSetNode<E>> implements ImmutableTreeSetNode<E, AvlTreeSetNode<E>> {
        
        private final E element;
        
        AvlTreeSetNode(E element) {
            this(null, null, element);
        }
        
        AvlTreeSetNode(AvlTreeSetNode<E> left, AvlTreeSetNode<E> right, E element) {
            super(left, right);
            PreConditions.paramNotNull(element);
            this.element = element;
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