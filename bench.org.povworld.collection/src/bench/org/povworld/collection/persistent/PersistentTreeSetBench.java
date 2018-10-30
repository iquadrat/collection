package bench.org.povworld.collection.persistent;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.persistent.PersistentOrderedSet;
import org.povworld.collection.persistent.PersistentTreeSet;

import bench.org.povworld.collection.StringProducer;

public class PersistentTreeSetBench extends PersistentOrderedSetBench<String> {
    
    public PersistentTreeSetBench(
//      @DivideBy @ForEachInt({ /*0,*/ 1, /* 10,*/ 100, /*1000,*/ 10000,/* 100000, 1000000*/}) int elementCount) 
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount) {
        super(elementCount, StringProducer.createDefaultElementProducer());
    }
    
    @Override
    protected PersistentOrderedSet<String> empty() {
        return PersistentTreeSet.empty(CollectionUtil.getDefaultComparator(String.class));
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(PersistentTreeSetBench.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}
