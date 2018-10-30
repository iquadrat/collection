package org.povworld.collection.persistent;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.immutable.ImmutableOrderedSet;

// TODO push some methods up to a new PersistentCollection interface?
@Immutable
public interface PersistentOrderedSet<E> extends ImmutableOrderedSet<E> {
    
    /**
     * Creates a new set that has the given {@code element} added.
     * @return the new set or the same object if the element is already contained
     */
    public PersistentOrderedSet<E> with(E element);
    
    /**
     * Creates a new set that has all the given {@code elements} added. 
     * @return the new set or the same object if all the elements are already contained
     */
    public PersistentOrderedSet<E> withAll(Collection<? extends E> elements);
    
    /**
     * Creates a new set that has the given {@code element} removed.
     * @return the new set or the same object if the element is not contained
     */
    public PersistentOrderedSet<E> without(E element);
    
    /**
     * Creates a new set that has all the given {@code elements} removed.
     * @return the new set or the same object if none of the elements is contained
     */
    public PersistentOrderedSet<E> withoutAll(Collection<? extends E> elements);
    
    /**
     * @return an empty persistent set of the same type
     */
    public PersistentOrderedSet<E> cleared();
    
}
