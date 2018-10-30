package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.povworld.collection.EntryIterator;

public class EntryValueIterator<V> implements Iterator<V> {
    
    @CheckForNull
    private EntryIterator<?, ? extends V> entryIterator = null;
    
    public EntryValueIterator(EntryIterator<?, ? extends V> entryIterator) {
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
    public V next() {
        if (entryIterator == null) {
            throw new NoSuchElementException();
        }
        V result = entryIterator.getCurrentValue();
        if (!entryIterator.next()) {
            entryIterator = null;
        }
        return result;
    }
    
}
