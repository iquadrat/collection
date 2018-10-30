package org.povworld.collection;

import java.util.NoSuchElementException;

public interface EntryIterator<K, V> {
    /**
     * Tries to iterate one element.
     * 
     * @return true if there was a next element, false if the end has been reached
     */
    public boolean next();
    
    public K getCurrentKey() throws NoSuchElementException;
    
    public V getCurrentValue() throws NoSuchElementException;
}
