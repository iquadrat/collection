package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIterator<E> implements Iterator<E> {
    
    @SuppressWarnings("unchecked")
    public static <E> EmptyIterator<E> getInstance() {
        return (EmptyIterator<E>)INSTANCE;
    }
    
    private static final EmptyIterator<?> INSTANCE = new EmptyIterator<>();
    
    private EmptyIterator() {}
    
    @Override
    public boolean hasNext() {
        return false;
    }
    
    @Override
    public E next() {
        throw new NoSuchElementException();
    }
    
}
