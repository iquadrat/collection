package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.AbstractUnOrderedCollection;

/**
 * Optimized implementation of an empty immutable unordered collection.
 * 
 * <p>Get instances of the class through {@link ImmutableCollections#setOf()}. 
 * 
 * <p>Due to the immutability, a single instance can be shared for all uses.
 *
 * @param <E> the element type
 */
@Immutable
class ImmutableEmptyUnOrderedCollection<E> extends AbstractUnOrderedCollection<E> implements ImmutableSet<E> {
    
    private static final ImmutableEmptyUnOrderedCollection<?> INSTANCE = new ImmutableEmptyUnOrderedCollection<Object>();
    
    @SuppressWarnings("unchecked")
    static <E> ImmutableEmptyUnOrderedCollection<E> instance() {
        return (ImmutableEmptyUnOrderedCollection<E>)INSTANCE;
    }
    
    private ImmutableEmptyUnOrderedCollection() {}
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public E getFirstOrNull() {
        return null;
    }
    
    @Override
    public Iterator<E> iterator() {
        return CollectionUtil.emptyIterator();
    }
    
    @Override
    public boolean contains(E element) {
        return false;
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return null;
    }
}
