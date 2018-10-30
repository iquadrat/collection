package org.povworld.collection.persistent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.common.AbstractKeySet;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.EntryValueIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableCollection;

/**
 * Implementation of a persistent map that uses the key's hash value to find it's position.
 */
// TODO Use same principle as in {@link PersistentHashSet}.
@Immutable
public class PersistentHashMap<K, V> extends AbstractMap<K, V> implements PersistentMap<K, V> {
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentHashMap<K, V> empty() {
        return (PersistentHashMap<K, V>)EMPTY_MAP;
    }
    
    public static <K, V> PersistentHashMap<K, V> empty(
            final Identificator<? super K> keyIdentificator,
            final Identificator<? super V> valueIdentificator) {
        if (keyIdentificator.equals(EMPTY_MAP.getKeyIdentificator()) &&
                valueIdentificator.equals(EMPTY_MAP.getValueIdentificator())) {
            return empty();
        }
        Bucket<K, V> root = emptyBucket();
        return new CustomIdentificatorsHashMap<K, V>(keyIdentificator, valueIdentificator, root);
    }
    
    private static class CustomIdentificatorsHashMap<K, V> extends PersistentHashMap<K, V> {
        private final Identificator<? super K> keyIdentificator;
        private final Identificator<? super V> valueIdentificator;
        
        private CustomIdentificatorsHashMap(
                Identificator<? super K> keyIdentificator,
                Identificator<? super V> valueIdentificator,
                Bucket<K, V> root) {
            super(root);
            this.keyIdentificator = keyIdentificator;
            this.valueIdentificator = valueIdentificator;
        }
        
        @Override
        public Identificator<? super K> getKeyIdentificator() {
            return keyIdentificator;
        }
        
        @Override
        public Identificator<? super V> getValueIdentificator() {
            return valueIdentificator;
        }
        
        @Override
        protected PersistentHashMap<K, V> create(Bucket<K, V> root) {
            return new CustomIdentificatorsHashMap<>(keyIdentificator, valueIdentificator, root);
        }
    }
    
    public static <K, V> PersistentMap<K, V> copyOf(Map<K, V> map) {
        if (map instanceof PersistentHashMap) {
            return (PersistentHashMap<K, V>)map;
        }
        return PersistentHashMap.<K, V>empty().withAll(map);
    }
    
    private static final int HASH_BITS = 6;
    
    private static final int HASH_TABLE_SIZE = 1 << HASH_BITS;
    
    private static final int HASH_MASK = HASH_TABLE_SIZE - 1;
    
    private static final int COMPACT_BUCKET_SPLIT_SIZE = 48;
    
    private static final int MAX_HEIGHT = ((32 + HASH_BITS - 1) / HASH_BITS) + 1;
    
    // NOTE: empty bucket must be created before empty map!
    private static final EmptyBucket<Object, Object> EMPTY_BUCKET = new EmptyBucket<Object, Object>();
    
    private static final EntryIterator<Object, Object> EMPTY_ITERATOR = new MapEntryIterator<Object, Object>(EMPTY_BUCKET);
    
    private static final PersistentHashMap<Object, Object> EMPTY_MAP;
    
    static {
        EMPTY_MAP = new PersistentHashMap<Object, Object>(emptyBucket()) {
            
            @Override
            public org.povworld.collection.EntryIterator<Object, Object> entryIterator() {
                return EMPTY_ITERATOR;
            }
            
        };
    }
    
    @Immutable
    protected static interface Bucket<K, V> {
        
        @CheckForNull
        public V get(Identificator<? super K> identificator, K key, int hashvalue, int rshift);
        
        @CheckForNull
        public K findKey(Identificator<? super K> identificator, K key, int hashvalue, int rshift);
        
        public int size();
        
        /**
         * Insert a key/value pair into the bucket.
         * @param hashvalue the key's hash code
         * @param rshift the bucket's right shift
         * @return an updated bucket or the same bucket when the same key/value pair was already contained
         */
        public Bucket<K, V> put(
                Identificator<? super K> keyIdentificator,
                Identificator<? super V> valueIdentificator,
                K key, int hashvalue, V value, int rshift);
        
