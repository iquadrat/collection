package bench.org.povworld.collection.persistent;

import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.persistent.PersistentHashMap;
import org.povworld.collection.persistent.PersistentMap;

/**
 * Benchmarks for {@link PersistentHashMap}.
 */
public class PersistentHashMapBenchString extends PersistentMapBenchString {
    
    public PersistentHashMapBenchString(
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000})//@ForEachInt({16100})
            //@ForEachInt({1 * 10000, 2 * 10000, 4 * 10000, 8 * 10000, 16 * 10000, 32 * 10000, 64 * 10000}) 
            int keyCount) {
        super(keyCount);
    }
    
    @Override
    protected PersistentMap<String, String> empty() {
        return PersistentHashMap.empty();
    }
}
