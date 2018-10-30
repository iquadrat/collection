package org.povworld.collection;

import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

/**
 * {@code Collection}s are containers for elements. Each collection is either empty or contains
 * some elements and allows to iterate all contained elements. Some collections have a notion of ordering, 
 * others do not (see {@link OrderedCollection} and {@link UnOrderedCollection}), some collections 
 * allow duplicates, others not (see {@link Sequence}, @link Bag}, {@link Set}, {@link OrderedSet).
 * 
 * <p>Generally, {@code Collection}s provide only operations that run reasonably fast (i.e., 
 * O(log size) per element in the operation). The helper class {@link CollectionUtil}
 * provides some static methods that contain more operations with potentially slower run time.
 * 
 * <p>The equality of two {@code Collection}s (as defined by {@link Object#equals(Object)}) is implementation 
 * dependent. Normally, collections should either implement {@link OrderedCollection} or {@link UnOrderedCollection}
 * which define the semantic for equality.
 * 
 * @param <E> the type of the contained elements
 * 
 * @see Sequence
 * @see Bag
 * @see Set
 * @see OrderedSet
 */
public interface Collection<E> extends Iterable<E> {
    
    /**
     * @return number of elements in this collection
     */
    public int size();
    
    /**
     * @return true if the collection contains no elements
     */
    public default boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Gets the first element of the collection if it is not empty.
     * The first element is the element that comes first when iterated
     * using an Iterator given by {@link #iterator()}.
     * 
     * @return the first element or {@code null} if the collection is empty
     */
    @CheckForNull
    public E getFirstOrNull();
    
    /**
     * @see #getFirstOrNull()
     * @throws NoSuchElementException if the collection is empty
     */
    public default E getFirst() throws NoSuchElementException {
        E first = getFirstOrNull();
        if (first == null) {
            throw new NoSuchElementException();
        }
        return first;
    }
    
    /**
     * @return the identificator used to decide whether two objects in the collection are equal or not
     * @see #equals(Object)
     */
    public default Identificator<? super E> getIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
}
