package org.povworld.collection;

import javax.annotation.CheckForNull;

/**
 * A collection of elements with no defined order. This means the order of elements
 * can change arbitrarily between subsequent iterations.
 *
 * @param <E> the element type
 */
public interface UnOrderedCollection<E> extends Container<E> {
    
    /**
     * Tests if an element belongs to the collection, i.e., if there is an object 
     * in the collection which is equal to the given element according to the
     * collection's identificator.
     * <p>
     * NOTE: Runs in O(log n) where n is the collection's size.
     * 
     * @return true if the collection contains the given element
     * @see #getIdentificator()
     * @see Identificator#equals(Object, Object)
     */
    @Override
    public boolean contains(E element);
    
    /**
     * @return occurrence count of the given element 
     */
    public int getCount(E element);
    
    /**
     * An instance of {@link UnOrderedCollection} is equal to some object if the object is an instance of
     * {@link UnOrderedCollection} as well, both collections have the same {@link Identificator},
     * and both collections contain the same elements.
     * 
     * @param obj the object to compare to
     * @return true if the collections are equal
     * @see #getIdentificator()
     * @see #contains(Object)
     */
    @Override
    public boolean equals(@CheckForNull Object obj);
    
    /**
     * The hashcode is defined as sum of all the hash codes of the elements in the set:
     * <pre>
        final Identificator<? super E> identificator = getIdentificator();
        int result = 0;
        for (E element: this) {
            result += identificator.hashCode(element);
        }
        return result;
     * </pre>
     */
    @Override
    public int hashCode();
    
}
