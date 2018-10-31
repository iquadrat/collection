package org.povworld.collection.mutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Bag;
import org.povworld.collection.Collection;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractUnOrderedCollection;

/**
 * Bag implementation that uses a hash map where the elements are the key and the number 
 * of occurrence of the element is the value.
 *
 * @param <E> the element type
 */
@NotThreadSafe
public class HashBag<E> extends AbstractUnOrderedCollection<E> implements Bag<E> {
    
    private static class Count {
        
        private int value;
        
        public Count(int initialValue) {
            value = initialValue;
        }
        
        /**
         * @return current value
         */
        public int getValue() {
            return value;
        }
        
        /**
         * @return new value
         */
        public int increment() {
            return ++value;
        }
        
        /**
         * @return new value
         */
        public int decrement() {
            return --value;
        }
        
        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }
    
    private final HashMap<E, Count> countMap;
    
    private int size;
    
    public HashBag() {
        countMap = new HashMap<>();
        size = 0;
    }
    
    public HashBag(int initialCapacity) {
        countMap = new HashMap<>(initialCapacity);
    }
    
    public HashBag(Collection<? extends E> collection) {
        this(collection, collection.size());
    }
    
    @SafeVarargs
    public HashBag(E... elements) {
        this(Arrays.asList(elements), elements.length);
    }
    
    public HashBag(Iterable<? extends E> initialElements, int initialCapacity) {
        this(new HashMap<E, Count>(/*TODO initialCapacity*/), initialElements);
    }
    
    private HashBag(HashMap<E, Count> map, Iterable<? extends E> initialElements) {
        countMap = map;
        addAll(initialElements);
    }
    
    /**
     * Adds the given {@code element} to the bag.
     * 
     * @return number of occurrences after adding
     */
    public int add(E element) {
        size++;
        Count count = countMap.get(element);
        if (count != null) {
            return count.increment();
        }
        countMap.put(element, new Count(1));
        return 1;
    }
    
    /**
     * Removes the given {@code element} to the bag.
     *
     * @return number of occurrences of the element left after removing one or -1 if 
     *         {@code value} was not contained in the bag 
     */
    public int remove(E element) {
        Count count = countMap.get(element);
        if (count == null) return -1;
        int valuesLeft = count.decrement();
        if (valuesLeft == 0) countMap.remove(element);
        size--;
        return valuesLeft;
    }
    
    /**
     * Adds all the elements in the {@code iterable} to the bag.
     */
    public void addAll(Iterable<? extends E> iterable) {
        for (E value: iterable) {
            add(value);
        }
    }
    
    /**
     * Removes all the elements in the {@code iterable} from the bag.
     * @return the number of elements removed
     */
    public int removeAll(Iterable<? extends E> values) {
        int removed = 0;
        for (E value: values) {
            if (remove(value) != -1) {
                removed++;
            }
        }
        return removed;
    }
    
    /**
     * Removes all occurrences of the given {@code element} from the bag.
     * @return the number of elements removed.
     */
    public int removeAllOccurrences(E element) {
        Count count = countMap.remove(element);
        if (count == null) {
            return 0;
        }
        size -= count.getValue();
        return count.getValue();
    }
    
    /**
     * Removes all elements from the bag.
     */
    public void clear() {
        countMap.clear();
        size = 0;
    }
    
    @Override
    public int getCount(E element) {
        Count count = countMap.get(element);
        if (count == null) return 0;
        return count.getValue();
    }
    
    @Override
    public boolean contains(E element) {
        return countMap.containsKey(element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return countMap.keys().findEqualOrNull(element);
    }
    
    public int getNumberOfDifferentElements() {
        return countMap.keyCount();
    }
    
    @Override
    public boolean isEmpty() {
        return countMap.isEmpty();
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public E getFirstOrNull() {
        return countMap.keys().getFirstOrNull();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new HashBagIterator();
    }
    
    // TODO add modifyingIterator()
    
    private class HashBagIterator implements Iterator<E> {
        
        private final EntryIterator<E, Count> hashSetIterator = countMap.entryIterator();
        
        private int elementsLeftInCurrent = 0;
        
        public HashBagIterator() {
            findNext();
        }
        
        @Override
        public boolean hasNext() {
            return elementsLeftInCurrent > 0;
        }
        
        @Override
        public E next() {
            if (elementsLeftInCurrent <= 0) {
                throw new NoSuchElementException();
            }
            E current = hashSetIterator.getCurrentKey();
            elementsLeftInCurrent--;
            if (elementsLeftInCurrent == 0) {
                findNext();
            }
            return current;
        }
        
        private void findNext() {
            if (hashSetIterator.next()) {
                elementsLeftInCurrent = hashSetIterator.getCurrentValue().getValue();
            }
        }
        
    }
    
    public static <E> HashBag.Builder<E> newBuilder() {
        return new Builder<E>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, HashBag<E>> {
        @Nullable
        private HashBag<E> hashBag = new HashBag<>();
        
        @Override
        protected void _add(E element) {
            hashBag.add(element);
        }
        
        @Override
        protected HashBag<E> _createCollection() {
            HashBag<E> result = hashBag;
            hashBag = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            hashBag = new HashBag<>();
        }
    }
    
    @Override
    public int hashCode() {
        // Optimized implementation: Multiply element's hash code by it's occurrence count
        // instead of adding the same value multiple times.
        final Identificator<? super E> identificator = getIdentificator();
        int result = 0;
        EntryIterator<E, Count> iterator = countMap.entryIterator();
        while (iterator.next()) {
            result += iterator.getCurrentValue().value * identificator.hashCode(iterator.getCurrentKey());
        }
        return result;
    }
    
}
