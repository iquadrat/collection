package bench.org.povworld.collection.persistent;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.persistent.PersistentTreeMap;
import org.povworld.collection.persistent.PersistentMap;

/**
 * Benchmark for {@link PersistentTreeMap}.
 */
public class PersistentTreeMapBenchString extends PersistentMapBenchString {
    
    public PersistentTreeMapBenchString(@DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int keyCount) {
        super(keyCount);
    }
    
    @Override
    protected PersistentMap<String, String> empty() {
        return PersistentTreeMap.empty(String.class);
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(PersistentTreeMapBenchString.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}
