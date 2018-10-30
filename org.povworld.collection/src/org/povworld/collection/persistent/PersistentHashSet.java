package org.povworld.collection.persistent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.EmptyIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;

/**
 * Persistent set implementation that uses the element's hash code to locate it in a hierarchical hash table.
 * 
 * <p>The hash table is split up into {@link HashBucket}s which have an upper size limit. The hash buckets use the 
 * lowest bits of the hash. The elements in the bucket are stored in a compact way: The array does not contain any
 * empty entries, a bitmap is used to signal which table entries are used. If the bucket size grows above the limit,
 * it is split into two buckets based on the next bit of the hash and an {@link InnerBucket} is created with two entries.
 * The inner bucket uses the same compact storage for its sub-buckets. If one of its sub-bucket is full further bits
 * of the hash will be used to split it again. If all the bits corresponding to one inner bucket are used, an 
 * additional indirection is created. Like this a tree structure is created.
 * 
 * <p>If too many entries with full 32-bit hash collision are present they will be put into a separate 
 * {@link CollisionBucket} which contains an array of colliding elements for each hash value. 
 */
public class PersistentHashSet<E> extends AbstractUnOrderedCollection<E> implements PersistentSet<E> {
    
    protected static final int HASH_BITS = 6;
    
    private static final int HASH_TABLE_SIZE = 1 << HASH_BITS;
    
    private static final int HASH_MASK = HASH_TABLE_SIZE - 1;
    
    private static final int MAX_HEIGHT = ((32 + HASH_BITS - 1) / HASH_BITS) + 2;
    
    private static final Bucket<?> EMPTY_LEAF = new EmptyBucket<Object>();
    
    private static final PersistentSet<?> EMPTY_SET = empty(CollectionUtil.getObjectIdentificator());
    
    //private static final int LEAF_BUCKET_SPLIT_SIZE = 32;
    
    private static int sBucketSplitSize;
    private static int sBucketMergeSize;
    static {
        setHashBucketSplitSize(40);
    }
    
    public static void setHashBucketSplitSize(int size) {
        sBucketSplitSize = size;
        sBucketMergeSize = 4 * size / 5;
    }
    
    @SuppressWarnings("unchecked")
    static <E> Bucket<E> emptyLeaf() {
        return (Bucket<E>)EMPTY_LEAF;
    }
    
    private interface BucketIterable<E> {
        boolean findNext(EntryIterator<E> iterator);
    }
    
    interface Bucket<E> extends BucketIterable<E> {
        Bucket<E> add(Identificator<? super E> identificator, E element, int hashValue, int rshift);
        
        Bucket<E> remove(Identificator<? super E> identificator, E element, int hashValue, int rshift);
        
        E getFirst();
        
        @CheckForNull
        E findEqualOrNull(Identificator<? super E> identificator, E element, int hashValue, int rshift);
        
        boolean isFullLeaf();
        
        int leafSize();
    }
    
    private static int rawIndex(int hashvalue) {
        return hashvalue & HASH_MASK;
    }
    
    private static int index(long occupied, int rawIndex) {
        long bit = 1L << rawIndex;
        return Long.bitCount(occupied & (bit - 1));
    }
    
    private static boolean isOccupied(long occupied, long rawIndex) {
        return ((occupied >>> rawIndex) & 1) != 0;
    }
    
    static class EmptyBucket<E> implements Bucket<E> {
        
        @Override
        public boolean findNext(EntryIterator<E> iterator) {
            return false;
        }
        
        @Override
        public Bucket<E> add(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            return new HashBucket<E>(ArrayUtil.arrayOf(element), new int[] {hashValue}, 1L << rawIndex(hashValue));
        }
        
        @Override
        public Bucket<E> remove(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            return this;
        }
        
        @Override
        public E getFirst() {
            throw new NoSuchElementException();
        }
        
        @Override
        public E findEqualOrNull(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            return null;
        }
        
        @Override
        public boolean isFullLeaf() {
            return false;
        }
        
        @Override
        public int leafSize() {
            return 0;
        }
    }
    
    static class InnerBucket<E> implements Bucket<E> {
        
        private final Bucket<E>[] buckets;
        
        private final long occupied;
        
        InnerBucket(Bucket<E>[] buckets, long occupied) {
            this.buckets = buckets;
            this.occupied = occupied;
        }
        
        @Override
        public Bucket<E> add(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int rawIndex = rawIndex(hashValue >>> rshift);
            int subBucketIndex = index0(rawIndex);
            Bucket<E> newBucket = buckets[subBucketIndex].add(identificator, element, hashValue, rshift + HASH_BITS);
            if (newBucket == buckets[subBucketIndex]) {
                return this;
            }
            if (!newBucket.isFullLeaf()) {
                Bucket<E>[] newBuckets = buckets.clone();
                newBuckets[subBucketIndex] = newBucket;
                return new InnerBucket<E>(newBuckets, occupied);
            }
            return split(buckets, (HashBucket<E>)newBucket, rawIndex, occupied, rshift);
        }
        
