package org.povworld.collection;

import java.util.AbstractMap;

import javax.annotation.CheckForNull;

/**
 * A map associates keys of some type {@code K} to values of some type {@code V}.
 * <p>
 * Each key is thereby mapped to at most one value. If there is a mapping for
 * some key, we say that the map contains the given key.
 * <p>
 * As the {@code Map} interface requires specific implementations of {@link #equals(Object)}
 * and {@link #hashCode()}, it is advised that implementors of the interface
 * inherit from {@link AbstractMap} which implements those methods. 
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface Map<K, V> {
    
    /**
     * @return the number of keys in this map
     */
    public int keyCount();
    
    public default boolean isEmpty() {
        return keyCount() == 0;
    }
    
    /**
     * @return the identificator used to compare keys
     */
    public Identificator<? super K> getKeyIdentificator();
    
    public Identificator<? super V> getValueIdentificator();
    
    /**
     * Returns the value associated which the given key or {@code null} if this
     * map contains no mapping for the key.
     */
    @CheckForNull
    public V get(K key);
    
    /**
     * Returns the value associated with the given key or {@code defaultValue} if
     * this map contains no mapping for the key.
     */
    public default V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return (value == null) ? defaultValue : value;
    }
    
    /**
     * Checks if the map contains the given key.
     */
    public boolean containsKey(K key);
    
    /**
     * @return any key in this map or null if it is empty
     */
    @CheckForNull
    public default K getFirstKeyOrNull() {
        return keys().getFirstOrNull();
    }
    
    /**
     * @return all keys contained in the map
     */
    public Container<K> keys();
    
    /**
     * @return all values contained in the map
     */
    public Collection<V> values();
    
    /**
     * @return an iterator over all key-value pairs in the map
     */
    public EntryIterator<K, V> entryIterator();
    
    /**
     * Two maps are equal if their key identificators are equal and all their
     * key/value pairs are equal whereas the keys and values are compared with their
     * corresponding identificators.
     *
     * @see #getKeyIdentificator()
     * @see #getValueIdentificator()
     */
    @Override
    public boolean equals(Object obj);
    
    /**
     * The hashcode of the map is calculated as:
     * <pre>
       int hashcode = -1;
       EntryIterator<K, ? extends V> iter = entryIterator();
       while (iter.next()) {
           int pairHash =
                   getKeyIdentificator().hashCode(iter.getCurrentKey()) +
                           255 * getValueIdentificator().hashCode(iter.getCurrentValue());
           hashcode += pairHash;
       }
       return hashcode;
     * </pre>
     */
    @Override
    public int hashCode();
    
}
