package org.povworld.collection.persistent;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.immutable.ImmutableIndexedSet;

@Immutable
public interface PersistentIndexedSet<E> extends PersistentOrderedSet<E>, ImmutableIndexedSet<E> {
    
    /**
     * Creates a new {@code PersistentIndexedSet} with the given {@code element} added at the last position.
     * <p>
     * Returns the same object if the element is already contained.
     */
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> with(E element);
    
    /**
     * Creates a new {@code PersistentIndexedSet} with the given {@code element} added at {@code index}.
     * <p>
     * Returns the same object if the element is already contained.
     */
    @CheckReturnValue
    public PersistentIndexedSet<E> with(E element, int index);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> withAll(Collection<? extends E> elements);
    
    /**
     * Creates a new {@code PersistentIndexedSet} with the element at the given {@code index} removed.
     * 
     * @throws IndexOutOfBoundsException if the index is negative or too large
     */
    @CheckReturnValue
    public PersistentIndexedSet<E> withoutElementAt(int index);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> without(E element);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> withoutAll(Collection<? extends E> elements);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> cleared();
    
}
