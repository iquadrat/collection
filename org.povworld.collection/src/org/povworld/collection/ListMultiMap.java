package org.povworld.collection;

/**
 * An interface for {@link ListMultiMap}s where the collection of values associated
 * with some key has a defined order and can be accessed by index.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface ListMultiMap<K, V> extends Map<K, List<V>> {
    
    /**
     * @return the list of values associated with the given key.
     *         If the key is not in the map, an empty list is returned.
     */
    public List<V> get(K key);
    
    /**
     * @return number of values associated with given key or 0 if the key is not in the map
     */
    public int numberOfValues(K key);
    
}
