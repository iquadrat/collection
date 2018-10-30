package bench.org.povworld.collection.mutable;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.MathUtil;

import bench.org.povworld.collection.ElementProducer;

public abstract class AbstractCollectionBench<E, C extends Collection<E>> {
    
    protected final Random random = new Random(123132323);
    
    protected final ElementProducer<E> elementProducer;
    
    protected final C collection;
    
    protected final int elementCount;
    
    protected final int[] permutation;
    
    protected final E[] containedElements;
    
    protected final E[] notContainedElements;
    
    protected final Class<E> type;
    
    public AbstractCollectionBench(@DivideBy int elementCount, ElementProducer<E> elementProducer, Class<E> type) {
        this.elementProducer = elementProducer;
        this.elementCount = elementCount;
        this.type = type;
        
        containedElements = ArrayUtil.newArray(type, elementCount);
        notContainedElements = ArrayUtil.newArray(type, elementCount);
        
        CollectionBuilder<E, C> builder = newBuilder();
        for (int i = 0; i < elementCount; ++i) {
            E contained = elementProducer.produce();
            E notContained = elementProducer.produce();
            containedElements[i] = contained;
            notContainedElements[i] = notContained;
            builder.add(contained);
        }
        collection = builder.build();
        
        permutation = MathUtil.randomPermutation(collection.size(), random);
        
        Assert.assertEquals(elementCount, collection.size());
    }
    
    protected abstract CollectionBuilder<E, C> newBuilder();
    
    @Bench
    public int iterate() {
        int i = 0;
        for (E element: collection) {
            if (element == this) i++;
        }
        return i;
    }
    
}
