package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.mutable.HashList;

import bench.org.povworld.collection.StringProducer;

/**
 * Benchmarks for {@link HashList}.
 */
public class HashListBench extends AbstractListBench<String, HashList<String>> {
    
    public HashListBench(@DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000 /*, 1000000*/}) int elementCount) {
        super(elementCount, new StringProducer(6, 8), String.class);
    }
    
    @Override
    protected CollectionBuilder<String, HashList<String>> newBuilder() {
        return HashList.newBuilder();
    }
    
    @Bench
    public boolean containsContained() {
        for (int i = 0; i < elementCount; ++i) {
            if (!collection.contains(containedElements[i])) return false;
        }
        return true;
    }
    
    @Bench
    public boolean containsNotContained() {
        for (int i = 0; i < elementCount; ++i) {
            if (collection.contains(notContainedElements[i])) return false;
        }
        return true;
    }
    
    @Bench
    public Object clearAdd() {
        collection.clear();
        for (int i = 0; i < elementCount; ++i) {
            collection.add(containedElements[i]);
        }
        return collection;
    }
}
