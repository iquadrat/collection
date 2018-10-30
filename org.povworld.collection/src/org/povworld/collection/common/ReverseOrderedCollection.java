package org.povworld.collection.common;

import java.util.Iterator;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.OrderedCollection;

/**
 * Implementation of {@link OrderedCollection} that is based delegated to an existing
 * collection but reverses the order of the elements in the delegate.
 * <p>
 * Changes of the delegate are reflected in the reverse collection.
 * 
 * @param <E> element type
 */
@NotThreadSafe
public class ReverseOrderedCollection<E> extends AbstractOrderedCollection<E> implements OrderedCollection<E> {
    
    protected final OrderedCollection<E> collection;
    
    public ReverseOrderedCollection(OrderedCollection<E> collection) {
        this.collection = collection;
    }
    
    @Override
    public Iterator<E> iterator() {
        return collection.reverseIterator();
    }
    
    @Override
    public int size() {
        return collection.size();
    }
    
    @Override
    public E getFirstOrNull() {
        return collection.getLastOrNull();
    }
    
    @Override
    public E getLastOrNull() {
        return collection.getFirstOrNull();
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        // double reverse gives original iteration order
        return collection.iterator();
    }
}
