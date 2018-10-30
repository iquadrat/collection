package org.povworld.collection.mutable;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.MultiMap;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractMultiMap;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.persistent.PersistentCollections;

/**
 * Implementation of a multi-map which uses a hash map to store the mapping between keys and the 
 * set of values and a hash set to store the values.
 * 
 * @param <K> key type
 * @param <V> value type
 */
@NotThreadSafe
public class HashMultiMap<K, V> extends AbstractMultiMap<K, V, Set<V>> implements MultiMap<K, V> {
    
    private final HashMap<K, HashSet<V>> map = new HashMap<K, HashSet<V>>();
    
    public HashMultiMap() {}
    
    public HashMultiMap(MultiMap<K, V> multiMap) {
        EntryIterator<K, ? extends Collection<V>> entries = multiMap.entryIterator();
        while (entries.next()) {
            getOrCreate(entries.getCurrentKey()).addAll(entries.getCurrentValue());
        }
    }
    
    @Override
    protected Map<K, ? extends HashSet<V>> getMap() {
        return map;
    }
    
    protected final HashSet<V> getOrCreate(K key) {
        HashSet<V> result = map.get(key);
        if (result == null) {
            result = createSet();
            map.put(key, result);
        }
        return result;
    }
    
    protected HashSet<V> createSet() {
        return new HashSet<V>(1);
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
    
    @Override
    public boolean contains(K key, V value) {
        return get(key).contains(value);
    }
    
    /**
     * @return the unmodifiable collection of values associated with the given key.
     *         If the key is not in the map, an empty collection is returned.
     */
    @Override
    public Set<V> get(K key) {
        Set<V> values = map.get(key);
        if (values == null) {
            return PersistentCollections.setOf();
        }
        return values;
    }
    
    /**
     * Adds the given value to the collection associated with the given key.
     */
    public boolean put(K key, V value) {
        PreConditions.paramNotNull(value);
        return getOrCreate(key).add(value);
    }
    
    /**
     * Appends values to the end of the list associated with the given key.
     */
    public void putAll(K key, Collection<? extends V> values) {
        if (values.isEmpty()) return;
        getOrCreate(key).addAll(values);
    }
    
    /**
     * Removes the first occurrence of given value from the values associated with given key.
     * 
     * @return true if the collection has changed.
     */
    public boolean remove(K key, V value) {
        HashSet<V> values = map.get(key);
        if (values == null || !values.remove(value)) return false;
        if (values.isEmpty()) map.remove(key);
        return true;
    }
    
    /**
     * Removes all values from the list associated with the given key.
     * 
     * @param key the key whose values are removed
     * @param values the values to remove
     * @return true if values have been removed
     */
    // TODO return count?
    public boolean removeAll(K key, Iterable<? extends V> values) {
        HashSet<V> set = map.get(key);
        if (set == null) return false;
        int size = set.size();
        set.removeAll(values);
        if (set.isEmpty()) {
            map.remove(key);
            return true;
        }
        return size != set.size();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public EntryIterator<K, Set<V>> entryIterator() {
        return (EntryIterator<K, Set<V>>)(EntryIterator<?, ?>)map.entryIterator();
    }
    
    /**
     * Removes all values associated with the given key.
     * @return true if the collection has changed
     */
    public boolean remove(K key) {
        Collection<V> list = map.remove(key);
        return list != null;
    }
    
    /**
     * Removes all entries of the map.
     */
    public void clear() {
        map.clear();
    }
    
    @Override
    // TODO implement key identificator customization
    public final Identificator<? super K> getKeyIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    // TODO implement value identificator customization
    public final Identificator<? super Set<V>> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
}
