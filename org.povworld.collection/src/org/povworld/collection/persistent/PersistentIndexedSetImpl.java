package org.povworld.collection.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;

public class PersistentIndexedSetImpl<E> extends AbstractOrderedCollection<E> implements PersistentIndexedSet<E> {
    
    private final PersistentList<E> list;
    
    private final PersistentSet<E> set;
    
    public static <E> PersistentIndexedSetImpl<E> empty() {
        return new PersistentIndexedSetImpl<E>(PersistentCollections.<E>listOf(), PersistentCollections.<E>setOf());
    }
    
    private PersistentIndexedSetImpl(PersistentList<E> list, PersistentSet<E> set) {
        this.list = list;
        this.set = set;
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public E getFirstOrNull() {
        return list.getFirstOrNull();
    }
    
    @Override
    public E getFirst() throws NoSuchElementException {
        return list.getFirst();
    }
    
    @Override
    public E getLastOrNull() {
        return list.getLastOrNull();
    }
    
    @Override
    public E getLast() throws NoSuchElementException {
        return list.getLast();
    }
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public boolean contains(E element) {
        return set.contains(element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return set.findEqualOrNull(element);
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return list.reverseIterator();
    }
    
    @Override
    public E get(int index) {
        return list.get(index);
    }
    
    @Override
    public PersistentIndexedSet<E> with(E element) {
        PersistentSet<E> newSet = set.with(element);
        if (newSet == set) {
            return this;
        }
        PersistentList<E> newList = list.with(element);
        return new PersistentIndexedSetImpl<E>(newList, newSet);
    }
    
    @Override
    public PersistentIndexedSet<E> withAll(Collection<? extends E> elements) {
        PersistentIndexedSet<E> result = this;
        for (E element: elements) {
            result = result.with(element);
        }
        return result;
    }
    
    @Override
    public PersistentIndexedSet<E> without(E element) {
        // not supported as it does not run in O(log n)
        // TODO can we support this with a PersistentTreeList?
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PersistentIndexedSet<E> withoutAll(Collection<? extends E> elements) {
        // not supported as it does not run in O(m * log n)
        // TODO can we support this with a PersistentTreeList?    
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PersistentIndexedSet<E> cleared() {
        return empty();
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentIndexedSet<E>> {
        
        private PersistentIndexedSet<E> set = empty();
        
        @Override
        protected void _add(E element) {
            set = set.with(element);
        }
        
        @Override
        protected PersistentIndexedSet<E> _createCollection() {
            return set;
        }
        
        @Override
        protected void _reset() {
            set = set.cleared();
        }
        
    }
    
}