        /**
         * NOTE: Returns {@code null} iff the new bucket would be empty!
         */
        @CheckForNull
        public Bucket<K, V> remove(
                Identificator<? super K> keyIdentificator,
                K key,
                int hashvalue, int rshift);
        
        public boolean findNext(MapEntryIterator<K, V> iterator);
        
        /**
         * Returns the first key found in the bucket or {@code null}
         * if the bucket is empty.
         */
        public K getFirstKeyOrNull();
        
    }
    
    private static class EmptyBucket<K, V> implements Bucket<K, V> {
        
        @Override
        public K findKey(Identificator<? super K> comparator, K key, int hashvalue, int rshift) {
            return null;
        }
        
        @Override
        @CheckForNull
        public V get(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            return null;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, K key, int hashvalue, V value,
                int rshift) {
            return new SingleEntryBucket<K, V>(key, hashvalue, value);
        }
        
        @Override
        public Bucket<K, V> remove(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            return this;
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            return false;
        }
        
        @Override
        @CheckForNull
        public K getFirstKeyOrNull() {
            return null;
        }
    }
    
    private static class SingleEntryBucket<K, V> implements Bucket<K, V> {
        
        private final K key;
        
        private final int hashValue;
        
        private final V value;
        
        public SingleEntryBucket(K key, int hashvalue, V value) {
            this.key = key;
            this.hashValue = hashvalue;
            this.value = value;
        }
        
        @Override
        public K findKey(Identificator<? super K> comparator, K key, int hashvalue, int rshift) {
            if ((hashValue == hashvalue) && comparator.equals(key, this.key)) {
                return this.key;
            }
            return null;
        }
        
        @Override
        public V get(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            if (findKey(identificator, key, hashvalue, rshift) == null) {
                return null;
            }
            return value;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, K key, int hashvalue, V value,
                int rshift) {
            if (findKey(keyIdentificator, key, hashvalue, rshift) != null) {
                if (valueIdentificator.equals(this.value, value)) {
                    return this;
                }
                return new SingleEntryBucket<K, V>(key, hashvalue, value);
            }
            if (hashvalue < this.hashValue) {
                return new LeafBucket<K, V>(arrayOf(key, this.key), arrayOf(value, this.value), new int[] {hashvalue, this.hashValue});
            } else {
                return new LeafBucket<K, V>(arrayOf(this.key, key), arrayOf(this.value, value), new int[] {this.hashValue, hashvalue});
            }
        }
        
        @Override
        public Bucket<K, V> remove(Identificator<? super K> keyIdentificator, K key, int hashvalue, int rshift) {
            if (findKey(keyIdentificator, key, hashvalue, rshift) == null) {
                return this;
            }
            return null;
        }
        
        @Override
        public int size() {
            return 1;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            if (iterator.getAndIncrementIndex() == 0) {
                iterator.setCurrent(key, value);
                return true;
            }
            return false;
        }
        
        @Override
        public K getFirstKeyOrNull() {
            return key;
        }
        
    }
    
    private static class LeafBucket<K, V> implements Bucket<K, V> {
        
        // TODO replace by single array for key+values 
        private final K[] keys;
        
        private final V[] values;
        
        private final int[] hashValues;
        
        /**
         * NOTE: keys and values must be sorted according to their hash values!
         *
         * @param keys
         * @param values
         * @param hashValues
         */
        LeafBucket(K[] keys, V[] values, int[] hashValues) {
            this.keys = keys;
            this.values = values;
            this.hashValues = hashValues;
        }
        
        @Override
        public K findKey(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            int index = indexOf(identificator, key, hashvalue);
            return (index < 0) ? null : keys[index];
        }
        
        @Override
        @CheckForNull
        public V get(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            int index = indexOf(identificator, key, hashvalue);
            if (index < 0) return null;
            return values[index];
        }
        
