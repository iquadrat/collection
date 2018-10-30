package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.List;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentMultiMap;
import org.povworld.collection.persistent.PersistentMultiMapImpl;

import bench.org.povworld.collection.ElementProducer;

/**
 * Benchmarks for {@link PersistentMultiMap} implementations.
 */
public abstract class PersistentMultiMapBench<K> {
    
    protected final Random random = new Random(17);
    
    protected final List<K> keys;
    
    protected final PersistentMultiMap<K, String> map;
    
    protected final List<K> nonKeys;
    
    protected final String value;
    
    public PersistentMultiMapBench(
            @DivideBy int keyCount, int valueCount, boolean variableValueCount, ElementProducer<K> keyProducer,
            ElementProducer<String> valueProducer) {
        ArrayList.Builder<K> nonKeyBuilder = ArrayList.newBuilder();
        PersistentMultiMap<K, String> map = empty();
        for (int i = 0; i < keyCount; ++i) {
            K key = keyProducer.produce();
            int values = valueCount;
            if (valueCount > 1 && variableValueCount) {
                values = random.nextInt(valueCount - 1) + 1;
            }
            for (int j = 0; j < values; ++j) {
                map = map.with(key, valueProducer.produce());
            }
            nonKeyBuilder.add(keyProducer.produce());
        }
        this.map = map;
        this.keys = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(map.keys())));
        this.nonKeys = ImmutableCollections.asList(CollectionUtil.shuffle(nonKeyBuilder.build()));
        this.value = valueProducer.produce();
    }
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        PersistentMultiMap<K, String> result = empty();
        EntryIterator<K, ? extends Set<String>> iterator = map.entryIterator();
        while (iterator.next()) {
            result = result.withAll(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
        return result;
    }
    
    private PersistentMultiMap<K, String> empty() {
        return PersistentMultiMapImpl.empty();
    }
    
    @Bench
//  @Ignore
    public Object getContainedKey() {
        for (K key: keys) {
            if (map.get(key) == this) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object getNotContainedKey() {
        for (K key: nonKeys) {
            if (map.get(key) == this) return null;
        }
        return this;
    }
    
    @Bench
    public Object putNewKey() {
        for (K key: nonKeys) {
            if (map.with(key, value) == this) return null;
        }
        return this;
    }
    
    @Bench
    public Object duplicate() {
        EntryIterator<K, ? extends Set<String>> entryIterator = map.entryIterator();
        PersistentMultiMap<K, String> newMap = map.cleared();
        while (entryIterator.next()) {
            newMap = newMap.withAll(entryIterator.getCurrentKey(), entryIterator.getCurrentValue());
        }
        return newMap;
    }
    
}
