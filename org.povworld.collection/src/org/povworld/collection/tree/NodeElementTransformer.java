package org.povworld.collection.tree;

public interface NodeElementTransformer<E, N> {
    
    public E getElement(N node);
    
    public N createNode(E element);
    
}
