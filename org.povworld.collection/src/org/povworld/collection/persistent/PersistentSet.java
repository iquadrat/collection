package org.povworld.collection.persistent;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.immutable.ImmutableSet;

/**
 * A persistent set of elements.
 */
@Immutable
public interface PersistentSet<E> extends ImmutableSet<E> {
    
    /**
     * Creates a new set that has the given {@code element} added.
     * @return the new set or the same object if the element is already contained
     */
    public PersistentSet<E> with(E element);
    
    /**
     * Creates a new set that has all the given {@code elements} added. 
     * @return the new set or the same object if all the elements are already contained
     */
    public PersistentSet<E> withAll(Iterable<? extends E> elements);
    
    /**
     * Creates a new set that has the given {@code element} removed.
     * @return the new set or the same object if the element is not contained
     */
    public PersistentSet<E> without(E element);
    
    /**
     * Creates a new set that has all the given {@code elements} removed.
     * @return the new set or the same object if none of the elements is contained
     */
    public PersistentSet<E> withoutAll(Iterable<? extends E> elements);
    
    /**
     * @return an empty persistent set of the same type
     */
    public PersistentSet<E> cleared();
    
}