        /**
         * @return the index of the key or {@code -insertionIndex} if the key is not present
         */
        private int indexOf(Identificator<? super K> identificator, K key, int hashvalue) {
            int size = size();
            // Do an interpolation step.
            int index = (int)(size * ((long)hashvalue - Integer.MIN_VALUE) >>> 32);
            
            // Now search linear.
            if (hashValues[index] >= hashvalue) {
                while (index > 0 && hashValues[index - 1] >= hashvalue) {
                    index--;
                }
                if (index == 0 && hashValues[0] != hashvalue) {
                    return -1;
                }
            } else {
                while (hashValues[index] < hashvalue) {
                    index++;
                    if (index == size) {
                        return -size - 1;
                    }
                }
            }
            while ((hashValues[index] == hashvalue)) {
                if (identificator.equals(key, keys[index])) {
                    return index;
                }
                index++;
                if (index == size) {
                    break;
                }
            }
            return -index - 1;
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, K key, int hashvalue, V value,
                int rshift) {
            int index = indexOf(keyIdentificator, key, hashvalue);
            
            if (index >= 0) {
                // Compare with == not with equals as required by PersistentMap.put.
                if (values[index] == value) return this;
                // The key is already contained in the map, only value array needs to be changed.
                V[] newValues = Arrays.copyOf(values, values.length);
                newValues[index] = value;
                return new LeafBucket<K, V>(keys, newValues, hashValues);
            }
            
            // The key is not yet contained in the map.
            index = -index - 1;
            int newSize = keys.length + 1;
            if ((newSize > COMPACT_BUCKET_SPLIT_SIZE) && (rshift < 32)) {
                Bucket<K, V>[] hashtable = buildHashtable(keyIdentificator, valueIdentificator, key, hashvalue, value, rshift);
                return new HashBucket<K, V>(hashtable, newSize);
            }
            K[] newKeys = Arrays.copyOf(keys, newSize);
            int[] newHashValues = Arrays.copyOf(hashValues, newSize);
            V[] newValues = Arrays.copyOf(values, newSize);
            
            for (int i = newSize - 1; i > index; --i) {
                newKeys[i] = newKeys[i - 1];
                newValues[i] = newValues[i - 1];
                newHashValues[i] = newHashValues[i - 1];
            }
            
            newKeys[index] = key;
            newHashValues[index] = hashvalue;
            newValues[index] = value;
            return new LeafBucket<K, V>(newKeys, newValues, newHashValues);
        }
        
        private Bucket<K, V>[] buildHashtable(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, K key,
                int hashvalue, V value, int rshift) {
            // Compute the table index for each entry and build an array of key indices for each table index. 
            byte[] hits = new byte[HASH_TABLE_SIZE];
            byte[][] table = new byte[HASH_TABLE_SIZE][];
            
            for (int i = 0; i < size(); ++i) {
                int idx = tableIndex(hashValues[i], rshift);
                if (table[idx] == null) {
                    table[idx] = new byte[6];
                } else if (hits[idx] == table[idx].length) {
                    table[idx] = Arrays.copyOf(table[idx], hits[idx] + 6);
                }
                table[idx][hits[idx]] = (byte)i;
                hits[idx]++;
            }
            
            // Convert the pre-computed table into a real hash table with child buckets.
            Bucket<K, V>[] hashtable = createHashtable();
            for (int i = 0; i < HASH_TABLE_SIZE; ++i) {
                int entries = hits[i];
                if (entries == 0) {
                    continue;
                }
                if (entries == 1) {
                    int j = table[i][0];
                    hashtable[i] = new SingleEntryBucket<K, V>(keys[j], hashValues[j], values[j]);
                } else {
                    K[] indexKeys = createArray(entries);
                    V[] indexValues = createArray(entries);
                    int[] indexHashes = new int[entries];
                    for (int j = 0; j < entries; ++j) {
                        int k = table[i][j];
                        indexKeys[j] = keys[k];
                        indexValues[j] = values[k];
                        indexHashes[j] = hashValues[k];
                    }
                    hashtable[i] = new LeafBucket<K, V>(indexKeys, indexValues, indexHashes);
                }
            }
            
            putEntry(keyIdentificator, valueIdentificator, hashtable, key, hashvalue, value, rshift);
            return hashtable;
        }
        
