package org.povworld.collection;

/**
 * A collection allowing to access the elements by index. If the collection
 * has size n, then its elements can be accessed by indices 0..n-1.
 * 
 * @param <E> the element type
 */
// TODO add methods for slicing
public interface IndexedCollection<E> extends OrderedCollection<E> {
    
    /**
     * Retrieves the element at given {@code index}.
     * 
     * @param index the index of the requested element
     * @return The element at the given index.
     * @throws IndexOutOfBoundsException if the index is out of the range
     *         ({@code (index < 0) || (index >= size()})
     */
    public E get(int index);
    
}
