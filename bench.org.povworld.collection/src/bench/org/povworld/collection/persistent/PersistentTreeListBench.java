package bench.org.povworld.collection.persistent;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.persistent.PersistentList;
import org.povworld.collection.persistent.PersistentTreeList;

import bench.org.povworld.collection.StringProducer;

/**
 * Benchmark for {@link PersistentTreeList}.
 */
public class PersistentTreeListBench extends PersistentListBench<String> {
    
    public PersistentTreeListBench(
            @DivideBy @ForEachInt({ /*0,*/ 1, /* 10,*/ 100, /*1000,*/ 10000,/* 100000, 1000000*/}) int elementCount)
//      @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount)
    {
        super(elementCount, StringProducer.createDefaultElementProducer());
    }
    
    @Override
    protected PersistentList<String> empty() {
        return PersistentTreeList.empty();
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(PersistentTreeListBench.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}
