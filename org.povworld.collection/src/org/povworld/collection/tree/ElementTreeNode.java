package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

public class ElementTreeNode<E> extends AbstractAvlTreeNode<ElementTreeNode<E>> {
    
    public static class Iterator<E> extends TreeIterator<E, ElementTreeNode<E>> {
        public Iterator(@CheckForNull ElementTreeNode<E> root) {
            super(root, AvlTreeNode.getHeight(root));
        }
        
        @Override
        protected E getElement(ElementTreeNode<E> node) {
            return node.getElement();
        }
    }
    
    public static class ReverseIterator<E> extends ReverseTreeIterator<E, ElementTreeNode<E>> {
        public ReverseIterator(@CheckForNull ElementTreeNode<E> root) {
            super(root, AvlTreeNode.getHeight(root));
        }

        @Override
        protected E getElement(ElementTreeNode<E> node) {
            return node.getElement();
        }
    }
    
    private static class Transformer<E> implements NodeElementTransformer<E, ElementTreeNode<E>> {

        @Override
        public E getElement(ElementTreeNode<E> node) {
            return node.getElement();
        }

        @Override
        public ElementTreeNode<E> createNode(E element) {
            return new ElementTreeNode<>(element);
        }
        
    }
    
    private static final Transformer<?> TRANSFORMER = new Transformer<Object>();
    
    @SuppressWarnings("unchecked")
    public static <E> Transformer<E> getTransformer() {
        return (Transformer<E>)TRANSFORMER;
    }

    private final E element;
    
    public ElementTreeNode(E element) {
        this.element = element;
    }
    
    public E getElement() {
        return element;
    }
}