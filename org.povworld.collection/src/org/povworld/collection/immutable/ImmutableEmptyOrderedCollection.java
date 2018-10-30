package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.AbstractOrderedCollection;

/**
 * Optimized implementation of an empty immutable ordered collection.
 * As it contains only a single element anyways it can be {@link List} and {@link Set}
 * at the same time.
 * 
 * <p>Get instances of the class through {@link ImmutableCollections#listOf()}, 
 * {@link ImmutableCollections#setOf()} or {@link ImmutableCollections#indexedSetOf()}.
 * 
 * <p>Due to the immutability, a single instance can be shared for all uses.
 *
 * @param <E> the element type
 */
@Immutable
class ImmutableEmptyOrderedCollection<E> extends AbstractOrderedCollection<E> implements ImmutableList<E>, ImmutableIndexedSet<E> {
    
    private static final ImmutableEmptyOrderedCollection<?> INSTANCE = new ImmutableEmptyOrderedCollection<Object>();
    
    @SuppressWarnings("unchecked")
    static <E> ImmutableEmptyOrderedCollection<E> instance() {
        return (ImmutableEmptyOrderedCollection<E>)INSTANCE;
    }
    
    private ImmutableEmptyOrderedCollection() {}
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public E getFirstOrNull() {
        return null;
    }
    
    @Override
    public E getLastOrNull() {
        return null;
    }
    
    @Override
    public Iterator<E> iterator() {
        return CollectionUtil.emptyIterator();
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return CollectionUtil.emptyIterator();
    }
    
    @Override
    public E get(int index) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
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