        private static <E> Bucket<E> split(Bucket<E>[] innerBuckets, HashBucket<E> fullBucket, int rawIndex, long occupied, int rshift) {
            while (true) {
                int index = index(occupied, rawIndex);
                int start = 64 - Long.numberOfLeadingZeros(occupied & ((1L << rawIndex) - 1));
                int zeros = Long.numberOfTrailingZeros(occupied >>> start);
                // 'zeros' is a value between 0 and (2 ^ HASH_BITS) - 1.
                if (zeros == 0) {
                    // If zeros is 0, we need a new indirection level.
                    int lowestBitAfterSplit = rshift + HASH_BITS;
                    Bucket<E> newBucket;
                    if (lowestBitAfterSplit < 32) {
                        // Recursive call to split.
                        newBucket = PersistentHashSet.split(fullBucket, rshift + HASH_BITS);
                    } else {
                        // We have used the full 32bit of the hash, now collisions are unavoidable.
                        newBucket = fullBucket.toCollisionBucket();
                    }
                    Bucket<E>[] newBuckets = innerBuckets.clone();
                    newBuckets[index] = newBucket;
                    return new InnerBucket<E>(newBuckets, occupied);
                } else {
                    // Calculate log2, to get a number in the range [0, HASH_BITS - 1]. 
                    int splitBit = Integer.numberOfTrailingZeros(zeros + 1) - 1;
                    Bucket<E>[] splitBuckets = fullBucket.split(rshift + splitBit);
                    innerBuckets = ArrayUtil.insertArrayElement(innerBuckets, index, splitBuckets[0]);
                    innerBuckets[index + 1] = splitBuckets[1];
                    int newIndex = start + zeros / 2;
                    Assert.assertFalse(PersistentHashSet.isOccupied(occupied, newIndex), "Should not be occupied");
                    occupied = occupied ^ (1L << newIndex);
                    if (splitBuckets[0].leafSize() == 0) {
                        // Split failed, one of the splits is empty, retry with different hash bit.            
                        continue;
                    }
                    if (splitBuckets[1].leafSize() == 0) {
                        // Split failed, one of the splits is empty, retry with different hash bit.
                        rawIndex = newIndex;
                        continue;
                    }
                    return new InnerBucket<E>(innerBuckets, occupied);
                }
            }
        }
        
        @Override
        public Bucket<E> remove(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int rawIndex = rawIndex(hashValue >>> rshift);
            int subBucketIndex = index0(rawIndex);
            Bucket<E> newBucket = buckets[subBucketIndex].remove(identificator, element, hashValue, rshift + HASH_BITS);
            if (newBucket == buckets[subBucketIndex]) {
                return this;
            }
            
            // Try to merge leafs.
            // NOTE merging here takes approx 20% of remove cpu
            rawIndex += Long.numberOfTrailingZeros(occupied >> rawIndex);
            int mergePartnerRawIndex = mergePartner(rawIndex);
            if (mergePartnerRawIndex != -1) {
                int mergePartnerIndex = index0(mergePartnerRawIndex);
                Bucket<E> mergePartnerBucket = buckets[mergePartnerIndex];
                int newBucketSize = newBucket.leafSize();
                int mergePartnerSize = mergePartnerBucket.leafSize();
                if (newBucketSize + mergePartnerSize < sBucketMergeSize) {
                    Bucket<E> mergedBucket = merge(identificator, newBucket, mergePartnerBucket, rshift);
                    if (buckets.length == 2) {
                        return mergedBucket;
                    }
                    
                    Bucket<E>[] newBuckets;
                    long newOccupied = occupied;
                    if (mergePartnerRawIndex < rawIndex) {
                        newBuckets = ArrayUtil.removeArrayElement(buckets, mergePartnerIndex);
                        newBuckets[mergePartnerIndex] = mergedBucket;
                        newOccupied ^= (1L << mergePartnerRawIndex);
                    } else {
                        newBuckets = ArrayUtil.removeArrayElement(buckets, subBucketIndex);
                        newBuckets[subBucketIndex] = mergedBucket;
                        newOccupied ^= (1L << rawIndex);
                    }
                    return new InnerBucket<E>(newBuckets, newOccupied);
                }
            }
            
            Bucket<E>[] newBuckets = buckets.clone();
            newBuckets[subBucketIndex] = newBucket;
            return new InnerBucket<E>(newBuckets, occupied);
        }
        
        private Bucket<E> merge(Identificator<? super E> identificator, Bucket<E> bucket1, Bucket<E> bucket2, int rshift) {
            if (bucket1 instanceof EmptyBucket) {
                return bucket2;
            }
            if (bucket2 instanceof EmptyBucket) {
                return bucket1;
            }
            return HashBucket.merge(identificator, (HashBucket<E>)bucket1, (HashBucket<E>)bucket2, rshift);
        }
        
