package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<E> implements Iterator<E> {
    
    private final E[] elements;
    
    private int next = 0;
    
    /**
     * Creates a new iterator for the entries in the given {@code elements} array.
     * <p>
     * The array must not contain any entries that are null, otherwise a
     * {@code NullPointerException} is thrown during iteration.
     * 
     * @param elements array of elements to iterate 
     */
    public ArrayIterator(E[] elements) {
        this.elements = elements;
    }
    
    @Override
    public boolean hasNext() {
        return next < elements.length;
    }
    
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return ObjectUtil.checkNotNull(elements[next++]);
    }
    
}