package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.annotations.MemoryBench;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;
import org.povworld.collection.persistent.PersistentHashSet;
import org.povworld.collection.persistent.PersistentMultiMap;
import org.povworld.collection.persistent.PersistentMultiMapImpl2;
import org.povworld.collection.persistent.PersistentSet;

import bench.org.povworld.collection.ElementProducer;
import bench.org.povworld.collection.StringProducer;

public class MemoryTest {
    
    protected final Random random = new Random(17);
    
    protected final Object[] keys;
    
    protected final Object[] nonKeys;
    
    public MemoryTest(
            @DivideBy @ForEachInt({100000}) int keyCount) {
        ElementProducer<String> keyProducer = new StringProducer(3, 13);
        ArrayList.Builder<String> nonKeyBuilder = ArrayList.newBuilder();
        ArrayList.Builder<String> keyBuilder = ArrayList.newBuilder();
        for (int i = 0; i < keyCount; ++i) {
            keyBuilder.add(keyProducer.produce());
            nonKeyBuilder.add(keyProducer.produce());
        }
        this.keys = CollectionUtil
                .toObjectArray(ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(keyBuilder.build()))));
        this.nonKeys = CollectionUtil.toObjectArray(ImmutableCollections.asList(CollectionUtil.shuffle(nonKeyBuilder.build())));
    }
    
    private final String BLA = "BLA";
    
    @MemoryBench
    public Object memoryMap() throws InterruptedException {
        PersistentMultiMap<Object, Object> result = empty();
        for (Object key: keys) {
            result = result.with(key, BLA);
        }
        return result;
    }
    
    @MemoryBench
    public Object linerarHashSet() {
        HashSet<Object> result = new HashSet<>();
        result.addAll(CollectionUtil.wrap(keys));
        return result;
    }
    
    @MemoryBench
    public Object memorySet() throws InterruptedException {
        PersistentSet<Object> result = PersistentHashSet.<Object>empty();
        for (Object key: keys) {
            result = result.with(key);
        }
//    Thread.sleep(100 * 1000);
        return result;
    }
    
    private PersistentMultiMap<Object, Object> empty() {
        return PersistentMultiMapImpl2.empty();
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(MemoryTest.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}