        private int mergePartner(int index) {
            int lowerIndex = lowerIndex(index);
            int mergeLength = mergeLength(index, lowerIndex);
            if (mergeLength != -1) {
                if (mergeLength(lowerIndex, lowerIndex(lowerIndex)) == -1) {
                    return lowerIndex;
                }
            } else {
                int higherIndex = higherIndex(index);
                if (mergeLength(index, higherIndex) != -1) {
                    return higherIndex;
                }
            }
            return -1;
        }
        
        private int lowerIndex(int index) {
            long bit = 1L << index;
            return 63 - Long.numberOfLeadingZeros(occupied & (bit - 1));
        }
        
        private int higherIndex(int index) {
            long bit = 1L << (index + 1);
            return Long.numberOfTrailingZeros(occupied & (~(bit - 1)));
        }
        
        private int mergeLength(int index1, int index2) {
            int xor = index1 ^ index2;
            if ((xor & (xor - 1)) == 0) {
                return xor;
            }
            return -1;
        }
        
        @Override
        public boolean findNext(EntryIterator<E> iterator) {
            int idx;
            while ((idx = iterator.getAndIncrementIndex()) < buckets.length) {
                Bucket<E> bucket = buckets[idx];
                iterator.push(bucket);
                return bucket.findNext(iterator);
            }
            return false;
        }
        
        @Override
        public E getFirst() {
            for (int i = 0; i < buckets.length; ++i) {
                Bucket<E> bucket = buckets[i];
                if (bucket.leafSize() == 0) {
                    continue;
                }
                return bucket.getFirst();
            }
            throw new IllegalStateException();
        }
        
        @Override
        public E findEqualOrNull(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int index = index0(rawIndex(hashValue >>> rshift));
            return buckets[index].findEqualOrNull(identificator, element, hashValue, rshift + HASH_BITS);
        }
        
        private int index0(int rawIndex) {
            // Top-level InnerBuckets are often full. This is a short-cut which avoid the bit-counting in this case.
            if (occupied == -1) {
                return rawIndex;
            }
            return index(occupied, rawIndex);
        }
        
        @Override
        public boolean isFullLeaf() {
            return false;
        }
        
        @Override
        public int leafSize() {
            return HASH_TABLE_SIZE;
        }
    }
    
    static class HashBucket<E> implements Bucket<E> {
        
        private final E[] elements;
        private final int[] hashValues;
        private final long occupied;
        
        HashBucket(E[] elements, int[] hashValues, long occupied) {
            Assert.assertTrue(elements.length <= sBucketSplitSize, "Overfull hash bucket!");
            this.elements = elements;
            this.hashValues = hashValues;
            this.occupied = occupied;
        }
        
        @Override
        public int leafSize() {
            return elements.length;
        }
        
        @Override
        public HashBucket<E> add(Identificator<? super E> identificator, E element, int hashvalue, int rshift) {
            int rawIndex = rawIndex(hashvalue);
            long bit = 1L << rawIndex;
            int index = Long.bitCount(occupied & (bit - 1));
            
            while ((occupied & bit) != 0) {
                if (hashValues[index] == hashvalue && identificator.equals(element, elements[index])) {
                    // The element is already contained in the map.
                    return this;
                }
                bit <<= 1;
                if (bit == 0) {
                    index = 0;
                    bit = 1;
                } else {
                    index++;
                }
            }
            
            // Insert new element into hash table.
            E[] newElements = ArrayUtil.insertArrayElement(elements, index, element);
            int[] newHashValues = ArrayUtil.insertArrayElement(hashValues, index, hashvalue);
            return new HashBucket<E>(newElements, newHashValues, occupied | bit);
        }
        
        static final byte[] identity;
        static {
            identity = new byte[64];
            for (byte i = 0; i < 64; ++i) {
                identity[i] = i;
            }
        }
        
