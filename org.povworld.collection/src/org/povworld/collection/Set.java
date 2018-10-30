package org.povworld.collection;

/**
 * An unordered collection of elements that does not allow duplicate elements.
 * <p>
 * Two elements are considered duplicates if they are equal according to the set's
 * element identificator (returned by {@link Container#getIdentificator()}).
 *
 * @param <E> the element type
 * @see Sequence
 * @see Bag
 * @see OrderedSet
 */
public interface Set<E> extends UnOrderedCollection<E>, Container<E> {
    
}
