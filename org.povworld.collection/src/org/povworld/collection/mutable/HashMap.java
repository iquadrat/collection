package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractKeySet;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.EntryValueIterator;
import org.povworld.collection.common.PreConditions;

/**
 * Map implementations that uses the key's hash value to find elements.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
@NotThreadSafe
public class HashMap<K, V> extends AbstractMap<K, V> {
    
    private class HashMapIterator implements EntryIterator<K, V> {
        
        private int current = -1;
        
        private void findNextElement() {
            if (current == keys.length) return;
            current = nextKeyIndex(current);
        }
        
        @Override
        public K getCurrentKey() throws NoSuchElementException {
            if (current == -1 || current == keys.length) throw new NoSuchElementException();
            return keys[current];
        }
        
        @Override
        public V getCurrentValue() throws NoSuchElementException {
            if (current == -1 || current == keys.length) throw new NoSuchElementException();
            return get(current);
        }
        
        @Override
        public boolean next() {
            findNextElement();
            return current < keys.length;
        }
        
    }
    
    private static final int FULL_SIZE = 2;
    
    private K keys[];
    
    private V values[];
    
    private int size = 0;
    
    private int mask;
    
    public HashMap() {
        keys = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        values = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        mask = 0;
    }
    
    public HashMap(int initialCapacity) {
        this();
        // TODO initialize larger arrays
    }
    
    public HashMap(Map<K, V> map) {
        this(map.keyCount());
        putAll(map);
    }
    
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    public Identificator<? super V> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    private int keyHash(K key) {
        PreConditions.paramNotNull(key);
        return getKeyIdentificator().hashCode(key);
    }
    
    private boolean keyEquals(K key1, K key2) {
        PreConditions.paramNotNull(key1);
        PreConditions.paramNotNull(key2);
        return getKeyIdentificator().equals(key1, key2);
    }
    
    private int findElementByHash(K element, int hash) {
        int idx = hash & mask;
        for (;;) {
            if (keys[idx] == null) return -1;
            if (keyEquals(element, keys[idx])) return idx;
            idx = increment(idx);
        }
    }
    
    private int findElementFullSearch(K element) {
        for (int idx = 0; idx < FULL_SIZE; ++idx) {
            if (keys[idx] != null && keyEquals(element, keys[idx])) {
                return idx;
            }
        }
        return -1;
    }
    
    private int findElement(K element, int hash) {
        if (size <= FULL_SIZE) {
            return findElementFullSearch(element);
        }
        return findElementByHash(element, hash);
    }
    
    private int increment(int idx) {
        return (idx + 1) & mask;
    }
    
    public V put(K key, V value) {
        PreConditions.paramNotNull(value);
        int hash = keyHash(key);
        int idx = findElement(key, hash);
        V oldElement = null;
        if (idx != -1) {
            oldElement = get(idx);
            keys[idx] = key;
            values[idx] = value;
        } else {
            
            ensureCapacityFor(size + 1);
            if (size < FULL_SIZE) {
                keys[size] = key;
                values[size] = value;
            } else {
                internalInsert(key, value, hash);
            }
            size++;
        }
        
        return oldElement;
    }
    
    public void putAll(Map<K, V> map) {
        EntryIterator<K, ? extends V> iterator = map.entryIterator();
        while (iterator.next()) {
            put(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
    }
    
    @Override
    @CheckForNull
    public V get(K key) {
        int idx;
        if (size <= FULL_SIZE) {
            idx = findElementFullSearch(key);
        } else {
            int hash = keyHash(key);
            idx = findElementByHash(key, hash);
        }
        return (idx == -1) ? null : get(idx);
    }
    
    @Override
    public boolean containsKey(K key) {
        int idx;
        if (size <= FULL_SIZE) {
            idx = findElementFullSearch(key);
        } else {
            int hash = keyHash(key);
            idx = findElementByHash(key, hash);
        }
        return idx != -1;
    }
    
    @CheckForNull
    public K findEqualKeyOrNull(K key) {
        int index;
        if (size <= FULL_SIZE) {
            index = findElementFullSearch(key);
        } else {
            int hash = keyHash(key);
            index = findElementByHash(key, hash);
        }
        return index == -1 ? null : keys[index];
    }
    
    private V get(int idx) {
        return values[idx];
    }
    
    /**
     * Removes the {@code key} from the map.
     * @return the value previously associated with the key or {@code null} if the key was not present
     */
    @CheckForNull
    public V remove(K key) {
        int idx;
        if (size <= FULL_SIZE) {
            idx = findElementFullSearch(key);
        } else {
            int hash = keyHash(key);
            idx = findElementByHash(key, hash);
        }
        
        if (idx == -1) {
            return null;
        }
        
        V oldValue = get(idx);
        
        keys[idx] = null;
        values[idx] = null;
        plugHole(idx);
        size--;
        checkEmptySpace();
        return oldValue;
    }
    
    /**
     * Removes all mappings.
     */
    public void clear() {
        keys = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        values = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        mask = 0;
        size = 0;
    }
    
    private void plugHole(int holeIdx) {
        if (size <= FULL_SIZE) {
            for (int idx = holeIdx; idx < FULL_SIZE - 1; ++idx) {
                keys[idx] = keys[idx + 1];
                values[idx] = values[idx + 1];
            }
            keys[FULL_SIZE - 1] = null;
            values[FULL_SIZE - 1] = null;
            return;
        }
        int idx = increment(holeIdx);
        while (keys[idx] != null) {
            K keyToReinsert = keys[idx];
            V valueToReinsert = values[idx];
            keys[idx] = null;
            values[idx] = null;
            internalInsert(keyToReinsert, valueToReinsert, keyHash(keyToReinsert));
            idx = increment(idx);
        }
    }
    
    private void internalInsert(K key, V value, int hash) {
        int idx = hash & mask;
        while (keys[idx] != null) {
            idx = increment(idx);
        }
        keys[idx] = key;
        values[idx] = value;
    }
    
    private void ensureCapacityFor(int size) {
        if (mask == 0 && size <= FULL_SIZE) {
            return;
        }
        
        if (keys.length * 3 >= size * 4) {
            return;
        }
        
        int newCapacity = keys.length;
        while (newCapacity * 3 < size * 4) {
            newCapacity *= 2;
        }
        
        buildHashTable(keys, values, newCapacity);
    }
    
    private void checkEmptySpace() {
        if (size <= FULL_SIZE) {
            if (keys.length != FULL_SIZE) {
                buildFullTable();
            }
            return;
        }
        
        if (keys.length <= size * 5) return;
        
        int newCapacity = keys.length;
        while ((newCapacity > size * 5) && (newCapacity > FULL_SIZE)) {
            newCapacity /= 2;
        }
        
        buildHashTable(keys, values, newCapacity);
    }
    
    private void buildFullTable() {
        K[] fullKeys = keys;
        V[] fullValues = values;
        keys = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        values = ArrayUtil.unsafeCastedNewArray(FULL_SIZE);
        mask = 0;
        int idx = 0;
        for (int i = 0; i < fullKeys.length; ++i) {
            if (fullKeys[i] == null) continue;
            keys[idx] = fullKeys[i];
            values[idx] = fullValues[i];
            idx++;
        }
    }
    
    private void buildHashTable(K[] keys, V[] values, int tableSize) {
        Assert.assertTrue((tableSize & (tableSize - 1)) == 0, "Table size must be a power of two!");
        this.keys = ArrayUtil.unsafeCastedNewArray(tableSize);
        this.values = ArrayUtil.unsafeCastedNewArray(tableSize);
        mask = tableSize - 1;
        for (int i = 0; i < keys.length; ++i) {
            K key = keys[i];
            if (key == null) continue;
            internalInsert(key, values[i], keyHash(key));
        }
    }
    
    /**
     * @return the next index with non-empty key or {@code keys.length} if the end of the array is reached
     */
    private int nextKeyIndex(int startIndex) {
        int index = startIndex;
        index++;
        while (index < keys.length && keys[index] == null) {
            index++;
        }
        return index;
    }
    
    @Override
    public int keyCount() {
        return size;
    }
    
    @Override
    public EntryIterator<K, V> entryIterator() {
        return new HashMapIterator();
    }
    
    @Override
    public K getFirstKeyOrNull() {
        int firstIndex = nextKeyIndex(-1);
        if (firstIndex == keys.length) {
            return null;
        }
        return keys[firstIndex];
    }
    
    @Override
    public Set<K> keys() {
        return new Keys();
    }
    
    private class Keys extends AbstractKeySet<K> {
        
        public Keys() {
            super(HashMap.this);
        }
        
        @Override
        public K findEqualOrNull(K element) {
            return HashMap.this.findEqualKeyOrNull(element);
        }
        
    }
    
    @Override
    public Collection<V> values() {
        return new Values();
    }
    
    private class Values implements Collection<V> {
        
        @Override
        public int size() {
            return HashMap.this.keyCount();
        }
        
        @Override
        public boolean isEmpty() {
            return HashMap.this.isEmpty();
        }
        
        @Override
        @CheckForNull
        public V getFirstOrNull() {
            int firstIndex = nextKeyIndex(-1);
            if (firstIndex == keys.length) {
                return null;
            }
            return values[firstIndex];
        }
        
        @Override
        public Iterator<V> iterator() {
            return new EntryValueIterator<V>(HashMap.this.entryIterator());
        }
        
    }
    
}
