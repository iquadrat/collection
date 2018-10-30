package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.MemoryBench;
import org.jbenchx.annotations.Tags;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.common.MathUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentList;

import bench.org.povworld.collection.ElementProducer;

public abstract class PersistentListBench<E> {
    
    private final Random random = new Random(1111);
    
    private final PersistentList<E> list;
    
    private final E element;
    
    private final List<E> nonElements;
    
    private final int[] permutation;
    
    public PersistentListBench(@DivideBy int elementCount, ElementProducer<E> elementProducer) {
        element = elementProducer.produce();
        ArrayList.Builder<E> nonElementBuilder = ArrayList.newBuilder();
        PersistentList<E> list = empty();
        for (int i = 0; i < elementCount; ++i) {
            E element = elementProducer.produce();
            list = list.with(element);
            nonElementBuilder.add(elementProducer.produce());
        }
        this.list = list;
        //this.elements = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(set), random));
        this.nonElements = ImmutableCollections.asList(CollectionUtil.shuffle(nonElementBuilder.build(), random));
        this.permutation = MathUtil.randomPermutation(elementCount, random);
    }
    
    protected abstract PersistentList<E> empty();
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        PersistentList<E> result = empty().withAll(list);
        return result;
    }
    
    @Bench
//  @Ignore
    public Object getRandomIndex() {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(permutation[i]) == null) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object addAtEnd() {
        for (E nonElement: nonElements) {
            if (list.with(nonElement) == null) return null;
        }
        return this;
    }
    
    @Bench
    public Object addAtRandomIndex() {
        for (int i = 0; i < list.size(); ++i) {
            if (list.with(element, permutation[i]) == null) return null;
        }
        return this;
    }
    
    @Bench
    public Object setRandomIndex() {
        for (int i = 0; i < list.size(); ++i) {
            if (list.withReplacementAt(element, permutation[i]) == null) return null;
        }
        return this;
    }
    
    @Bench
    public Object removeRandomIndex() {
        for (int i = 0; i < list.size(); ++i) {
            if (list.without(permutation[i]) == null) return null;
        }
        return this;
    }
    
    @Bench
    @Tags("duplicate")
//  @Ignore
    public Object duplicate() {
        return list.cleared().withAll(list);
    }
    
}
