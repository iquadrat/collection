package org.povworld.collection.persistent;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.immutable.ImmutableCollection;
import org.povworld.collection.immutable.ImmutableContainer;
import org.povworld.collection.immutable.ImmutableMap;

/**
 * A map which maps elements of the key type {@code K} to the value type {@code
 * V}. The map is immutable, i.e., the mappings from the keys to the values as
 * well as the number of mappings does never change. Instead, each modifying
 * operation returns a new map which differs from the current one only by the
 * result of the operation.
 * <p>
 * NOTES:
 * <ul>
 * <li>The map uses an instance of {@link Identificator} to compare keys.</li>
 *
 * <li>The iterators returned by {@link PersistentMap#entryIterator()}
 *   <ul>
 *   <li>special iterator that allow to access keys and values of the current iteration independently</li>
 *   <li>are not thread-safe</li>
 *   </ul>
 * </li>
 *
 * <li><code>null</code> is not allowed as value or as key</li>
 *
 * </ul>
 * 
 * @param <K> the map's key type
 * @param <V> the map's value type
 */
@Immutable
public interface PersistentMap<K, V> extends ImmutableMap<K, V> {
    
    @CheckForNull
    public K getFirstKeyOrNull();
    
    @Override
    public ImmutableContainer<K> keys();
    
    @Override
    public ImmutableCollection<V> values();
    
    /**
     * Creates a new {@link PersistentMap} which is equal to this map but associates
     * the given key with the given value. If the map already contained a mapping
     * for the given key, the old value is replaced. If the old value of the mapping
     * is equal to the new value, the old and new map are equal. Nonetheless a new
     * map object is created if the old and new value are equal (as given by {@link #equals(Object)})
     * but not the same objects. So if you want to know if the key/value pair was already contained before or
     * need to know the old mapping you must first retrieve the old value with {@link #get(Object)}
     * before inserting the new value.
     */
    // TODO why? shouldn't we just use a replaceable value comparator instead?
    @CheckReturnValue
    public PersistentMap<K, V> with(K key, V value);
    
    /**
     * Creates a new {@link PersistentMap} which is equal to this map but has the
     * mapping for given key removed if there was any mapping for the key.
     * If the key is not contained in the map there the old and the new map are the
     * same. To indicate this, the same object is returned.
     */
    @CheckReturnValue
    public PersistentMap<K, V> without(K key);
    
    /**
     * Creates a new {@link PersistentMap} which has all key/value pairs of the given {@link Map} inserted.
     */
    @CheckReturnValue
    public PersistentMap<K, V> withAll(Map<? extends K, ? extends V> map);
    
    /**
     * Creates a new {@link PersistentMap} which is empty or returns the same object if the map is already empty.
     */
    @CheckReturnValue
    public PersistentMap<K, V> cleared();
    
}