        @Override
        public Bucket<E> remove(Identificator<? super E> identificator, E element, int hashvalue, int rshift) {
            int rawIndex = rawIndex(hashvalue);
            long bit = 1L << rawIndex;
            int index = Long.bitCount(occupied & (bit - 1));
            
            while (true) {
                if ((occupied & bit) == 0) {
                    // Element is not present.          
                    return this;
                }
                if (hashValues[index] == hashvalue && identificator.equals(element, elements[index])) {
                    // Found the element.
                    break;
                }
                rawIndex++;
                if (rawIndex == HASH_TABLE_SIZE) {
                    // Wrap around.
                    rawIndex = 0;
                    index = 0;
                    bit = 1;
                } else {
                    index++;
                    bit <<= 1;
                }
            }
            
            if (leafSize() == 1) {
                return emptyLeaf();
            }
            
            E[] newElements = ArrayUtil.removeArrayElement(elements, index);
            int[] newHashValues = ArrayUtil.removeArrayElement(hashValues, index);
            long newOccupied = occupied ^ (1L << rawIndex);
            
            // To handle collisions correctly, we need to re-insert all elements that come after
            // the given index until we reach an empty index.
            long rotated = Long.rotateRight(occupied, rawIndex);
            int nextEmpty = Long.numberOfTrailingZeros(rotated + 1);
            int hole = rawIndex;
            for (int i = 1; i < nextEmpty; ++i) {
                // Consider moving element currently at position rawIndex + i:
                int currentRawIndex = (rawIndex + i) & HASH_MASK;
                int currentIndex = index(occupied, currentRawIndex);
                int hashValueI = hashValues[currentIndex];
                int rawIndexI = rawIndex(hashValueI);
                
                // Only move the element to the hole if the hole is closer to the ideal position 
                // than the current position.
                int distanceHole = (hole - rawIndexI) & HASH_MASK;
                int distanceCurrentPos = (currentRawIndex - rawIndexI) & HASH_MASK;
                if (distanceCurrentPos <= distanceHole) {
                    // Position i is already the perfect place for the element.
                    continue;
                }
                
                int newElementsIndex = index(newOccupied, currentRawIndex);
                newOccupied ^= (1L << currentRawIndex);
                int k = index(newOccupied, hole);
                newOccupied ^= (1L << hole);
                if (newElementsIndex != k) {
                    if (newElementsIndex < k) {
                        System.arraycopy(newElements, newElementsIndex + 1, newElements, newElementsIndex, k - newElementsIndex);
                        System.arraycopy(newHashValues, newElementsIndex + 1, newHashValues, newElementsIndex, k - newElementsIndex);
                    } else {
                        System.arraycopy(newElements, k, newElements, k + 1, newElementsIndex - k);
                        System.arraycopy(newHashValues, k, newHashValues, k + 1, newElementsIndex - k);
                    }
                    newElements[k] = elements[currentIndex];
                    newHashValues[k] = hashValueI;
                }
                hole = currentRawIndex;
            }
            
            return new HashBucket<>(newElements, newHashValues, newOccupied);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + " " + Arrays.toString(elements) + " " + Long.toBinaryString(occupied);
        }
        
        @Override
        public boolean findNext(EntryIterator<E> iterator) {
            int index = iterator.getAndIncrementIndex();
            if (index >= elements.length) return false;
            iterator.setCurrent(elements[index]);
            return true;
        }
        
        @Override
        public E getFirst() {
            return elements[0];
        }
        
        @Override
        public E findEqualOrNull(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            long bit = 1L << rawIndex(hashValue);
            int index = Long.bitCount(occupied & (bit - 1));
            while ((occupied & bit) != 0) {
                if (hashValues[index] == hashValue && identificator.equals(element, elements[index])) {
                    return elements[index];
                }
                bit <<= 1;
                if (bit == 0) {
                    index = 0;
                    bit = 1;
                } else {
                    index++;
                }
            }
            return null;
        }
        
        @Override
        public boolean isFullLeaf() {
            return elements.length >= sBucketSplitSize;
        }
        
        public Bucket<E>[] split(int bit) {
            if (bit >= 32) {
                // Bits higher than 31 are always considered to be zero. Therefore, all the
                // entries fall into the lower bucket.
                Bucket<E> empty = emptyLeaf();
                return ArrayUtil.arrayOf(this, empty);
            }
            
            byte[] zeroIndices = new byte[HASH_TABLE_SIZE];
            long zeroOccupied = 0;
            
            byte[] oneIndices = new byte[HASH_TABLE_SIZE];
            long oneOccupied = 0;
            
            int mask = 1 << bit;
            for (byte i = 0; i < elements.length; ++i) {
                int hashValue = hashValues[i];
                if ((hashValue & mask) == 0) {
                    int index = findInsertPosition(hashValue, zeroOccupied);
                    zeroIndices[index] = i;
                    zeroOccupied |= (1L << index);
                } else {
                    int index = findInsertPosition(hashValue, oneOccupied);
                    oneIndices[index] = i;
                    oneOccupied |= (1L << index);
                }
            }
            
            if (zeroOccupied == 0) {
                return ArrayUtil.arrayOf(PersistentHashSet.<E>emptyLeaf(), this);
            }
            if (oneOccupied == 0) {
                return ArrayUtil.arrayOf(this, PersistentHashSet.<E>emptyLeaf());
            }
            Bucket<E> zeroBucket = createBucket(zeroIndices, zeroOccupied);
            Bucket<E> oneBucket = createBucket(oneIndices, oneOccupied);
            return ArrayUtil.arrayOf(zeroBucket, oneBucket);
        }
        
        private int findInsertPosition(int hashValue, long occupied) {
            int index = hashValue & HASH_MASK;
            long rotated = Long.rotateRight(occupied, index);
            int nextFree = Long.numberOfTrailingZeros(rotated + 1);
            return (index + nextFree) & HASH_MASK;
        }
        
