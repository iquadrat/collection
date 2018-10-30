package bench.org.povworld.collection.persistent;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentMap;

import bench.org.povworld.collection.ElementProducer;

public abstract class PersistentMapBench<K> {
    
    private final PersistentMap<K, String> map;
    
    private final List<K> keys;
    
    private final List<K> nonKeys;
    
    private final String value;
    
    public PersistentMapBench(@DivideBy int keyCount, ElementProducer<K> keyProducer, ElementProducer<String> valueProducer) {
        ArrayList.Builder<K> nonKeyBuilder = ArrayList.newBuilder();
        PersistentMap<K, String> map = empty();
        for (int i = 0; i < keyCount; ++i) {
            K key = keyProducer.produce();
            map = map.with(key, valueProducer.produce());
            nonKeyBuilder.add(keyProducer.produce());
        }
        this.map = map;
        this.keys = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(map.keys())));
        this.nonKeys = ImmutableCollections.asList(CollectionUtil.shuffle(nonKeyBuilder.build()));
        this.value = valueProducer.produce();
    }
    
    protected abstract PersistentMap<K, String> empty();
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        PersistentMap<K, String> result = empty();
        EntryIterator<K, ? extends String> iterator = map.entryIterator();
        while (iterator.next()) {
            result = result.with(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
        return result;
    }
    
    @Bench
//  @Ignore
    public Object getContainedKey() {
        for (K key: keys) {
            if (map.get(key) == null) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object getNotContainedKey() {
        for (K key: nonKeys) {
            if (map.get(key) == value) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object putNewKey() {
        for (K key: nonKeys) {
            if (map.with(key, value) == null) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object duplicate() {
        EntryIterator<K, ? extends String> entryIterator = map.entryIterator();
        PersistentMap<K, String> newMap = map.cleared();
        while (entryIterator.next()) {
            newMap = newMap.with(entryIterator.getCurrentKey(), entryIterator.getCurrentValue());
        }
        return newMap;
    }
    
}
