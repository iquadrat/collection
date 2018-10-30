package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.SingletonIterator;

/**
 * Implementation of an immutable ordered collection that contains exactly one element.
 * As it contains only a single element anyways it can be {@link List} and {@link Set}
 * at the same time.
 * 
 * <p>Get instances of the class through {@link ImmutableCollections#listOf(Object)},
 * {@link ImmutableCollections#indexedSetOf(Object)} or 
 * {@link ImmutableCollections#orderedSetOf(Object)}.
 *
 * @param <E> the element type
 */
@Immutable
class ImmutableSingletonOrderedCollection<E> extends AbstractOrderedCollection<E> implements ImmutableList<E>, ImmutableIndexedSet<E> {
    
    private final E theElement;
    
    ImmutableSingletonOrderedCollection(E theElement) {
        PreConditions.paramNotNull(theElement);
        this.theElement = theElement;
    }
    
    @Override
    public int size() {
        return 1;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        return theElement;
    }
    
    @Override
    public E getFirst() {
        return theElement;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new SingletonIterator<>(theElement);
    }
    
    @Override
    public E get(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return theElement;
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return iterator();
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        return theElement;
    }
    
    @Override
    public E getLast() {
        return theElement;
    }
    
    @Override
    public boolean contains(E element) {
        return getIdentificator().equals(theElement, element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        if (contains(element)) {
            return element;
        }
        return null;
    }
    
}
