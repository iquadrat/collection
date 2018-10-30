package org.povworld.collection.mutable;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.Map;
import org.povworld.collection.ListMultiMap;
import org.povworld.collection.common.AbstractMultiMap;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.persistent.PersistentCollections;

/**
 * Implements a hash map that maps keys of type {@code K} to a list of type {@code V}.
 * A key cannot map to an empty list, that is, when the last element of a list is removed,
 * the corresponding key is removed as well. The list of values for a key is stored in
 * insertion-order and may contain a value multiple times.
 * 
 * @param <K> key type
 * @param <V> value type
 **/
@NotThreadSafe
public class HashListMultiMap<K, V> extends AbstractMultiMap<K, V, List<V>> implements ListMultiMap<K, V> {
    
    public static final int DEFAULT_CAPACITY = 16;
    private static final ListIdentificator<?> DEFAULT_VALUE_COMPARATOR = new ListIdentificator<>(CollectionUtil.getObjectIdentificator()); 
    
    private final ListIdentificator<V> valuesIdentificator;
    private final HashMap<K, TreeList<V>> map;
    
    /**
     * Creates an empty ordered multi-map with default size.
     */
    public HashListMultiMap() {
        this(DEFAULT_CAPACITY);
    }
    
    /**
     * Creates an empty ordered multi-map with the given size hint.
     * @param capacity the expected number of keys in the map
     * @see #keyCount()
     */
    @SuppressWarnings("unchecked")
    public HashListMultiMap(int capacity) {
        this.map = new HashMap<>(capacity);
        this.valuesIdentificator = (ListIdentificator<V>)DEFAULT_VALUE_COMPARATOR;
    }
    
    public HashListMultiMap(int size, final Identificator<? super K> keyIdentificator, Identificator<? super V> valueIdentificator) {
        map = new HashMap<K, TreeList<V>>(size) {
            @Override
            public Identificator<? super K> getKeyIdentificator() {
                return keyIdentificator;
            }
        };
        this.valuesIdentificator = new ListIdentificator<>(valueIdentificator);
    }
    
    /**
     * Creates a new ordered multi-map which gets initialized by a copy of
     * the given {@code base} map.
     */
    public HashListMultiMap(ListMultiMap<? extends K, ? extends V> base) {
        this(base.keyCount());
        // make a deep copy
        EntryIterator<? extends K, ? extends List<? extends V>> entryIterator = base.entryIterator();
        while (entryIterator.next()) {
            TreeList<V> treeList = TreeList.<V>newBuilder().addAll(entryIterator.getCurrentValue()).build();
            map.put(entryIterator.getCurrentKey(), treeList);
        }
    }
    
    @Override
    public List<V> get(K key) {
        TreeList<V> values = map.get(key);
        if (values == null) {
            return PersistentCollections.listOf();
        }
        return values;
    }
    
    @Override
    protected Map<K, ? extends List<V>> getMap() {
        return map;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public EntryIterator<K, List<V>> entryIterator() {
        return (EntryIterator<K, List<V>>)(EntryIterator<?,?>)map.entryIterator();
    }
    
    /**
     * Appends value to the beginning of the list associated with given key.
     */
    public void putAtBegin(K key, V value) {
        getOrCreate(key).add(value, 0);
    }
    
    /**
     * Appends value to the end of the list associated with given key.
     */
    public void putAtEnd(K key, V value) {
        PreConditions.paramNotNull(key);
        PreConditions.paramNotNull(value);
        getOrCreate(key).add(value);
    }
    
    /**
     * Appends values to the end of the list associated with the given key.
     */
    public void putAll(K key, Collection<? extends V> values) {
        PreConditions.paramNotNull(key);
        if (values.isEmpty()) return;
        TreeList<V> coll = getOrCreate(key);
        for (V value: values) {
            coll.add(value);
        }
    }
    
    /**
     * Inserts {@code value} at given {@code index} into the list associated with the given {@code key}.
     * 
     * @throws IndexOutOfBoundsException if {@code index} is negative or larger than the number of 
     *                                   values already associated with the key
     */
    public void putAt(K key, V value, int index) {
        PreConditions.paramNotNull(key);
        PreConditions.paramNotNull(value);
        getOrCreate(key).add(value, index);
    }
    
    /**
     * Returns the list of values associated with key. If there is no list
     * for given key, a new, empty list is created and inserted into the map.
     */
    private TreeList<V> getOrCreate(K key) {
        TreeList<V> result = map.get(key);
        if (result == null) {
            result = new TreeList<V>() {
                @Override
                public Identificator<? super V> getIdentificator() {
                    return valuesIdentificator.getIdentificator();
                }
            };
            map.put(key, result);
        }
        return result;
    }
    
    /**
     * Removes all values associated with the given key.
     * 
     * @return true iff the collection has changed
     */
    public boolean remove(K key) {
        Collection<V> list = map.remove(key);
        return list != null;
    }
    
    /**
     * Removes the first occurrence of given value from the collection associated with given key.
     * 
     * @return true iff the collection has changed.
     */
    public boolean remove(K key, V value) {
        TreeList<V> values = map.get(key);
        if (values == null || !values.remove(value)) {
            return false;
        }
        if (values.isEmpty()) {
            map.remove(key);
        }
        return true;
    }
    
    /**
     * Removes all values from the list associated with the given key.
     *
     * @param key the key whose values are removed
     * @param values the values to remove
     * @return true if values have been removed
     */
    public boolean removeAll(K key, Iterable<V> values) {
        TreeList<V> list = map.get(key);
        if (list == null) {
            return false;
        }
        int size = list.size();
        list.removeAll(values);
        if (list.isEmpty()) {
            map.remove(key);
        }
        return size != list.size();
    }
    
    /**
     * Replaces in the list associated with {@code key} all occurrences
     * of {@code oldValue} (all elements {@code e} with {@code e.equals(oldValue)}
     * by {@code newValue}.
     * 
     * @param key key that references the list
     * @param oldValue old value to replace
     * @param newValue new value to replace with
     * @return the number of replacements that have been done
     */
    public int replaceAll(K key, V oldValue, V newValue) {
        TreeList<V> list = map.get(key);
        if (list == null) {
            return 0;
        }
        return list.replaceAll(oldValue, newValue);
    }
    
    /**
     * Removes all entries of the map.
     * @post isEmpty()==true
     */
    public void clear() {
        map.clear();
    }
    
    @Override
    public Identificator<? super List<V>> getValueIdentificator() {
        return valuesIdentificator;
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
    
}