        private void putEntry(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, Bucket<K, V>[] hashtable, K key,
                int hashvalue, V value, int rshift) {
            int tableIdx = tableIndex(hashvalue, rshift);
            if (hashtable[tableIdx] == null) {
                hashtable[tableIdx] = new SingleEntryBucket<K, V>(key, hashvalue, value);
            } else {
                hashtable[tableIdx] = hashtable[tableIdx].put(keyIdentificator, valueIdentificator, key, hashvalue, value, rshift + HASH_BITS);
            }
        }
        
        @Override
        @CheckForNull
        public Bucket<K, V> remove(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            int index = indexOf(identificator, key, hashvalue);
            if (index < 0) {
                // key not present
                return this;
            }
            
            int newSize = keys.length - 1;
            if (newSize == 0) {
                return null;
            }
            
            // TODO could return SingleEntryBucket if there is only one key left
            
            K[] newKeys = createArray(newSize);
            V[] newValues = createArray(newSize);
            int[] newHashValues = new int[newSize];
            
            for (int i = 0; i < index; ++i) {
                newKeys[i] = keys[i];
                newValues[i] = values[i];
                newHashValues[i] = hashValues[i];
            }
            for (int i = index + 1; i < keys.length; ++i) {
                newKeys[i - 1] = keys[i];
                newValues[i - 1] = values[i];
                newHashValues[i - 1] = hashValues[i];
            }
            return new LeafBucket<K, V>(newKeys, newValues, newHashValues);
        }
        
        @Override
        public int size() {
            return keys.length;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            int index = iterator.getAndIncrementIndex();
            if (index >= keys.length) return false;
            iterator.setCurrent(keys[index], values[index]);
            return true;
        }
        
        @Override
        public K getFirstKeyOrNull() {
            return keys[0];
        }
        
    }
    
    private static class HashBucket<K, V> implements Bucket<K, V> {
        
        private final Bucket<K, V>[] hashtable;
        
        private final int size;
        
        HashBucket(Bucket<K, V>[] hashtable, int size) {
            Assert.assertTrue(hashtable.length == HASH_TABLE_SIZE, "Invalid array length!");
            this.hashtable = hashtable;
            this.size = size;
        }
        
        private Bucket<K, V> bucketOf(int hashvalue, int rshift) {
            return hashtable[tableIndex(hashvalue, rshift)];
        }
        
        @Override
        public K findKey(Identificator<? super K> comparator, K key, int hashvalue, int rshift) {
            Bucket<K, V> bucket = bucketOf(hashvalue, rshift);
            if (bucket == null) {
                return null;
            }
            return bucket.findKey(comparator, key, hashvalue, rshift + HASH_BITS);
        }
        
        @Override
        @CheckForNull
        public V get(Identificator<? super K> identificator, K key, int hashvalue, int rshift) {
            Bucket<K, V> bucket = bucketOf(hashvalue, rshift);
            if (bucket == null) {
                return null;
            }
            return bucket.get(identificator, key, hashvalue, rshift + HASH_BITS);
        }
        
        @Override
        public Bucket<K, V> put(Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator, K key, int hashvalue, V value,
                int rshift) {
            int index = tableIndex(hashvalue, rshift);
            Bucket<K, V> bucket = hashtable[index];
            if (bucket != null) {
                Bucket<K, V> newBucket = bucket.put(keyIdentificator, valueIdentificator, key, hashvalue, value, rshift + HASH_BITS);
                if (newBucket == bucket) {
                    return this;
                }
                Bucket<K, V>[] newTable = hashtable.clone();
                newTable[index] = newBucket;
                return new HashBucket<K, V>(newTable, size - bucket.size() + newBucket.size());
            } else {
                Bucket<K, V>[] newTable = hashtable.clone();
                newTable[index] = new SingleEntryBucket<K, V>(key, hashvalue, value);
                return new HashBucket<K, V>(newTable, size + 1);
            }
        }
        
