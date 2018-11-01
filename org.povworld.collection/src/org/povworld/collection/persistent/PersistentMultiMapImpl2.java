package org.povworld.collection.persistent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractKeySet;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.EntryValueIterator;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.SingletonIterator;
import org.povworld.collection.immutable.ImmutableSet;

/**
 * NOTE: This is an alternative implementation of {@link PersistentMultiMapImpl}.
 * 
 * @author micha
 */
@Immutable
public class PersistentMultiMapImpl2<K, V> extends AbstractMap<K, Set<V>> implements PersistentMultiMap<K, V> {
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMultiMapImpl2<K, V> empty() {
        return (PersistentMultiMapImpl2<K, V>)EMPTY_MAP;
    }
    
    public static <K, V> PersistentMultiMapImpl2<K, V> empty(Identificator<? super K> identificator) {
        return new PersistentMultiMapImpl2<K, V>(identificator);
    }
    
    public static final int HASH_BITS = 5;
    
    public static final int HASHTABLE_SIZE = 1 << HASH_BITS;
    
    private static final int HASH_MASK = HASHTABLE_SIZE - 1;
    
    private static final int MAX_HEIGHT = 2 * (((32 + HASH_BITS - 1) / HASH_BITS) + 1);
    
    // NOTE: empty bucket must be created before empty map!
    private static final EmptyBucket<Object, Object> EMPTY_BUCKET = new EmptyBucket<Object, Object>();
    
    // TODO make special class for empty entry iterator
    private static final EntryIterator<Object, Set<Object>> EMPTY_ITERATOR = new MapEntryIterator<Object, Object>(
            EMPTY_BUCKET);
    
    private static final PersistentMultiMapImpl2<Object, Object> EMPTY_MAP;
    
    static {
        
        EMPTY_MAP = new PersistentMultiMapImpl2<Object, Object>() {
            
            @Override
            public org.povworld.collection.EntryIterator<Object, Set<Object>> entryIterator() {
                return EMPTY_ITERATOR;
            }
            
        };
        
    }
    
    @Immutable
    protected static interface Bucket<K, V> {
        
        public PersistentSet<V> get(Identificator<? super K> identificator, K key, int keyHash, int rshift);
        
        @CheckForNull
        public K findKey(Identificator<? super K> identificator, K key, int keyHash, int rshift);
        
        public boolean contains(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift);
        
        public int numberOfKeys();
        
        /**
         * Insert a key/value pair into the bucket.
         * @param keyHash the key's hash code
         * @param rshift the bucket's right shift
         * @return an updated bucket or the same bucket when the same key/value pair was already contained
         */
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, V insertValue, int rshift);
        
        /**
         * @return an updated bucket or the same bucket when the same key/value pair was already contained
         */
        public Bucket<K, V> putAll(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, Iterable<? extends V> values,
                int rshift);
        
