package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

public class SingletonIterator<E> implements Iterator<E> {
    
    @CheckForNull
    private E element;
    
    public SingletonIterator(E element) {
        this.element = ObjectUtil.checkNotNull(element);
    }
    
    @Override
    public boolean hasNext() {
        return element != null;
    }
    
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        E result = element;
        element = null;
        return result;
    }
    
}
