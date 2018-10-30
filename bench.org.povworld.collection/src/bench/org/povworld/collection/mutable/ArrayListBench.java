package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.mutable.ArrayList;

import bench.org.povworld.collection.StringProducer;

public class ArrayListBench extends AbstractListBench<String, ArrayList<String>> {
    
    public ArrayListBench(@DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount) {
        super(elementCount, new StringProducer(6, 8), String.class);
    }
    
    @Override
    protected CollectionBuilder<String, ArrayList<String>> newBuilder() {
        return ArrayList.newBuilder();
    }
    
    @Bench(maxDeviation = 0.005, maxRunCount = 100)
    public Object addNoReserve() {
        ArrayList<String> list = new ArrayList<>();
        for(String s: this.containedElements) {
            list.add(s);
        }
        return list;
    }
    
    @Bench
    public Object addReserve() {
        ArrayList<String> list = new ArrayList<>(elementCount);
        for(String s: this.containedElements) {
            list.add(s);
        }
        return list;
        
    }
    
    
}
