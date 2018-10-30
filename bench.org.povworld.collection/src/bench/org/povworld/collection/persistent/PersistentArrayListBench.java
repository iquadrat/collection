package bench.org.povworld.collection.persistent;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.persistent.PersistentArrayList;
import org.povworld.collection.persistent.PersistentList;

import bench.org.povworld.collection.StringProducer;

/**
 * Benchmark for {@link PersistentArrayList}.
 */
public class PersistentArrayListBench extends PersistentListBench<String> {
    
    public PersistentArrayListBench(
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount) {
        super(elementCount, StringProducer.createDefaultElementProducer());
    }
    
    @Override
    protected PersistentList<String> empty() {
        return PersistentArrayList.empty();
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(PersistentArrayListBench.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
}
