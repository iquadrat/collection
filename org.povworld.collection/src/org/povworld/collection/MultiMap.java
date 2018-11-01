package org.povworld.collection;

import javax.annotation.Nonnull;

/**
 * An interface for {@link ListMultiMap}s where the collection of values associated
 * with some key contain no duplicates and have no defined order. 
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface MultiMap<K, V> extends Map<K, Set<V>> {
    
    /**
     * @return the set of values associated with the given key.
     *         If the key is not in the map, an empty set is returned.
     */
    @Override
    @Nonnull
    public Set<V> get(K key);
    
    /**
     * @return true if the set associated with given key contains the given value
     */
    public boolean contains(K key, V value);

    public int numberOfValues(K key);
    
}
