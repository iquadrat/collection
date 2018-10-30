package org.povworld.collection.immutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.MathUtil;
import org.povworld.collection.mutable.HashSet;

/**
 * Tries to create a perfect hash set with a fast collision free hash-function.
 * 
 * <p>The hash function will be of type {@code (hashvalue + (hashvalue >> C)) % size} where size
 * is the number of hash buckets. It is at least the next power of two of the element count
 * and at most 16 times that value.
 */
@Immutable
public class PerfectHashSet<E> extends AbstractUnOrderedCollection<E> implements ImmutableSet<E> {
    
    public static class NoPerfectHashSetFoundException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        public NoPerfectHashSetFoundException() {
            super("No perfect hash function found!");
        }
        
    }
    
    /**
     * Tries to create a perfect hash set using the hash values of the given objects. 
     * Duplicate elements in the input are ignored.
     * 
     * @throws NoPerfectHashSetFoundException if there is no success, i.e. no perfect hash function could be found.
     */
    @SafeVarargs
    public static <E> PerfectHashSet<E> of(E... elements) {
        return of(ImmutableCollections.setOf(elements));
    }
    
    /**
     * Tries to create a perfect hash set using the hash values of the given contained in the given set.
     * 
     * @throws NoPerfectHashSetFoundException if there is no success, i.e. no perfect hash function could be found.
     */
    public static <E> PerfectHashSet<E> of(Set<E> elements) {
        return create(elements, elements.size());
    }
    
    private static <E> PerfectHashSet<E> create(Iterable<E> elements, int elementCount) throws NoPerfectHashSetFoundException {
        int size = Math.max(2, MathUtil.nextPowerOfTwo(elementCount));
        for (int i = 0; i < 5; ++i) {
            E[] buckets = ArrayUtil.unsafeCastedNewArray(size);
            try {
                return tryCreate(elements, elementCount, buckets);
            } catch (NoPerfectHashSetFoundException e) {
                size *= 2;
            }
        }
        throw new NoPerfectHashSetFoundException();
    }
    
    private static <E> PerfectHashSet<E> tryCreate(Iterable<E> elements, int elementCount, E[] buckets) {
        for (int shift = 1; shift < 32; ++shift) {
            clear(buckets);
            if (tryFillBucketsShift(buckets, elements, shift)) {
                return new PerfectHashSet<E>(buckets, shift, elementCount);
            }
        }
        throw new NoPerfectHashSetFoundException();
    }
    
    private static <E> boolean tryFillBucketsShift(E[] buckets, Iterable<E> elements, int shift) {
        int mask = buckets.length - 1;
        for (E element: elements) {
            int hashcode = element.hashCode();
            int hashvalue = hash(shift, mask, hashcode);
            if (buckets[hashvalue] != null) {
                return false;
            }
            buckets[hashvalue] = element;
        }
        return true;
    }
    
    private static int hash(int shift, int mask, int hashcode) {
        return (hashcode + (hashcode >> shift)) & mask;
    }
    
    private static <E> void clear(E[] buckets) {
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = null;
        }
    }
    
    private final E[] buckets;
    
    private final int[] hashCodes;
    
    private final int shift;
    
    private final int mask;
    
    private final int elementCount;
    
    private PerfectHashSet(E[] buckets, int shift, int elementCount) {
        this.buckets = buckets;
        this.hashCodes = new int[buckets.length];
        this.shift = shift;
        this.mask = buckets.length - 1;
        this.elementCount = elementCount;
        for (int i = 0; i < buckets.length; ++i) {
            if (buckets[i] == null) continue;
            hashCodes[i] = buckets[i].hashCode();
        }
    }
    
    @Override
    public boolean contains(E element) {
        int hashcode = element.hashCode();
        int bucket = hash(shift, mask, hashcode);
        return (hashCodes[bucket] == hashcode) && element.equals(buckets[bucket]);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        int hashcode = element.hashCode();
        int bucket = hash(shift, mask, hashcode);
        if (!(hashCodes[bucket] == hashcode) && element.equals(buckets[bucket])) {
            return null;
        }
        return buckets[bucket];
    }
    
    @Override
    public int size() {
        return elementCount;
    }
    
    @Override
    public E getFirstOrNull() {
        for (E object: buckets) {
            if (object != null) {
                return object;
            }
        }
        return null;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new PerfectHashSetIterator();
    }
    
    private class PerfectHashSetIterator implements Iterator<E> {
        
        private int nextIndex = -1;
        
        PerfectHashSetIterator() {
            findNext();
        }
        
        @Override
        public boolean hasNext() {
            return nextIndex < buckets.length;
        }
        
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E next = buckets[nextIndex];
            findNext();
            return next;
        }
        
        private void findNext() {
            nextIndex++;
            while (nextIndex < buckets.length && buckets[nextIndex] == null) {
                nextIndex++;
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public static <E> CollectionBuilder<E, ImmutableSet<E>> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, ImmutableSet<E>> {
        
        @Nullable
        private HashSet<E> set = new HashSet<>();
        
        @Override
        protected void _add(E element) {
            set.add(element);
        }
        
        @Override
        protected ImmutableSet<E> _createCollection() {
            PerfectHashSet<E> result = of(set);
            set = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            set = new HashSet<>();
        }
        
    }
    
}
