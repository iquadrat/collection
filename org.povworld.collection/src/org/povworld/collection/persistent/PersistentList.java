package org.povworld.collection.persistent;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.immutable.ImmutableList;

/**
 * @param <E> the element type
 */
@Immutable
public interface PersistentList<E> extends ImmutableList<E> {
    
    @CheckReturnValue
    public PersistentList<E> with(E element);
    
    @CheckReturnValue
    public PersistentList<E> with(E element, int index);
    
    @CheckReturnValue
    public PersistentList<E> withAll(Collection<? extends E> elements);
    
    /**
     * Replaces the element at {@code index} to {@code element}.
     * 
     * @return the updated list or the same object if the object at {@code index} 
     *         was the same (using '==') as {@code element}.
     * @throws IndexOutOfBoundsException if {@code index} is too small or too big
     */
    @CheckReturnValue
    public PersistentList<E> withReplacementAt(E element, int index);
    
    /**
     * Creates a new list without the element at {@code index}.
     * 
     * @return the updated list
     * @throws IndexOutOfBoundsException if the index is too small or too big
     */
    @CheckReturnValue
    public PersistentList<E> without(int index);
    
    /**
     * Replaces all occurrences of {@code oldElement} (all elements {@code e}
     * with {@code e.equals(oldElement)}) in the list by {@code newElement}.
     *
     * @return the updated list or the same object if no replacement was done 
     */
    public PersistentList<E> withAllReplaced(E oldElement, E newElement);
    
    /**
     * Returns an empty list of the same type.
     */
    public PersistentList<E> cleared();
}