        private Bucket<E> createBucket(byte[] indices, long occupied) {
            int size = Long.bitCount(occupied);
            E[] elementsCompact = ArrayUtil.unsafeCastedNewArray(size);
            int[] hashesCompact = new int[size];
            long bits = occupied;
            for (int i = 0, j = 0; i < size; ++i) {
                int gap = Long.numberOfTrailingZeros(bits);
                j += gap;
                int index = indices[j];
                elementsCompact[i] = elements[index];
                hashesCompact[i] = hashValues[index];
                j++;
                bits >>>= (gap + 1);
            }
            return new HashBucket<E>(elementsCompact, hashesCompact, occupied);
        }
        
        private Bucket<E> toCollisionBucket() {
            return CollisionBucket.create(elements, hashValues);
        }
        
        private static <E> Bucket<E> merge(Identificator<? super E> identificator, HashBucket<E> bucket1, HashBucket<E> bucket2, int rshift) {
            int bucket1Size = bucket1.leafSize();
            int bucket2Size = bucket2.leafSize();
            if (bucket1Size > bucket2Size) {
                HashBucket<E> tmp = bucket2;
                bucket2 = bucket1;
                bucket1 = tmp;
            }
            // bucket1 is the smaller.
            Bucket<E> result = bucket2;
            // TODO optimize
            for (int i = 0; i < bucket1.leafSize(); ++i) {
                result = result.add(identificator, bucket1.elements[i], bucket1.hashValues[i], rshift);
            }
            return result;
        }
    }
    
    private static class CollisionBucket<E> implements Bucket<E> {
        
        private final E[][] elements;
        
        private final long occupied;
        
        private final int size;
        
        private final int hashBase;
        
        static <E> Bucket<E> create(E[] elements, int[] hashValues) {
            int hashBase = hashValues[0] & (~HASH_MASK);
            byte[] count = new byte[HASH_TABLE_SIZE];
            long occupied = 0;
            for (int i = 0; i < hashValues.length; ++i) {
                int index = rawIndex(hashValues[i]);
                if (count[index] == 0) {
                    occupied |= (1L << index);
                }
                count[index]++;
            }
            
            int size = Long.bitCount(occupied);
            
            @SuppressWarnings("unchecked")
            E[][] groupedElements = (E[][])new Object[size][];
            int pos = 0;
            for (int i = 0; i < HASH_TABLE_SIZE; ++i) {
                if (count[i] == 0) {
                    continue;
                }
                groupedElements[pos] = ArrayUtil.unsafeCastedNewArray(count[i]);
                pos++;
            }
            Assert.assertEquals(pos, size);
            
            byte[] full = new byte[size]; // TODO reuse count array?
            for (int i = 0; i < hashValues.length; ++i) {
                int index = index(occupied, rawIndex(hashValues[i]));
                groupedElements[index][full[index]] = elements[i];
                full[index]++;
            }
            
            return new CollisionBucket<E>(groupedElements, occupied, elements.length, hashBase);
        }
        
        private CollisionBucket(E[][] elements, long occupied, int size, int hashBase) {
            Assert.assertTrue(size >= sBucketMergeSize, "Underfull CollisionBucket!");
            this.elements = elements;
            this.occupied = occupied;
            this.size = size;
            this.hashBase = hashBase;
        }
        
        @Override
        public E findEqualOrNull(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int rawIndex = rawIndex(hashValue);
            if (!isOccupied(occupied, rawIndex)) {
                return null;
            }
            int index = index(occupied, rawIndex);
            int indexOf = indexOf(identificator, elements[index], element);
            return (indexOf == -1) ? null : elements[index][indexOf];
        }
        
        @Override
        public Bucket<E> add(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int rawIndex = rawIndex(hashValue);
            int index = index(occupied, rawIndex);
            if (isOccupied(occupied, rawIndex)) {
                if (indexOf(identificator, elements[index], element) != -1) {
                    return this;
                }
                E[] newElementsForIndex = ArrayUtil.appendArrayElement(elements[index], element);
                E[][] newElements = elements.clone();
                newElements[index] = newElementsForIndex;
                return new CollisionBucket<>(newElements, occupied, size + 1, hashBase);
            } else {
                E[] newElementsForIndex = ArrayUtil.arrayOf(element);
                E[][] newElements = ArrayUtil.insertArrayElement(elements, index, newElementsForIndex);
                return new CollisionBucket<>(newElements, occupied | (1L << rawIndex), size + 1, hashBase);
            }
        }
        
        private int indexOf(Identificator<? super E> identificator, E[] elements, E element) {
            for (int i = 0; i < elements.length; ++i) {
                if (identificator.equals(element, elements[i])) {
                    return i;
                }
            }
            return -1;
        }
        
