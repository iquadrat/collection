package org.povworld.collection.persistent;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractMultiMap;
import org.povworld.collection.immutable.ImmutableContainer;

/**
 * Implements a hash map that maps keys of type {@code K} to a set of type {@code V}.
 * A key cannot map to an empty set, that is, when the last element of a list is removed,
 * the corresponding key is removed as well. The set of values for a key is unordered.
 */
@NotThreadSafe
public class PersistentMultiMapImpl<K, V> extends AbstractMultiMap<K, V, Set<V>> implements PersistentMultiMap<K, V> {
    
    private static final PersistentMultiMap<Object, Object> EMPTY_MAP = new PersistentMultiMapImpl<Object, Object>();
    
    private final PersistentMap<K, PersistentSet<V>> map;
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentMultiMap<K, V> empty() {
        return (PersistentMultiMap<K, V>)EMPTY_MAP;
    }
    
    private PersistentMultiMapImpl() {
        this(PersistentHashMap.<K, PersistentSet<V>>empty());
    }
    
    public PersistentMultiMapImpl(PersistentMap<K, PersistentSet<V>> mapping) {
        map = mapping;
    }
    
    protected PersistentSet<V> emptyValueSet() {
        return PersistentCollections.setOf();
    }
    
    @Override
    protected final PersistentMap<K, PersistentSet<V>> getMap() {
        return map;
    }
    
    @Override
    @Nonnull
    public PersistentSet<V> get(K key) {
        PersistentSet<V> values = map.get(key);
        if (values == null) return emptyValueSet();
        return values;
    }
    
    @Override
    public PersistentMultiMap<K, V> with(K key, V value) {
        PersistentSet<V> currentValues = get(key);
        PersistentSet<V> newValues = currentValues.with(value);
        if (currentValues == newValues) return this;
        return new PersistentMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentMultiMap<K, V> withAll(K key, Iterable<? extends V> values) {
        PersistentSet<V> currentValues = get(key);
        PersistentSet<V> newValues = currentValues.withAll(values);
        if (currentValues == newValues) return this;
        return new PersistentMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentMultiMap<K, V> without(K key) {
        PersistentMap<K, PersistentSet<V>> newMap = map.without(key);
        if (map == newMap) return this;
        return new PersistentMultiMapImpl<K, V>(newMap);
    }
    
    @Override
    public PersistentMultiMap<K, V> without(K key, V value) {
        PersistentSet<V> currentValues = get(key);
        PersistentSet<V> newValues = currentValues.without(value);
        if (currentValues == newValues) return this;
        if (newValues.isEmpty()) return without(key);
        return new PersistentMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentMultiMap<K, V> withoutAll(K key, Collection<? extends V> values) {
        PersistentSet<V> currentValues = get(key);
        PersistentSet<V> newValues = currentValues.withoutAll(values);
        if (currentValues == newValues) return this;
        if (newValues.isEmpty()) return without(key);
        return new PersistentMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentMultiMap<K, V> cleared() {
        return empty();
    }
    
    @Override
    public ImmutableContainer<K> keys() {
        return map.keys();
    }
    
    @Override
    public boolean contains(K key, V value) {
        return get(key).contains(value);
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public EntryIterator<K, Set<V>> entryIterator() {
        return (EntryIterator<K, Set<V>>)(EntryIterator<?,?>)map.entryIterator();
    }
    
    @Override
    public final Identificator<? super K> getKeyIdentificator() {
        return map.getKeyIdentificator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final Identificator<? super Set<V>> getValueIdentificator() {
        Identificator<?> valueIdentificator = map.getValueIdentificator();
        return (Identificator<? super Set<V>>)valueIdentificator;
    }
    
}
