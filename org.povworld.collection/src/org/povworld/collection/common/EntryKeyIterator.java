package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.povworld.collection.EntryIterator;

public class EntryKeyIterator<E> implements Iterator<E> {
    
    @CheckForNull
    private EntryIterator<? extends E, ?> entryIterator = null;
    
    public EntryKeyIterator(EntryIterator<? extends E, ?> entryIterator) {
        this.entryIterator = entryIterator;
        if (!this.entryIterator.next()) {
            this.entryIterator = null;
        }
    }
    
    @Override
    public boolean hasNext() {
        return entryIterator != null;
    }
    
    @Override
    public E next() {
        if (entryIterator == null) {
            throw new NoSuchElementException();
        }
        E result = entryIterator.getCurrentKey();
        if (!entryIterator.next()) {
            entryIterator = null;
        }
        return result;
    }
    
}