        /**
         * NOTE: Returns {@code null} iff the new bucket would be empty!
         */
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, int rshift);
        
        /**
         * NOTE: Returns {@code null} iff the new bucket would be empty!
         */
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, V removeValue, int rshift);
        
        public boolean findNext(MapEntryIterator<K, V> iterator);
        
    }
    
    @Immutable
    protected static interface SingleKeyBucket<K, V> extends Bucket<K, V>, PersistentSet<V> {
        
        public SingleKeyBucket<K, V> putValueForSingleKey(V insertValue);
        
        public SingleKeyBucket<K, V> putValuesForSingleKey(Iterable<? extends V> insertValues);
        
        @CheckForNull
        public SingleKeyBucket<K, V> removeValueForSingleKey(V removeValue);
        
        public K getKey();
        
        public int getKeyHash();
        
    }
    
    // TODO actually use value identificator
    private static int valueHash(int hash) {
        return ObjectUtil.strengthenedHashcode(hash);
    }
    
    private static class EmptyBucket<K, V> implements Bucket<K, V> {
        
        @Override
        @CheckForNull
        public K findKey(Identificator<? super K> comparator, K key, int hashvalue, int rshift) {
            return null;
        }
        
        @Override
        public boolean contains(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift) {
            return false;
        }
        
        @Override
        public PersistentSet<V> get(Identificator<? super K> identificator, K key, int keyHash, int rshift) {
            return PersistentCollections.setOf();
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, K key, int keyHash, V value, int rshift) {
            return new SingleEntryBucket<K, V>(key, value, keyHash, valueHash(value.hashCode()));
        }
        
        @Override
        public Bucket<K, V> putAll(Identificator<? super K> keyIdentificator, K key, int keyHash, Iterable<? extends V> values, int rshift) {
            return createSingleKeyBucket(key, keyHash, values);
        }
        
        @Override
        public Bucket<K, V> remove(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift) {
            return this;
        }
        
        @Override
        public int numberOfKeys() {
            return 0;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            return false;
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K key, int keyHash, int rshift) {
            return null;
        }
        
    }
    
    private static class SingleEntryBucket<K, V> extends AbstractUnOrderedCollection<V> implements SingleKeyBucket<K, V>, PersistentSet<V> {
        
        private final K key;
        
        private final V value;
        
        private final int keyHash;
        
        private final int valueHash;
        
        public SingleEntryBucket(K key, V value, int keyHash, int valueHash) {
            this.key = key;
            this.value = value;
            this.keyHash = keyHash;
            this.valueHash = valueHash;
        }
        
        @Override
        @CheckForNull
        public K findKey(Identificator<? super K> comparator, K key, int keyHash, int rshift) {
            if ((this.keyHash == keyHash) && comparator.equals(key, this.key)) {
                return this.key;
            }
            return null;
        }
        
        @Override
        public boolean contains(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift) {
            return findKey(identificator, key, keyHash, rshift) != null && containsValue(value);
        }
        
        private boolean containsValue(V value) {
            // TODO use value identificator for equals
            return (this.valueHash == valueHash(value.hashCode())) && (this.value.equals(value));
        }
        
        @Override
        public V findEqualOrNull(V value) {
            if (contains(value)) {
                return this.value;
            }
            return null;
        }
        
        @Override
        public PersistentSet<V> get(Identificator<? super K> identificator, K key, int keyHash, int rshift) {
            if (findKey(identificator, key, keyHash, rshift) == null) {
                return PersistentCollections.setOf();
            }
            return this;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, V insertValue, int rshift) {
            if (findKey(keyIdentificator, insertKey, insertKeyHash, rshift) != null) {
                return putValueForSingleKey(insertValue);
            }
            return HashBucket.create(this, new SingleEntryBucket<>(insertKey, insertValue, insertKeyHash, valueHash(insertValue.hashCode())), rshift);
        }
        
        @Override
        public SingleKeyBucket<K, V> putValueForSingleKey(V insertValue) {
            if (containsValue(insertValue)) {
                return this;
            }
            return createSingleKeyBucket(key, keyHash, CollectionUtil.wrap(arrayOf(value, insertValue)));
        }
        
        @Override
        public Bucket<K, V> putAll(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, Iterable<? extends V> values,
                int rshift) {
            if (findKey(keyIdentificator, insertKey, insertKeyHash, rshift) != null) {
                return putValuesForSingleKey(values);
            }
            return HashBucket.create(this, createSingleKeyBucket(insertKey, insertKeyHash, values), rshift);
        }
        
        @Override
        public SingleKeyBucket<K, V> putValuesForSingleKey(Iterable<? extends V> insertValues) {
            // TODO do in single step
            SingleKeyBucket<K, V> valueBucket = createSingleKeyBucket(this.key, this.keyHash, insertValues);
            SingleKeyBucket<K, V> result = valueBucket.putValueForSingleKey(this.value);
            if (result.size() == 1) {
                return this;
            } else {
                return result;
            }
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, int rshift) {
            if (findKey(identificator, removeKey, removeKeyHash, rshift) == null) {
                return this;
            }
            return null;
        }
        
        @Override
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, V removeValue, int rshift) {
            if (findKey(identificator, removeKey, removeKeyHash, rshift) == null) {
                return this;
            }
            return removeValueForSingleKey(removeValue);
        }
        
        @Override
        @CheckForNull
        public SingleKeyBucket<K, V> removeValueForSingleKey(V removeValue) {
            if (!containsValue(removeValue)) {
                return this;
            }
            return null;
        }
        
        @Override
        public int numberOfKeys() {
            return 1;
        }
        
        @Override
        public int size() {
            return 1;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            if (iterator.getAndIncrementIndex() == 0) {
                iterator.setCurrent(key, this);
                return true;
            }
            return false;
        }
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public V getFirstOrNull() {
            return value;
        }
        
        @Override
        public V getFirst() {
            return value;
        }
        
        @Override
        public Iterator<V> iterator() {
            return new SingletonIterator<V>(value);
        }
        
        @Override
        public boolean contains(V value) {
            return containsValue(value);
        }
        
        @Override
        public PersistentSet<V> with(V value) {
            if (contains(value)) return this;
            return PersistentCollections.setOf(this.value, value);
        }
        
        @Override
        public PersistentSet<V> withAll(Iterable<? extends V> values) {
            PersistentSet<V> collection =
                    PersistentHashSet.<V>newBuilder()
                            .add(value)
                            .addAll(values)
                            .build();
            if (collection.size() == 1) {
                // The only element in the new collection must be the value from the SingleEntryBucket.
                return this;
            }
            return collection;
        }
        
        @Override
        public PersistentSet<V> without(V value) {
            if (contains(value)) return PersistentCollections.setOf();
            return this;
        }
        
        @Override
        public PersistentSet<V> withoutAll(Iterable<? extends V> values) {
            for (V value: values) {
                if (containsValue(value)) return PersistentCollections.setOf();
            }
            return this;
        }
        
        @Override
        public PersistentSet<V> cleared() {
            return PersistentCollections.setOf();
        }
        
        @Override
        public Identificator<? super V> getIdentificator() {
            return CollectionUtil.getObjectIdentificator();
        }
        
        @Override
        public int getKeyHash() {
            return keyHash;
        }
        
    }
    
    private static <K, V> SingleKeyBucket<K, V> createSingleKeyBucket(K key, int keyHash, Iterable<? extends V> values) {
        // TODO optimize    
        PersistentHashSet.Bucket<V> root = PersistentHashSet.emptyLeaf();
        int size = 0;
        for (V value: values) {
            PersistentHashSet.Bucket<V> newBucket =
                    root.add(CollectionUtil.getObjectIdentificator(), value, valueHash(value.hashCode()), PersistentHashSet.HASH_BITS);
            if (newBucket.isFullLeaf()) {
                newBucket = PersistentHashSet.split((PersistentHashSet.HashBucket<V>)newBucket, PersistentHashSet.HASH_BITS);
            }
            if (newBucket != root) {
                root = newBucket;
                size++;
            }
        }
        if (size == 1) {
            V value = root.getFirst();
            return new SingleEntryBucket<>(key, value, keyHash, valueHash(value.hashCode()));
        } else {
            return new SingleKeyHashBucket<K, V>(key, keyHash, root, size);
        }
    }
    
    /**
     * Contains key/value pairs for a single key. The values associated with the single key 
     * are stored in a {@link PersistentHashSet}.
     */
    private static class SingleKeyHashBucket<K, V> extends PersistentHashSet<V> implements SingleKeyBucket<K, V> {
        
        private final K key;
        
        private final int keyHash;
        
        private SingleKeyHashBucket(K key, int keyHash, Bucket<V> root, int size) {
            super(size, root, CollectionUtil.getObjectIdentificator());
            Assert.assertTrue(size >= 2, "Needs at least two elements!");
            this.key = key;
            this.keyHash = keyHash;
        }
        
        @Override
        public int numberOfKeys() {
            return 1;
        }
        
        @Override
        public PersistentSet<V> get(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, int rshift) {
            if (findKey(identificator, lookupKey, lookupKeyHash, rshift) == null) {
                return PersistentCollections.setOf();
            }
            return this;
        }
        
        @Override
        public K findKey(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, int rshift) {
            if ((keyHash == lookupKeyHash) && identificator.equals(key, lookupKey)) {
                return key;
            };
            return null;
        }
        
        @Override
        public boolean contains(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, V lookupValue, int rshift) {
            return findKey(identificator, lookupKey, lookupKeyHash, rshift) != null && contains(lookupValue);
        }
        
        @Override
        public PersistentMultiMapImpl2.Bucket<K, V> put(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash,
                V insertValue, int rshift) {
            if (findKey(keyIdentificator, insertKey, insertKeyHash, rshift) != null) {
                return putValueForSingleKey(insertValue);
            }
            return PersistentMultiMapImpl2.HashBucket.create(this,
                    new SingleEntryBucket<>(insertKey, insertValue, insertKeyHash, valueHash(insertValue.hashCode())), rshift);
        }
        
        @Override
        public PersistentMultiMapImpl2.Bucket<K, V> putAll(Identificator<? super K> keyIdentificator,
                K insertKey, int insertKeyHash, Iterable<? extends V> values, int rshift) {
            if (findKey(keyIdentificator, insertKey, insertKeyHash, rshift) != null) {
                return putValuesForSingleKey(values);
            }
            return PersistentMultiMapImpl2.HashBucket.create(this, createSingleKeyBucket(insertKey, insertKeyHash, values), rshift);
        }
        
        @Override
        public SingleKeyHashBucket<K, V> putValueForSingleKey(V insertValue) {
            Bucket<V> newRoot = addToRoot(root, insertValue);
            if (newRoot == root) return this;
            return new SingleKeyHashBucket<K, V>(key, keyHash, newRoot, size + 1);
        }
        
        @Override
        public SingleKeyHashBucket<K, V> putValuesForSingleKey(Iterable<? extends V> insertValues) {
            // TODO optimize hashset bulk add
            Bucket<V> newRoot = root;
            int newSize = size;
            for (V insertValue: insertValues) {
                org.povworld.collection.persistent.PersistentHashSet.Bucket<V> bucket = addToRoot(newRoot, insertValue);
                if (bucket != newRoot) {
                    newRoot = bucket;
                    newSize++;
                }
            }
            if (newRoot == root) return this;
            return new SingleKeyHashBucket<K, V>(key, keyHash, newRoot, newSize);
        }
        
        @Override
        @CheckForNull
        public PersistentMultiMapImpl2.Bucket<K, V> remove(Identificator<? super K> identificator,
                K removeKey, int removeKeyHash, int rshift) {
            if (findKey(identificator, removeKey, removeKeyHash, rshift) == null) {
                return this;
            }
            return null;
        }
        
        @Override
        @CheckForNull
        public PersistentMultiMapImpl2.Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash,
                V removeValue, int rshift) {
            if (findKey(identificator, removeKey, removeKeyHash, rshift) == null) {
                return this;
            }
            return removeValueForSingleKey(removeValue);
        }
        
        @Override
        @CheckForNull
        public SingleKeyBucket<K, V> removeValueForSingleKey(V removeValue) {
            Bucket<V> newRoot = ObjectUtil.checkNotNull(root.remove(super.identificator, removeValue, valueHash(removeValue.hashCode()), HASH_BITS));
            if (newRoot == root) {
                return this;
            }
            // As we have always at least two values, there must be at least one left.
            if (size() == 2) {
                V remaining = newRoot.getFirst();
                return new SingleEntryBucket<K, V>(key, remaining, keyHash, valueHash(remaining.hashCode()));
            }
            return new SingleKeyHashBucket<K, V>(key, keyHash, newRoot, size - 1);
        }
        
        @Override
        public boolean findNext(PersistentMultiMapImpl2.MapEntryIterator<K, V> iterator) {
            if (iterator.getAndIncrementIndex() == 0) {
                iterator.setCurrent(key, this);
                return true;
            }
            return false;
        }
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public int getKeyHash() {
            return keyHash;
        }
        
    }
    
    private static class HashBucket<K, V> implements Bucket<K, V> {
        
        private final Bucket<K, V>[] hashtable;
        
        private final int keyCount;
        
        HashBucket(Bucket<K, V>[] hashtable, int keyCount) {
            //Assert.assertTrue(keyCount > 1, "keyCount must be positive: %d", keyCount);
            //Assert.assertTrue(MathUtil.nextPowerOfTwo(hashtable.length) == hashtable.length, "hashtable size not a power of 2");
            this.hashtable = hashtable;
            this.keyCount = keyCount;
        }
        
        static <K, V> Bucket<K, V> create(SingleKeyBucket<K, V> entry1, SingleKeyBucket<K, V> entry2, int rshift) {
            if (rshift >= 32) {
                Assert.assertTrue(entry1.getKeyHash() == entry2.getKeyHash(), "hash mismatch");
                @SuppressWarnings("unchecked")
                SingleKeyBucket<K, V>[] children = new SingleKeyBucket[] {entry1, entry2};
                return new CollisionBucket<K, V>(entry1.getKeyHash(), children);
            }
            int size = minSize(entry1.getKeyHash(), entry2.getKeyHash(), rshift);
            int index1 = tableIndex(size, entry1.getKeyHash(), rshift);
            int index2 = tableIndex(size, entry2.getKeyHash(), rshift);
            if (index1 == index2) {
                Bucket<K, V> singleEntry = create(entry1, entry2, rshift + HASH_BITS);
                Bucket<K, V>[] hashtable = createHashtable(HASHTABLE_SIZE);
                hashtable[index1] = singleEntry;
                return new HashBucket<>(hashtable, 2);
            }
            Bucket<K, V>[] hashtable = createHashtable(size);
            hashtable[index1] = entry1;
            hashtable[index2] = entry2;
            return new HashBucket<>(hashtable, 2);
        }
        
        private static int tableIndex(int size, int hash, int rshift) {
            return (hash >>> rshift) & (size - 1);
        }
        
        private int tableIndex(int hash, int rshift) {
            return (hash >>> rshift) & (hashtable.length - 1);
        }
        
        @SuppressWarnings("unchecked")
        private static <K, V> Bucket<K, V>[] createHashtable(int size) {
            return new Bucket[size];
        }
        
        private Bucket<K, V> bucketOf(int hashvalue, int rshift) {
            return hashtable[tableIndex(hashvalue, rshift)];
        }
        
        @Override
        public K findKey(Identificator<? super K> comparator, K key, int keyHash, int rshift) {
            Bucket<K, V> bucket = bucketOf(keyHash, rshift);
            if (bucket == null) {
                return null;
            }
            return bucket.findKey(comparator, key, keyHash, rshift + HASH_BITS);
        }
        
        @Override
        public boolean contains(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift) {
            Bucket<K, V> bucket = bucketOf(keyHash, rshift);
            if (bucket == null) {
                return false;
            }
            return bucket.contains(identificator, key, keyHash, value, rshift + HASH_BITS);
        }
        
        @Override
        public PersistentSet<V> get(Identificator<? super K> identificator, K key, int keyHash, int rshift) {
            Bucket<K, V> bucket = bucketOf(keyHash, rshift);
            if (bucket == null) {
                return PersistentCollections.setOf();
            }
            return bucket.get(identificator, key, keyHash, rshift + HASH_BITS);
        }
        
        private boolean isSmall() {
            return hashtable.length != HASHTABLE_SIZE;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, K key, int keyHash, V value, int rshift) {
            int index = tableIndex(keyHash, rshift);
            Bucket<K, V> bucket = hashtable[index];
            if (bucket == null) {
                Bucket<K, V>[] newTable = hashtable.clone();
                newTable[index] = new SingleEntryBucket<K, V>(key, value, keyHash, valueHash(value.hashCode()));
                return new HashBucket<K, V>(newTable, keyCount + 1);
            }
            
            if (isSmall() && bucket.findKey(keyIdentificator, key, keyHash, rshift) == null) {
                // small and new key          
                int newSize = minSize(keyHash, ((SingleKeyBucket<K, V>)bucket).getKeyHash(), rshift);
                Bucket<K, V>[] newTable = resizeTable(keyIdentificator, key, keyHash, value, rshift, newSize);
                int newIndex = tableIndex(newSize, keyHash, rshift);
                if ((newSize == HASHTABLE_SIZE) && (newTable[newIndex] != null)) {
                    newTable[newIndex] = newTable[newIndex].put(keyIdentificator, key, keyHash, value, rshift + HASH_BITS);
                } else {
                    newTable[newIndex] = new SingleEntryBucket<K, V>(key, value, keyHash, valueHash(value.hashCode()));
                }
                return new HashBucket<K, V>(newTable, keyCount + 1);
            }
            
            Bucket<K, V> newBucket = bucket.put(keyIdentificator, key, keyHash, value, rshift + HASH_BITS);
            if (newBucket == bucket) {
                return this;
            }
            Bucket<K, V>[] newTable = hashtable.clone();
            newTable[index] = newBucket;
            int newKeyCount = numberOfKeys() - bucket.numberOfKeys() + newBucket.numberOfKeys();
            return new HashBucket<K, V>(newTable, newKeyCount);
        }
        
        @Override
        public Bucket<K, V> putAll(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, Iterable<? extends V> values,
                int rshift) {
            // TODO restructure
            int index = tableIndex(insertKeyHash, rshift);
            Bucket<K, V> bucket = hashtable[index];
            Bucket<K, V> newBucket;
            int oldKeyCount;
            if (bucket == null) {
                newBucket = createSingleKeyBucket(insertKey, insertKeyHash, values);
                oldKeyCount = 0;
            } else {
                if (!isSmall() || bucket.findKey(keyIdentificator, insertKey, insertKeyHash, rshift) != null) {
                    newBucket = bucket.putAll(keyIdentificator, insertKey, insertKeyHash, values, rshift + HASH_BITS);
                    if (newBucket == bucket) return this;
                    oldKeyCount = bucket.numberOfKeys();
                } else {
                    // small and new key
                    int newSize = minSize(insertKeyHash, ((SingleKeyBucket<K, V>)bucket).getKeyHash(), rshift);
                    Bucket<K, V>[] newTable = resizeTable(keyIdentificator, insertKey, insertKeyHash, null, rshift, newSize);
                    int newIndex = tableIndex(newTable.length, insertKeyHash, rshift);
                    bucket = newTable[newIndex];
                    if (bucket == null) {
                        newTable[newIndex] = createSingleKeyBucket(insertKey, insertKeyHash, values);
                    } else {
                        newTable[newIndex] =
                                ObjectUtil.checkNotNull(bucket.putAll(keyIdentificator, insertKey, insertKeyHash, values, rshift + HASH_BITS));
                    }
                    return new HashBucket<K, V>(newTable, keyCount + 1);
                }
            }
            Bucket<K, V>[] newTable = hashtable.clone();
            newTable[index] = newBucket;
            int newKeyCount = numberOfKeys() - oldKeyCount + newBucket.numberOfKeys();
            return new HashBucket<K, V>(newTable, newKeyCount);
        }
        
        private static int minSize(int hash1, int hash2, int rshift) {
            int cross = ((hash1 ^ hash2) >>> rshift) & HASH_MASK;
            int newSize = 2 * Integer.lowestOneBit(cross);
            if (newSize == 0) {
                newSize = HASHTABLE_SIZE;
            }
            return Math.max(8, newSize);
        }
        
        private Bucket<K, V>[] resizeTable(Identificator<? super K> keyIdentificator, K key, int keyHash, V value, int rshift, int newSize) {
            Bucket<K, V>[] newTable = createHashtable(newSize);
            for (int i = 0; i < hashtable.length; ++i) {
                if (hashtable[i] == null) {
                    continue;
                }
                int hash = ((SingleKeyBucket<K, V>)hashtable[i]).getKeyHash();
                int newIndex = tableIndex(newSize, hash, rshift);
                Assert.assertNull(newTable[newIndex], "duplicate table entry");
                newTable[newIndex] = hashtable[i];
            }
            return newTable;
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, int rshift) {
            int index = tableIndex(removeKeyHash, rshift);
            Bucket<K, V> bucket = hashtable[index];
            if (bucket == null) {
                return this;
            }
            Bucket<K, V> newBucket = bucket.remove(identificator, removeKey, removeKeyHash, rshift + HASH_BITS);
            if (newBucket == bucket) {
                return this;
            }
            
            int newKeyCount = keyCount - 1;
            if (newKeyCount == 1) {
                // single key will remain
                if (newBucket != null) {
                    return newBucket;
                }
                for (int i = 0;; ++i) {
                    if (hashtable[i] == null || i == index) {
                        continue;
                    }
                    return hashtable[i];
                }
            }
            
            Bucket<K, V>[] newTable = hashtable.clone();
            newTable[index] = newBucket;
            return new HashBucket<K, V>(newTable, newKeyCount);
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K key, int keyHash, V value, int rshift) {
            int index = tableIndex(keyHash, rshift);
            Bucket<K, V> bucket = hashtable[index];
            if (bucket == null) {
                return this;
            }
            Bucket<K, V> newBucket = bucket.remove(identificator, key, keyHash, value, rshift + HASH_BITS);
            if (newBucket == bucket) {
                return this;
            }
            
            int newKeyCount = numberOfKeys() - bucket.numberOfKeys() + (newBucket == null ? 0 : newBucket.numberOfKeys());
            if (newKeyCount == 1) {
                // single key will remain
                if (newBucket != null) {
                    return newBucket;
                }
                for (int i = 0;; ++i) {
                    if (hashtable[i] == null || i == index) {
                        continue;
                    }
                    return hashtable[i];
                }
            }
            
            Bucket<K, V>[] newTable = hashtable.clone();
            newTable[index] = newBucket;
            return new HashBucket<K, V>(newTable, newKeyCount);
        }
        
        @Override
        public int numberOfKeys() {
            return keyCount;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            int idx;
            while ((idx = iterator.getAndIncrementIndex()) < hashtable.length) {
                if (hashtable[idx] != null) {
                    iterator.push(hashtable[idx]);
                    return hashtable[idx].findNext(iterator);
                }
            }
            return false;
        }
        
    }
    
    /**
     * Handles the case where a multiple keys have exactly the same hash values.
     */
    private static class CollisionBucket<K, V> implements Bucket<K, V> {
        
        private final SingleKeyBucket<K, V>[] children;
        
        private final int keyHash;
        
        public CollisionBucket(int keyHash, SingleKeyBucket<K, V>[] children) {
            Assert.assertTrue(children.length > 1, "Must have at least 2 keys!");
            this.children = children;
            this.keyHash = keyHash;
        }
        
        @Override
        public PersistentSet<V> get(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, int rshift) {
            if (lookupKeyHash != keyHash) {
                return PersistentCollections.setOf();
            }
            // We need to do a linear search through the keys.
            for (int i = 0; i < children.length; ++i) {
                if (identificator.equals(lookupKey, children[i].getKey())) {
                    return children[i];
                }
            }
            return PersistentCollections.setOf();
        }
        
        @Override
        public K findKey(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, int rshift) {
            if (lookupKeyHash != keyHash) {
                return null;
            }
            // We need to do a linear search through the keys.
            for (SingleKeyBucket<K, V> child: children) {
                if (identificator.equals(lookupKey, child.getKey())) {
                    return child.getKey();
                }
            }
            return null;
        }
        
        @Override
        public boolean contains(Identificator<? super K> identificator, K lookupKey, int lookupKeyHash, V lookupValue, int rshift) {
            if (lookupKeyHash != keyHash) {
                return false;
            }
            // We need to do a linear search through the keys.
            for (SingleKeyBucket<K, V> child: children) {
                if (identificator.equals(lookupKey, child.getKey())) {
                    return child.contains(lookupValue);
                }
            }
            return false;
        }
        
        @Override
        public int numberOfKeys() {
            return children.length;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> identificator, K insertKey, int insertKeyHash, V insertValue, int rshift) {
            Assert.assertTrue(keyHash == insertKeyHash, "hash mismatch");
            for (int i = 0; i < children.length; ++i) {
                SingleKeyBucket<K, V> child = children[i];
                if (identificator.equals(insertKey, child.getKey())) {
                    SingleKeyBucket<K, V> newChild = child.putValueForSingleKey(insertValue);
                    if (newChild == child) {
                        // value has already been contained in the set, no change
                        return this;
                    }
                    return replaceChild(i, newChild);
                }
            }
            SingleKeyBucket<K, V>[] newChildren = ArrayUtil.appendArrayElement(children, new SingleEntryBucket<K, V>(insertKey, insertValue,
                    insertKeyHash, valueHash(insertValue.hashCode())));
            return new CollisionBucket<K, V>(keyHash, newChildren);
        }
        
        @Override
        public Bucket<K, V> putAll(Identificator<? super K> keyIdentificator, K insertKey, int insertKeyHash, Iterable<? extends V> values,
                int rshift) {
            // TODO optimize
            Bucket<K, V> newBucket = this;
            for (V value: values) {
                newBucket = newBucket.put(keyIdentificator, insertKey, insertKeyHash, value, rshift);
            }
            return newBucket;
        }
        
        /**
         * Creates a new bucket with the child at {@code index} replaced by
         * {@code newChild}.  
         */
        private Bucket<K, V> replaceChild(int index, SingleKeyBucket<K, V> newValueSet) {
            SingleKeyBucket<K, V>[] newValueSets = ArrayUtil.replaceArrayElement(children, index, newValueSet);
            return new CollisionBucket<K, V>(keyHash, newValueSets);
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, int rshift) {
            if (keyHash != removeKeyHash) {
                return this;
            }
            for (int i = 0; i < children.length; ++i) {
                SingleKeyBucket<K, V> child = children[i];
                if (identificator.equals(removeKey, child.getKey())) {
                    return removeChild(i);
                }
            }
            return this;
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K removeKey, int removeKeyHash, V removeValue, int rshift) {
            if (keyHash != removeKeyHash) {
                return this;
            }
            for (int i = 0; i < children.length; ++i) {
                SingleKeyBucket<K, V> child = children[i];
                if (identificator.equals(removeKey, child.getKey())) {
                    SingleKeyBucket<K, V> newChild = child.removeValueForSingleKey(removeValue);
                    if (newChild == child) {
                        // value has not been contained in the set, no change 
                        return this;
                    }
                    if (newChild == null) {
                        return removeChild(i);
                    }
                    return replaceChild(i, newChild);
                }
            }
            return this;
        }
        
        private Bucket<K, V> removeChild(int index) {
            // As there are always at least two children, after removing one, at least one remains.
            if (children.length == 2) {
                int remainingIndex = 1 - index;
                return children[remainingIndex];
            }
            SingleKeyBucket<K, V>[] newValueSets = ArrayUtil.removeArrayElement(children, index);
            return new CollisionBucket<K, V>(keyHash, newValueSets);
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            int idx = iterator.getAndIncrementIndex();
            if (idx < numberOfKeys()) {
                iterator.push(children[idx]);
                return children[idx].findNext(iterator);
            }
            return false;
        }
        
    }
    
    @SafeVarargs
    private static <X> X[] arrayOf(X... elements) {
        return elements;
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> Bucket<K, V> emptyBucket() {
        return (Bucket<K, V>)EMPTY_BUCKET;
    }
    
    private final Bucket<K, V> root;
    
    private final Identificator<? super K> keyIdentificator;
    
    protected PersistentMultiMapImpl2() {
        this(CollectionUtil.getObjectIdentificator(), PersistentMultiMapImpl2.<K, V>emptyBucket());
    }
    
    protected PersistentMultiMapImpl2(Identificator<? super K> identificator) {
        this(identificator, PersistentMultiMapImpl2.<K, V>emptyBucket());
    }
    
    protected PersistentMultiMapImpl2(Identificator<? super K> keyIdentificator, Bucket<K, V> root) {
        PreConditions.paramNotNull(root);
        this.keyIdentificator = keyIdentificator;
        this.root = root;
    }
    
    @Override
    public boolean isEmpty() {
        return root.numberOfKeys() == 0;
    }
    
    @Override
    public boolean containsKey(K key) {
        return root.findKey(keyIdentificator, key, keyIdentificator.hashCode(key), 0) != null;
    }
    
    @Override
    public boolean contains(K key, V value) {
        return root.contains(keyIdentificator, key, keyIdentificator.hashCode(key), value, 0);
    }
    
    @Override
    @Nonnull
    public PersistentSet<V> get(K key) {
        return root.get(keyIdentificator, key, keyIdentificator.hashCode(key), 0);
    }

    @Override
    public PersistentMultiMapImpl2<K, V> cleared() {
        return empty();
    }
    
    @Override
    public PersistentMultiMapImpl2<K, V> with(K key, V value) {
        PreConditions.paramNotNull(key);
        PreConditions.paramNotNull(value);
        Bucket<K, V> newRoot = root.put(keyIdentificator, key, keyIdentificator.hashCode(key), value, 0);
        if (newRoot == root) return this;
        return new PersistentMultiMapImpl2<K, V>(keyIdentificator, newRoot);
    }
    
    @Override
    public PersistentMultiMapImpl2<K, V> without(K key, V value) {
        Bucket<K, V> newRoot = root.remove(keyIdentificator, key, keyIdentificator.hashCode(key), value, 0);
        if (newRoot == root) {
            return this;
        }
        if (newRoot == null) {
            return empty();
        }
        return new PersistentMultiMapImpl2<K, V>(keyIdentificator, newRoot);
    }
    
    private static class MapEntryIterator<K, V> implements EntryIterator<K, Set<V>> {
        
        /**
         * Stack of buckets which marks the current path of the iteration in the
         * tree.
         */
        private final Deque<Bucket<K, V>> bucketStack = new ArrayDeque<Bucket<K, V>>(MAX_HEIGHT);
        
        /**
         * For each entry on the stack there is an index counter available.
         */
        private final int[] pos = new int[MAX_HEIGHT];
        
        @CheckForNull
        private K currentKey = null;
        
        @CheckForNull
        private PersistentSet<V> currentValue = null;
        
        public MapEntryIterator(Bucket<K, V> root) {
            bucketStack.push(root);
        }
        
        public int getAndIncrementIndex() {
            int top = bucketStack.size() - 1;
            return pos[top]++;
        }
        
        public void push(Bucket<K, V> bucket) {
            pos[bucketStack.size()] = 0;
            bucketStack.push(bucket);
        }
        
        public void setCurrent(K key, PersistentSet<V> value) {
            currentKey = key;
            currentValue = value;
        }
        
        @Override
        public boolean next() {
            if (bucketStack.isEmpty()) return false;
            if (findNext()) {
                return true;
            }
            currentKey = null;
            currentValue = null;
            return false;
        }
        
        @Override
        public K getCurrentKey() {
            if (currentKey == null) throw new NoSuchElementException();
            return currentKey;
        }
        
        @Override
        @CheckForNull
        public PersistentSet<V> getCurrentValue() {
            if (currentKey == null) throw new NoSuchElementException();
            return currentValue;
        }
        
        private boolean findNext() {
            while (!bucketStack.isEmpty()) {
                Bucket<K, V> top = bucketStack.peek();
                if (top.findNext(this)) {
                    return true;
                }
                bucketStack.pop();
            }
            return false;
        }
        
    }
    
    public int numberOfValues(K key) {
        return get(key).size(); // TODO optimize
    }
    
    public int keyCount() {
        return root.numberOfKeys();
    }

    @Override
    public EntryIterator<K, Set<V>> entryIterator() {
        return new MapEntryIterator<K, V>(root);
    }
    
    @Override
    public PersistentMultiMapImpl2<K, V> withAll(K key, Iterable<? extends V> values) {
        Iterator<? extends V> iterator = values.iterator();
        if (!iterator.hasNext()) {
            return this;
        }
        V value = iterator.next();
        if (!iterator.hasNext()) {
            return with(key, value);
        }
        Bucket<K, V> newRoot = root;
        int keyHash = keyIdentificator.hashCode(key);
        newRoot = newRoot.putAll(keyIdentificator, key, keyHash, values, 0);
        if (newRoot == root) return this;
        return new PersistentMultiMapImpl2<K, V>(keyIdentificator, newRoot);
    }
    
    @Override
    public PersistentMultiMap<K, V> without(K key) {
        int keyHash = keyIdentificator.hashCode(key);
        Bucket<K, V> newRoot = root.remove(keyIdentificator, key, keyHash, 0);
        if (newRoot == root) {
            return this;
        }
        if (newRoot == null) {
            return empty();
        }
        return new PersistentMultiMapImpl2<K, V>(keyIdentificator, newRoot);
    }
    
    @Override
    public PersistentMultiMap<K, V> withoutAll(K key, Collection<? extends V> values) {
        if (values.isEmpty()) {
            return this;
        }
        Bucket<K, V> newRoot = root;
        int keyHash = keyIdentificator.hashCode(key);
        for (V value: values) {
            newRoot = newRoot.remove(keyIdentificator, key, keyHash, value, 0);
            if (newRoot == null) {
                return empty();
            }
        }
        if (newRoot == root) return this;
        return new PersistentMultiMapImpl2<K, V>(keyIdentificator, newRoot);
    }
    
    private class KeySet extends AbstractKeySet<K> implements ImmutableSet<K> {
        
        KeySet(PersistentMultiMap<K, ?> map) {
            super(map);
        }
        
        @Override
        @CheckForNull
        public K findEqualOrNull(K element) {
            return root.findKey(keyIdentificator, element, keyIdentificator.hashCode(element), 0);
        }
        
    }
    
    @Override
    public ImmutableSet<K> keys() {
        return new KeySet(this);
    }
    
    private class Values implements Collection<Set<V>> {

        @Override
        public Iterator<Set<V>> iterator() {
            return new EntryValueIterator<>(entryIterator());
        }

        @Override
        public int size() {
            return keyCount();
        }

        @Override
        public Set<V> getFirstOrNull() {
            // TODO optimize 
            if (isEmpty()) {
                return null;
            }
            return CollectionUtil.firstElement(this);
        }
    }

    @Override
    public Collection<Set<V>> values() {
        return new Values();
    }
    
    @CheckForNull
    public K getFirstKeyOrNull() {
        // TODO optimize
        EntryIterator<K, ?> iterator = entryIterator();
        if (!iterator.next()) {
            return null;
        }
        return iterator.getCurrentKey();
    }
    
    @Override
    public final Identificator<? super K> getKeyIdentificator() {
        return keyIdentificator;
    }
    
    @Override
    // TODO allow customization of value identificator
    public final Identificator<? super Set<V>> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
}
