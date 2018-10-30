package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;

public class HashSetIntegerBench extends AbstractHashSetBench<Integer> {
    
    public HashSetIntegerBench(
            // @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000,
            // 50000000}) int elementCount
            @DivideBy @ForEachInt({10000, 1000000}) int elementCount) {
        super(elementCount, new IntegerProducer());
    }
}
