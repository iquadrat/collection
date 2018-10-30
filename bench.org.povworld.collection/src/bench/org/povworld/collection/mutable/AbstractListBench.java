package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.Tags;
import org.povworld.collection.List;

import bench.org.povworld.collection.ElementProducer;

public abstract class AbstractListBench<E, C extends List<E>> extends AbstractCollectionBench<E, C> {
    
    public AbstractListBench(@DivideBy int elementCount, ElementProducer<E> elementProducer, Class<E> type) {
        super(elementCount, elementProducer, type);
    }
    
    @Bench
    @Tags("get")
    public Object getRandom() {
        for (int i = 0; i < elementCount; ++i) {
            if (collection.get(permutation[i]) == this) return this;
        }
        return null;
    }
    
}
