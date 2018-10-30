package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;

import bench.org.povworld.collection.StringProducer;

public class HashSetStringBench extends AbstractHashSetBench<String> {
    
    public HashSetStringBench(
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount
    //@DivideBy @ForEachInt({10}) int elementCount
    ) {
        super(elementCount, StringProducer.createDefaultElementProducer());
    }
}