        @Override
        public Bucket<E> remove(Identificator<? super E> identificator, E element, int hashValue, int rshift) {
            int rawIndex = rawIndex(hashValue);
            if (!isOccupied(occupied, rawIndex)) {
                return this;
            }
            int index = index(occupied, rawIndex);
            int i = indexOf(identificator, elements[index], element);
            if (i == -1) {
                return this;
            }
            
            if (size == sBucketMergeSize) {
                // TODO optimize
                return addAllTo(identificator, PersistentHashSet.<E>emptyLeaf(), rshift).remove(identificator, element, hashValue, rshift);
            }
            
            if (elements[index].length == 1) {
                if (elements.length == 1) {
                    return emptyLeaf();
                }
                E[][] newElements = ArrayUtil.removeArrayElement(elements, index);
                return new CollisionBucket<>(newElements, occupied ^ (1L << rawIndex), size - 1, hashBase);
            } else {
                E[] newElementsForIndex = ArrayUtil.removeArrayElement(elements[index], i);
                E[][] newElements = elements.clone();
                newElements[index] = newElementsForIndex;
                return new CollisionBucket<>(newElements, occupied, size - 1, hashBase);
            }
        }
        
        @Override
        public boolean findNext(EntryIterator<E> iterator) {
            int idx;
            while ((idx = iterator.getAndIncrementIndex()) < elements.length) {
                final int outerIndex = idx;
                BucketIterable<E> innerIterable = new BucketIterable<E>() {
                    @Override
                    public boolean findNext(EntryIterator<E> iterator) {
                        int index = iterator.getAndIncrementIndex();
                        if (index >= elements[outerIndex].length) return false;
                        iterator.setCurrent(elements[outerIndex][index]);
                        return true;
                    }
                };
                iterator.push(innerIterable);
                return innerIterable.findNext(iterator);
            }
            return false;
        }
        
        @Override
        public E getFirst() {
            return elements[0][0];
        }
        
        @Override
        public boolean isFullLeaf() {
            return false;
        }
        
        @Override
        public int leafSize() {
            return size;
        }
        
        private Bucket<E> addAllTo(Identificator<? super E> identificator, Bucket<E> bucket, int rshift) {
            // TODO optimize
            for (int i = 0; i < HASH_TABLE_SIZE; ++i) {
                if (!isOccupied(occupied, i)) {
                    continue;
                }
                int hashValue = hashBase + i;
                int index = index(occupied, i);
                for (int j = 0; j < elements[index].length; ++j) {
                    bucket = bucket.add(identificator, elements[index][j], hashValue, rshift);
                }
            }
            return bucket;
        }
    }
    
    protected final int size;
    
    protected final Bucket<E> root;
    
    protected final Identificator<? super E> identificator;
    
    private static class EmptyPersistentHashSet<E> extends AbstractUnOrderedCollection<E> implements PersistentSet<E> {
        
        private final Identificator<? super E> identificator;
        
