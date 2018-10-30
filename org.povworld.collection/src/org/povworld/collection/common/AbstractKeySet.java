package org.povworld.collection.common;

import java.util.Iterator;

import org.povworld.collection.Map;
import org.povworld.collection.Set;

public abstract class AbstractKeySet<K> extends AbstractUnOrderedCollection<K> implements Set<K> {
    
    private final Map<K, ?> map;
    
    public AbstractKeySet(Map<K, ?> map) {
        this.map = map;
    }

    @Override
    public boolean contains(K element) {
        return map.containsKey(element);
    }
    
    @Override
    public int size() {
        return map.keyCount();
    }
    
    @Override
    public K getFirstOrNull() {
        return map.getFirstKeyOrNull();
    }
    
    @Override
    public Iterator<K> iterator() {
        return new EntryKeyIterator<>(map.entryIterator());
    }
    
}
