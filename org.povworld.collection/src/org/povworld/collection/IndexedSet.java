package org.povworld.collection;

/**
 * An indexed set is an ordered set that allows to efficiently
 * address the elements by the index in the order. 
 * 
 * @param <E> the element type
 */
public interface IndexedSet<E> extends OrderedSet<E>, IndexedCollection<E> {
    
}
