package org.povworld.collection.persistent;

import java.util.NoSuchElementException;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.ListMultiMap;
import org.povworld.collection.immutable.ImmutableContainer;

@Immutable
public interface PersistentListMultiMap<K, V> extends ListMultiMap<K, V> {
    
    @Override
    public PersistentList<V> get(K key);
    
    @Override
    public ImmutableContainer<K> keys();
    
    /**
     * Appends values to the end of the list associated with the given key.
     * @return the new map
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withAll(K key, Collection<? extends V> values);
    
    /**
     * Removes all values from the list associated with the given key.
     *
     * @param key the key whose values are removed
     * @param values the values to remove
     * @return the new map
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withoutAll(K key, Collection<? extends V> values);
    
    /**
     * Removes all values associated with the given key.
     * @return the new map
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> without(K key);
    
    /**
     * Removes the first occurrence of given value from the collection associated with given key.
     *
     * @return <code>true</code> iff the collection has changed.
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> without(K key, V value);
    
    /**
     * @return an empty map of the same kind
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> cleared();
    
    /**
     * Appends value to the end of the list associated with given key.
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withAtEnd(K key, V value);
    
    /**
     * Appends value to the beginning of the list associated with given key.
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withAtBegin(K key, V value);
    
    /**
     * Inserts value at given position into the list associated with given key.
     * The value is inserted after the (first occurrence of the) <code>previous</code> element.
     *
     * @throws NoSuchElementException if <code>previous</code> does not exists in the list
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withAfter(K key, V value, V previous) throws NoSuchElementException;
    
    /**
     * Inserts value at given position into the list associated with given key.
     * The value is inserted  before the (first occurrence of the) <code>next</code> element.
     *
     * @throws NoSuchElementException if <code>next</code> does not exists in the list
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withBefore(K key, V value, V next) throws NoSuchElementException;
    
    /**
     * Replaces in the list associated with <code>key</code> all occurrences
     * of <code>oldValue</code> (all elements <code>e</code> with <code>e.equals(oldValue)</code>)
     * with <code>newValue</code>.
     *
     * @param key key that references the list
     * @param oldValue old value to replace
     * @param newValue new value to replace with
     */
    @CheckReturnValue
    public PersistentListMultiMap<K, V> withReplacement(K key, V oldValue, V newValue);
    
}
