package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.SingletonIterator;

/**
 * Implementation of an immutable unordered collection that contains exactly one element.
 * 
 * <p>Get instances of the class through {@link ImmutableCollections#setOf(Object)}.
 *
 * @param <E> the element type
 */
@Immutable
class ImmutableSingletonUnOrderedCollection<E> extends AbstractUnOrderedCollection<E> implements ImmutableSet<E> {
    
    private final E theElement;
    
    ImmutableSingletonUnOrderedCollection(E theElement) {
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
    public boolean contains(E element) {
        return getIdentificator().equals(theElement, element);
    }
    
    @Override
    @CheckForNull
    public E findEqualOrNull(E element) {
        if (contains(element)) {
            return element;
        }
        return null;
    }
}
