package org.povworld.collection.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.povworld.collection.IndexedCollection;

public class ShuffledIterable<E> implements Iterable<E> {
    
    private final IndexedCollection<E> collection;
    
    private final int[] permutation;
    
    /**
     * Creates a new Iterable that iterates the given collection in shuffled orders.
     * Multiple iterations will always be in the same order, i.e., the shuffling does
     * not change between iterations. The given collection must not change in size
     * during the life time of the {@code ShuffledIterable}. Otherwise, {@link IndexOutOfBoundsException}
     * can occur if the passed-in collection gets smaller and not all elements will be
     * iterated when the collection gets larger. The creation of iterators is thread-safe but
     * the returned {@code Iterator} is not.
     *  
     * @param collection the collection to shuffle
     * @param random the random generator to use
     */
    public ShuffledIterable(IndexedCollection<E> collection, Random random) {
        this.collection = collection;
        this.permutation = MathUtil.randomPermutation(collection.size(), random);
    }
    
    private class ShuffledIterator implements Iterator<E> {
        
        private int current = 0;
        
        @Override
        public boolean hasNext() {
            return current < permutation.length;
        }
        
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int index = permutation[current];
            current++;
            return collection.get(index);
        }
        
        @Override
        public void remove() {
            // Removing the current element from the underlying collection would
            // change the indexes and therefore mess up the permutation.
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ShuffledIterator();
    }
    
}