        EmptyPersistentHashSet(Identificator<? super E> identificator) {
            this.identificator = identificator;
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public Identificator<? super E> getIdentificator() {
            return identificator;
        }
        
        @Override
        @CheckForNull
        public E getFirstOrNull() {
            return null;
        }
        
        @Override
        public Iterator<E> iterator() {
            return EmptyIterator.getInstance();
        }
        
        @Override
        public boolean contains(E element) {
            return false;
        }
        
        @Override
        public E findEqualOrNull(E element) {
            return null;
        }
        
        @Override
        public PersistentSet<E> with(E element) {
            int hashValue = identificator.hashCode(element);
            long occupied = 1L << (hashValue & HASH_MASK);
            return new PersistentHashSet<E>(1, new HashBucket<>(ArrayUtil.arrayOf(element), new int[] {hashValue}, occupied), identificator);
        }
        
        @Override
        public PersistentSet<E> withAll(Iterable<? extends E> elements) {
            // TODO optimize
            PersistentSet<E> result = this;
            for (E element: elements) {
                result = result.with(element);
            }
            return result;
        }
        
        @Override
        public PersistentSet<E> without(E element) {
            PreConditions.paramNotNull(element);
            return this;
        }
        
        @Override
        public PersistentSet<E> withoutAll(Iterable<? extends E> elements) {
            return this;
        }
        
        @Override
        public PersistentSet<E> cleared() {
            return this;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public static <E> PersistentSet<E> empty() {
        return (PersistentSet<E>)EMPTY_SET;
    }
    
    public static <E> PersistentSet<E> empty(Identificator<? super E> identificator) {
        return new EmptyPersistentHashSet<>(identificator);
    }
    
    public static <E> PersistentSet<E> copyOf(Collection<E> collection) {
        return PersistentHashSet.<E>empty().withAll(collection);
    }
    
    PersistentHashSet(int size, Bucket<E> root, Identificator<? super E> identificator) {
        this.size = size;
        this.root = root;
        this.identificator = identificator;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return identificator;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        // EmptyPersistentHashSet is used for empty set, so instances of PersistentHashSet are never empty.
        return root.getFirst();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new EntryIterator<>(root);
    }
    
    protected static class EntryIterator<E> implements Iterator<E> {
        
        /**
         * Stack of buckets which marks the current path of the iteration in the
         * tree.
         */
        private final ArrayList<BucketIterable<E>> bucketStack = new ArrayList<>(MAX_HEIGHT);
        
        /**
         * For each entry on the stack there is an index counter available.
         */
        private final int[] position = new int[MAX_HEIGHT];
        
        @CheckForNull
        private E currentKey = null;
        
        public EntryIterator(Bucket<E> root) {
            bucketStack.push(root);
            findNext();
        }
        
        public int getAndIncrementIndex() {
            int top = bucketStack.size() - 1;
            return position[top]++;
        }
        
        public void push(BucketIterable<E> bucket) {
            position[bucketStack.size()] = 0;
            bucketStack.push(bucket);
        }
        
        public void setCurrent(E key) {
            currentKey = key;
        }
        
        @Override
        public boolean hasNext() {
            return currentKey != null;
        }
        
        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            E result = currentKey;
            findNext();
            return result;
        }
        
        private boolean findNext() {
            while (!bucketStack.isEmpty()) {
                BucketIterable<E> top = bucketStack.peek();
                if (top.findNext(this)) {
                    return true;
                }
                bucketStack.pop();
            }
            currentKey = null;
            return false;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Override
    public boolean contains(E element) {
        return findEqualOrNull(element) != null;
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return root.findEqualOrNull(identificator, element, identificator.hashCode(element), HASH_BITS);
    }
    
    @Override
    public PersistentSet<E> with(E element) {
        Bucket<E> newRoot = addToRoot(root, element);
        if (newRoot == root) {
            return this;
        }
        return new PersistentHashSet<E>(size + 1, newRoot, identificator);
    }
    
    Bucket<E> addToRoot(Bucket<E> root, E element) {
        Bucket<E> newRoot = root.add(identificator, element, identificator.hashCode(element), HASH_BITS);
        if (newRoot != root && newRoot.isFullLeaf()) {
            return split((HashBucket<E>)newRoot, HASH_BITS);
        }
        return newRoot;
    }
    
    static <E> Bucket<E> split(HashBucket<E> fullBucket, int rshift) {
        return InnerBucket.split(ArrayUtil.<Bucket<E>>arrayOf(fullBucket), fullBucket, HASH_TABLE_SIZE - 1, 1L << (HASH_TABLE_SIZE - 1), rshift);
    }
    
    @Override
    public PersistentSet<E> withAll(Iterable<? extends E> elements) {
        Bucket<E> result = root;
        int newSize = size;
        for (E element: elements) {
            Bucket<E> newRoot = addToRoot(result, element);
            if (newRoot != result) {
                result = newRoot;
                newSize++;
            }
        }
        if (result == root) {
            return this;
        }
        return new PersistentHashSet<E>(newSize, result, identificator);
    }
    
    @Override
    public PersistentSet<E> without(E element) {
        Bucket<E> newRoot = root.remove(identificator, element, identificator.hashCode(element), HASH_BITS);
        if (newRoot == root) {
            return this;
        }
        if (newRoot.leafSize() == 0) {
            return cleared();
        }
        return new PersistentHashSet<>(size - 1, newRoot, identificator);
    }
    
    @Override
    public PersistentSet<E> withoutAll(Iterable<? extends E> elements) {
        Bucket<E> result = root;
        int newSize = size;
        for (E element: elements) {
            Bucket<E> newRoot = result.remove(identificator, element, identificator.hashCode(element), HASH_BITS);
            if (newRoot != result) {
                result = newRoot;
                newSize--;
            }
        }
        if (result == root) {
            return this;
        }
        if (result.leafSize() == 0) {
            return cleared();
        }
        return new PersistentHashSet<E>(newSize, result, identificator);
    }
    
    @Override
    public PersistentSet<E> cleared() {
        return empty(identificator);
    }
    
    public static <E> Builder<E> newBuilder() {
        return newBuilder(CollectionUtil.getObjectIdentificator());
    }
    
    public static <E> Builder<E> newBuilder(Identificator<? super E> identificator) {
        return new Builder<>(identificator);
    }
    
//  /**
//   * Builds a bucket tree containing the given unique elements.
//   */
//  private static <E> Bucket<E> buildBucket(Identificator<? super E> identificator, E[] elements, int[] hashValues, int[] indices, long rshift, int length) {
//    // FIXME this fails in the large performance test
//    if (length < sBucketSplitSize) {
//      // Build a single hash bucket with the elements.
//      // Do a simple insertion sort of the elements according to the raw index.
//      E[] newElements = ArrayUtil.newObjectArrayCasted(length);
//      int[] newHashValues = new int[length];
//      for(int i=0; i<length; ++i) {
//        // Put elements[indices[i]] in the correct position.
//        int hashValue = hashValues[indices[i]];
//        int rawIndex = rawIndex(hashValue);
//        int j = i;
//        while(j > 0 && rawIndex(newHashValues[j-1]) > rawIndex) {
//          newElements[j] = newElements[j-1];
//          newHashValues[j] = newHashValues[j-1];
//          j--;
//        }
//        newElements[j] = elements[indices[i]];
//        newHashValues[j] = hashValue;;
//      }
//
//      // Fill occupied bit array.
//      long occupied = 0;
//      int lastUsed = -1;
//      for(int i=0; i<length; ++ i) {
//        int rawIndex = rawIndex(newHashValues[i]);
//        int index = Math.max(lastUsed + 1, rawIndex);
//        if (index >= HASH_TABLE_SIZE) {
//          while(isOccupied(occupied, index & HASH_MASK)) {
//            // TODO Cover this case in a test.
//            index++;
//          }
//          // Fix element position
//          E element = newElements[i];
//          int hashValue = newHashValues[i];
//          int targetIndex = index & HASH_MASK;
//          for(int j = i; j>targetIndex; --j) { // Use arraycopy?
//            newElements[j] = newElements[j-1];
//            newHashValues[j] = newHashValues[j-1];
//          }
//          newElements[targetIndex] = element;
//          newHashValues[targetIndex] = hashValue;
//        }
//        occupied |= 1L << (index & HASH_MASK);
//        lastUsed = index;
//      }
//      
//      return new HashBucket<>(newElements, newHashValues, occupied);
//    }
//    
//    // TODO: We could use a two-pass algorithm, first only count how many entries there will be
//    // in each sub-bucket. Then, create the arrays with the correct size. This would safe memory
//    // and avoid copying the data when the array need to be resized.
//    int[] full = new int[HASH_TABLE_SIZE];
//    int size = 0;
//    int initialSize = (2 * length / HASH_TABLE_SIZE) + 1;
//    int[][] subBuckets = new int[HASH_TABLE_SIZE][];
//    for(int i=0; i<length; ++i) {
//      int index = indices[i];
//      int hashValue = hashValues[index];
//      int rawIndex = rawIndex(hashValue >>> rshift);
//      if (subBuckets[rawIndex] == null) {
//        subBuckets[rawIndex] = new int[initialSize];
//        size++;
//      } else if (subBuckets[rawIndex].length == full[rawIndex]) {
//        subBuckets[rawIndex] = Arrays.copyOf(subBuckets[rawIndex], subBuckets[rawIndex].length * 2);
//      }
//      subBuckets[rawIndex][full[rawIndex]] = index;
//      full[rawIndex]++;
//    }
//    
//    if (rshift < 32) {
//      // TODO This creates too many buckets. We need to merge inner buckets until we get a full bucket.
//      @SuppressWarnings("unchecked")
//      Bucket<E>[] subBucketArray = new Bucket[HASH_TABLE_SIZE];
//      long occupied = 0;
//      for (int i = 0; i < HASH_TABLE_SIZE; ++i) {
//        subBucketArray[i] = buildBucket(identificator, elements, hashValues, subBuckets[i], rshift + HASH_BITS, full[i]);
//        occupied |= 1L << i;
//      }
//      return new InnerBucket<E>(subBucketArray, occupied);
//    } else {
//      @SuppressWarnings("unchecked")
//      E[][] groupedElements = (E[][])new Object[size][];
//      int pos = 0;
//      long occupied = 0;
//      for(int i = 0; i < HASH_TABLE_SIZE; ++i) {
//        if (full[i] == 0) {
//          continue;
//        }
//        groupedElements[pos] = ArrayUtil.newObjectArrayCasted(full[i]);
//        for(int j = 0; j<full[i]; ++j) {
//          groupedElements[pos][j] = elements[subBuckets[i][j]];
//        }
//        occupied |= (1L << i);
//        pos++;
//      }
//      return new CollisionBucket<>(groupedElements, occupied, length, hashValues[indices[0]] & (~HASH_MASK));
//    }
//  }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentSet<E>> {
        
        private final Identificator<? super E> identificator;
        
        @Nullable
        private HashSet<E> set;
        
        protected Builder(Identificator<? super E> identificator) {
            this.identificator = identificator;
            set = new HashSet<>(identificator);
        }
        
        @Override
        protected void _add(E element) {
            set.add(element);
        }
        
        @Override
        protected PersistentSet<E> _createCollection() {
            return new EmptyPersistentHashSet<E>(identificator).withAll(set);
//      int size = set.size();
//      if (size == 0) {
//        return empty(identificator);
//      }
//      E[] array = ArrayUtil.newObjectArrayCasted(size);
//      int[] hashValues = new int[array.length];
//      int i = 0;
//      for (E element: set) {
//        array[i] = element;
//        hashValues[i] = element.hashCode();
//        i++;
//      }
//      Bucket<E> root = buildBucket(identificator, array, hashValues, MathUtil.identityPermutation(size), HASH_BITS, size);
//      return new PersistentHashSet<>(size, root, identificator);
        }
        
        @Override
        protected void _reset() {
            set = new HashSet<>(identificator);
        }
    }
    
}