        @Override
        public Bucket<K, V> remove(Identificator<? super K> keyIdentificator, K key, int hashvalue, int rshift) {
            int index = tableIndex(hashvalue, rshift);
            Bucket<K, V> bucket = hashtable[index];
            if (bucket == null) {
                return this;
            }
            Bucket<K, V> newBucket = bucket.remove(keyIdentificator, key, hashvalue, rshift + HASH_BITS);
            if (newBucket == bucket) {
                return this;
            }
            int newSize = (newBucket == null) ? 0 : newBucket.size();
            Bucket<K, V>[] newTable = hashtable.clone();
            newTable[index] = newBucket;
            int newTotalSize = size - bucket.size() + newSize; // TODO shouldn't this always be fSize-1?
            // TODO implement collapsing of bucket if newsize is < some size
            return new HashBucket<K, V>(newTable, newTotalSize);
        }
        
        @Override
        public int size() {
            return size;
        }
        
        @Override
        public boolean findNext(MapEntryIterator<K, V> iterator) {
            int idx;
            while ((idx = iterator.getAndIncrementIndex()) < HASH_TABLE_SIZE) {
                if (hashtable[idx] != null) {
                    iterator.push(hashtable[idx]);
                    return hashtable[idx].findNext(iterator);
                }
            }
            return false;
        }
        
        @Override
        public K getFirstKeyOrNull() {
            for (int i = 0; i < HASH_TABLE_SIZE; ++i) {
                if (hashtable[i] != null) {
                    return hashtable[i].getFirstKeyOrNull();
                }
            }
            throw Assert.fail(HashBucket.class.getSimpleName() + " is empty");
        }
        
    }
    
    protected static class MapEntryIterator<K, V> implements EntryIterator<K, V> {
        
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
        private V currentValue = null;
        
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
        
