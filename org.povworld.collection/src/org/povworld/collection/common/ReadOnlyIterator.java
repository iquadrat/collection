package org.povworld.collection.common;

import java.util.Iterator;

public class ReadOnlyIterator<E> implements Iterator<E> {
    
    private final Iterator<E> delegate;
    
    public ReadOnlyIterator(Iterator<E> iterator) {
        this.delegate = iterator;
    }
    
    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }
    
    @Override
    public E next() {
        return delegate.next();
    }
    
    
}