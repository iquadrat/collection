package org.povworld.collection.persistent;

import java.util.NoSuchElementException;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.common.AbstractMultiMap;
import org.povworld.collection.immutable.ImmutableContainer;

public class PersistentListMultiMapImpl<K, V> extends AbstractMultiMap<K, V, List<V>> implements PersistentListMultiMap<K, V> {
    
    private static final PersistentListMultiMap<Object, Object> EMPTY_MAP = new PersistentListMultiMapImpl<Object, Object>();
    
    private final PersistentMap<K, PersistentList<V>> map;
    
    @SuppressWarnings("unchecked")
    public static <K, V> PersistentListMultiMap<K, V> empty() {
        return (PersistentListMultiMap<K, V>)EMPTY_MAP;
    }
    
    protected PersistentListMultiMapImpl() {
        map = PersistentHashMap.empty();
    }
    
    public PersistentListMultiMapImpl(PersistentMap<K, PersistentList<V>> mapping) {
        map = mapping;
    }
    
    protected PersistentList<V> getEmptyValueList() {
        return PersistentCollections.listOf();
    }
    
    @Override
    protected final PersistentMap<K, PersistentList<V>> getMap() {
        return map;
    }
    
    @Override
    public PersistentList<V> get(K key) {
        PersistentList<V> values = map.get(key);
        if (values == null) return getEmptyValueList();
        return values;
    }
    
    @Override
    public PersistentListMultiMap<K, V> withAtEnd(K key, V value) {
        PersistentList<V> newValues = get(key).with(value);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> withAtBegin(K key, V value) {
        PersistentList<V> newValues = get(key).with(value, 0);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> withAfter(K key, V value, V previous) {
        PersistentList<V> values = get(key);
        int index = CollectionUtil.indexOf(values, previous);
        if (index == -1) throw new NoSuchElementException();
        PersistentList<V> newValues = values.with(value, index + 1);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> withBefore(K key, V value, V next) {
        PersistentList<V> values = get(key);
        int index = CollectionUtil.indexOf(values, next);
        if (index == -1) throw new NoSuchElementException();
        PersistentList<V> newValues = values.with(value, index);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> withAll(K key, Collection<? extends V> values) {
        if (values.isEmpty()) return this;
        PersistentList<V> newValues = get(key).withAll(values);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> without(K key, V value) {
        PersistentList<V> currentValues = get(key);
        
        int index = CollectionUtil.indexOf(currentValues, value);
        if (index == -1) {
            // key/value pair is not present, result is same as this map
            return this;
        }
        
        if (currentValues.size() == 1) {
            // the only value for that key is removed, so there
            // are no remaining values for the key
            return without(key);
        }
        
        PersistentList<V> newValues = currentValues.without(index);
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> withoutAll(K key, Collection<? extends V> values) {
        PersistentList<V> currentValues = get(key);
        PersistentList<V> newValues = PersistentCollections.removeAll(currentValues, values);
        if (newValues == currentValues) {
            // no value has been removed, result is same as this map
            return this;
        }
        if (newValues.isEmpty()) {
            return without(key);
        }
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public PersistentListMultiMap<K, V> without(K key) {
        PersistentMap<K, PersistentList<V>> newMap = map.without(key);
        if (newMap == map) {
            // key is not present, result is same as this map
            return this;
        }
        return new PersistentListMultiMapImpl<K, V>(newMap);
    }
    
    @Override
    public ImmutableContainer<K> keys() {
        return map.keys();
    }
    
    @Override
    public PersistentListMultiMap<K, V> cleared() {
        if (isEmpty()) return this;
        return empty();
    }
    
    @Override
    public PersistentListMultiMap<K, V> withReplacement(K key, V oldValue, V newValue) {
        PersistentList<V> currentValues = get(key);
        PersistentList<V> newValues = currentValues.withAllReplaced(oldValue, newValue);
        if (newValues == currentValues) return this;
        return new PersistentListMultiMapImpl<K, V>(map.with(key, newValues));
    }
    
    @Override
    public String toString() {
        return map.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public EntryIterator<K, List<V>> entryIterator() {
        return (EntryIterator<K, List<V>>)(EntryIterator<?, ?>)map.entryIterator();
    }
    
    @Override
    public final Identificator<? super K> getKeyIdentificator() {
        return map.getKeyIdentificator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Identificator<? super List<V>> getValueIdentificator() {
        Identificator<?> valueIdentificator = map.getValueIdentificator();
        return (Identificator<? super List<V>>)valueIdentificator;
    }
    
}