        public void setCurrent(K key, V value) {
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
        public V getCurrentValue() {
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
    
    private static class Values<V> implements ImmutableCollection<V> {
        
        private final PersistentHashMap<?, V> map;
        
        public Values(PersistentHashMap<?, V> map) {
            this.map = map;
        }
        
        @Override
        public int size() {
            return map.keyCount();
        }
        
        @Override
        public Iterator<V> iterator() {
            return new EntryValueIterator<V>(map.entryIterator());
        }
        
        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }
        
        @Override
        public V getFirstOrNull() {
            // TODO make getFirstValue on map..
            if (isEmpty()) {
                return null;
            }
            EntryIterator<?, V> iterator = map.entryIterator();
            iterator.next();
            return iterator.getCurrentValue();
        }
        
    }
    
    private static class KeySet<E> extends AbstractKeySet<E> implements PersistentSet<E> {
        
        private static final Object PRESENT = new Object();
        
        protected final PersistentHashMap<E, Object> map;
        
        protected KeySet(PersistentHashMap<E, Object> map) {
            super(map);
            this.map = map;
        }
        
        @Override
        public PersistentSet<E> with(E element) {
            if (map.containsKey(element)) return this;
            PersistentHashMap<E, Object> newMap = map.with(element, PRESENT);
            return new KeySet<E>(newMap);
        }
        
        @Override
        public PersistentSet<E> cleared() {
            if (isEmpty()) return this;
            return PersistentHashSet.empty();
        }
        
        @Override
        public E findEqualOrNull(E key) {
            Identificator<? super E> keyIdentificator = map.getKeyIdentificator();
            return map.root.findKey(keyIdentificator, key, keyIdentificator.hashCode(key), 0);
        }
        
        @Override
        public PersistentSet<E> without(E element) {
            PreConditions.paramNotNull(element);
            PersistentHashMap<E, Object> newMap = map.without(element);
            if (map == newMap) return this;
            return new KeySet<E>(newMap);
        }
        
        @Override
        public PersistentSet<E> withoutAll(Iterable<? extends E> elements) {
            PersistentHashMap<E, Object> result = map;
            for (E element: elements) {
                PreConditions.paramNotNull(element);
                result = result.without(element);
            }
            if (result == map) return this;
            return new KeySet<E>(result);
        }
        
        @Override
        public PersistentSet<E> withAll(Iterable<? extends E> elements) {
            PersistentHashMap<E, Object> result = map;
            for (E element: elements) {
                PreConditions.paramNotNull(element);
                result = result.with(element, PRESENT);
            }
            if (result == map) {
                return this;
            }
            return new KeySet<E>(result);
        }
        
        @Override
        public E getFirstOrNull() {
            return map.getFirstKeyOrNull();
        }
        
    }
    
    @SafeVarargs
    private static <X> X[] arrayOf(X... elements) {
        return elements;
    }
    
    @SuppressWarnings("unchecked")
    private static <K> K[] createArray(int size) {
        return (K[])new Object[size];
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> Bucket<K, V> emptyBucket() {
        return (Bucket<K, V>)EMPTY_BUCKET;
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> Bucket<K, V>[] createHashtable() {
        return new Bucket[HASH_TABLE_SIZE];
    }
    
    private static int tableIndex(int hashvalue, int rshift) {
        return (hashvalue >>> rshift) & HASH_MASK;
    }
    
    protected final Bucket<K, V> root;
    
    protected PersistentHashMap(Bucket<K, V> root) {
        this.root = root;
    }
    
    protected PersistentHashMap<K, V> create(Bucket<K, V> root) {
        return new PersistentHashMap<>(root);
    }
    
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    public Identificator<? super V> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    public int keyCount() {
        return root.size();
    }
    
    @Override
    public boolean containsKey(K key) {
        Identificator<? super K> keyIdentificator = getKeyIdentificator();
        return root.findKey(keyIdentificator, key, keyIdentificator.hashCode(key), 0) != null;
    }
    
    @Override
    @CheckForNull
    public V get(K key) {
        Identificator<? super K> keyIdentificator = getKeyIdentificator();
        return root.get(keyIdentificator, key, keyIdentificator.hashCode(key), 0);
    }
    
    @Override
    public PersistentHashMap<K, V> cleared() {
        if (isEmpty()) {
            return this;
        }
        Bucket<K, V> root = emptyBucket();
        return create(root);
    }
    
    @Override
    public PersistentHashMap<K, V> with(K key, V value) {
        PreConditions.paramNotNull(key);
        PreConditions.paramNotNull(value);
        final Identificator<? super K> keyIdentificator = getKeyIdentificator();
        Bucket<K, V> newRoot = root.put(keyIdentificator, getValueIdentificator(), key, keyIdentificator.hashCode(key), value, 0);
        if (newRoot == root) {
            return this;
        }
        return create(newRoot);
    }
    
    @Override
    public PersistentHashMap<K, V> withAll(Map<? extends K, ? extends V> map) {
        final Identificator<? super K> keyIdentificator = getKeyIdentificator();
        final Identificator<? super V> valueIdentificator = getValueIdentificator();
        Bucket<K, V> newRoot = root;
        EntryIterator<? extends K, ? extends V> iter = map.entryIterator();
        while (iter.next()) {
            K key = iter.getCurrentKey();
            V value = iter.getCurrentValue();
            newRoot = newRoot.put(keyIdentificator, valueIdentificator, key, keyIdentificator.hashCode(key), value, 0);
        }
        if (newRoot == root) {
            return this;
        }
        return create(newRoot);
    }
    
    @Override
    public PersistentHashMap<K, V> without(K key) {
        final Identificator<? super K> keyIdentificator = getKeyIdentificator();
        Bucket<K, V> newRoot = root.remove(keyIdentificator, key, keyIdentificator.hashCode(key), 0);
        if (newRoot == root) {
            return this;
        }
        if (newRoot == null) {
            newRoot = emptyBucket();
        }
        return create(newRoot);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public PersistentSet<K> keys() {
        return new KeySet<K>((PersistentHashMap<K, Object>)this);
    }
    
    @Override
    public ImmutableCollection<V> values() {
        return new Values<V>(this);
    }
    
    @Override
    public EntryIterator<K, V> entryIterator() {
        return new MapEntryIterator<K, V>(root);
    }
    
    @CheckForNull
    public K getFirstKeyOrNull() {
        return root.getFirstKeyOrNull();
    }
    
}
