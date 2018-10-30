package org.povworld.collection;

import javax.annotation.CheckForNull;

/**
 * This interface is implemented by collections which efficiently support checking
 * for element containment.
 * 
 * @param <E> the element type
 */
public interface Container<E> extends Collection<E> {
    
    /**
     * Test if an element belongs to the collection.
     * <p>
     * NOTE: Runs in O(log n) where n is the collection's size.
     * 
     * @return true if the collection contains the given element
     */
    public boolean contains(E element);
    
    public default boolean containsAll(Iterable<? extends E> elements) {
        for (E value: elements) {
            if (!contains(value)) return false;
        }
        return true;
    }
    
    /**
     * Finds an element that is equal to the given element (as determined by the colleciton's 
     * {@link Identificator} or returns null if there is no such element.
     * 
     * @see #getIdentificator()
     */
    @CheckForNull
    public E findEqualOrNull(E element);
    
}
