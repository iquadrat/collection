package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.povworld.collection.List;

public class ReverseListIterator<E> implements Iterator<E> {
    
    private final List<E> list;
    
    private int next;
    
    public ReverseListIterator(List<E> list) {
        this.list = list;
        this.next = list.size();
    }
    
    @Override
    public boolean hasNext() {
        return next > 0;
    }
    
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        --next;
        return list.get(next);
    }
    
}