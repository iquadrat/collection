package org.povworld.collection.adapt;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.ReadOnlyIterator;

/**
 * Adapts from {@link java.util.Set} to {@link Set}.
 */
public class SetAdapter<E> extends AbstractUnOrderedCollection<E> implements Set<E> {
    
    private final java.util.Set<E> set;
    
    public SetAdapter(java.util.Set<E> set) {
        this.set = set;
    }
    
    @Override
    public boolean contains(E element) {
        return set.contains(element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size() {
        return set.size();
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        return CollectionUtil.firstElement(set);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIterator<>(set.iterator());
    }
    
    public Iterator<E> modifyingIterator() {
        return set.iterator();
    }
    
    public void add(E element) {
        PreConditions.paramNotNull(element);
        set.add(element);
    }
    
    public void remove(E element) {
        PreConditions.paramNotNull(element);
        set.remove(element);
    }
    
}
