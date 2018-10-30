package bench.org.povworld.collection.persistent;

import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.persistent.PersistentHashSet;
import org.povworld.collection.persistent.PersistentSet;

import bench.org.povworld.collection.mutable.IntegerProducer;

/**
 * Benchmark for {@link PersistentHashSet}.
 */
public class PersistentHashSetIntBench extends PersistentSetBench<Integer> {
    
    public PersistentHashSetIntBench(
    //      @ForEachInt({8,10,12,15,16,18,20,24,30,32,35,40,45,50,55}) int  splitSize,
//      @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount
            @DivideBy @ForEachInt({1000000, 2000000, 5000000}) int elementCount) {
        super(foo(40, elementCount), new IntegerProducer(), false);
    }
    
    private static int foo(int splitSize, int elementCount) {
        PersistentHashSet.setHashBucketSplitSize(splitSize);
        return elementCount;
    }
    
    @Override
    protected CollectionBuilder<Integer, ? extends PersistentSet<Integer>> newBuilder() {
        return PersistentHashSet.newBuilder();
    }
    
}
