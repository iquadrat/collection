package org.povworld.collection;

/**
 * Unordered collection of elements which explicitly allows duplicates (elements
 * that are equal to each other).
 *
 * @param <E> the element type
 * 
 * @see Sequence
 * @see Set
 * @see OrderedSet
 */
public interface Bag<E> extends UnOrderedCollection<E> {
    
}
