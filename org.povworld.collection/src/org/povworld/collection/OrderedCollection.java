package org.povworld.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

/**
 * An ordered collection of elements.
 *
 * @param <E> the element type
 */
public interface OrderedCollection<E> extends Collection<E> {
    
    /**
     * @return iterator that iterates the elements in the list in reverse order
     */
    public Iterator<E> reverseIterator();
    
    /**
     * Gets the last element of the collection if it is not empty.
     * The last element is the element that comes first when iterated
     * using an Iterator given by {@link #reverseIterator()}.
     * 
     * @return the last element or {@code null} if the collection is empty
     */
    @CheckForNull
    public E getLastOrNull();
    
    /**
     * @see #getLastOrNull()
     * @throws NoSuchElementException if the list is empty
     */
    public default E getLast() throws NoSuchElementException {
        E last = getLastOrNull();
        if (last == null) {
            throw new NoSuchElementException();
        }
        return last;
    }
    
    /**
     * An instance of {@link OrderedCollection} is equal to some other object if the object is an
     * instance of {@link OrderedCollection} as well, both collections have the same identificator and 
     * both collections iterate over equal elements (where equality is defined using the collection's
     * identificator).
     */
    @Override
    public boolean equals(Object obj);
    
    /**
     * The hash code must be calculated as
     * <pre>
        final Identificator<? super E> identificator = getIdentificator();
        int hashCode = 1;
        for (E element: this) {
            hashCode = 31 * hashCode + identificator.hashCode(element);
        }
        return hashCode;
     * </pre>
     */
    @Override
    public int hashCode();
    
}
