package org.povworld.collection.persistent;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.MultiMap;
import org.povworld.collection.immutable.ImmutableContainer;

@Immutable
public interface PersistentMultiMap<K, V> extends MultiMap<K, V> {
    
    @Override
    public PersistentSet<V> get(K key);
    
    @Override
    public ImmutableContainer<K> keys();
    
    /**
     * Adds the given value to the collection associated with the given key.
     * 
     * <p>Returns an updated map or the same object if it stayed unchanged. 
     */
    public PersistentMultiMap<K, V> with(K key, V value);
    
    /**
     * Appends values to the end of the list associated with the given key.
     * 
     * <p>Returns an updated map or the same object if it stayed unchanged.
     */
    public PersistentMultiMap<K, V> withAll(K key, Iterable<? extends V> values);
    
    /**
     * Removes all values associated with the given key.
     * 
     * <p>Returns an updated map or the same object if it stayed unchanged.
     */
    public PersistentMultiMap<K, V> without(K key);
    
    /**
     * Removes the first occurrence of given value from the collection
     * associated with given key.
     *
     * <p>Returns an updated map or the same object if it stayed unchanged.
     */
    public PersistentMultiMap<K, V> without(K key, V value);
    
    /**
     * Removes all values from the list associated with the given key.
     *
     * @param key
     *            the key whose values are removed
     * @param values
     *            the values to remove
     *            
     * <p>Returns an updated map or the same object if it stayed unchanged.
     */
    public PersistentMultiMap<K, V> withoutAll(K key, Collection<? extends V> values);
    
    /**
     * @return an empty map of the same kind
     */
    public PersistentMultiMap<K, V> cleared();
    
}
