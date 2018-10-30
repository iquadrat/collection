package bench.org.povworld.collection.mutable;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.TreeSet;

import bench.org.povworld.collection.ElementProducer;

public abstract class AbstractTreeSetBench<E extends Comparable<E>> {
    
    private final Class<E> clazz;
    
    private final Random random = new Random(1111);
    
    private final TreeSet<E> set;
    
    private final List<E> elements;
    
    private final List<E> nonElements;
    
    public AbstractTreeSetBench(Class<E> clazz, @DivideBy int elementCount, ElementProducer<E> elementProducer) {
        this.clazz = clazz;
        ArrayList.Builder<E> nonElementBuilder = ArrayList.newBuilder();
        TreeSet<E> set = empty();
        for (int i = 0; i < elementCount; ++i) {
            E element = elementProducer.produce();
            set.add(element);
            nonElementBuilder.add(elementProducer.produce());
        }
        this.set = set;
        this.elements = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(set), random));
        this.nonElements = ImmutableCollections.asList(CollectionUtil.shuffle(nonElementBuilder.build(), random));
    }
    
    protected TreeSet<E> empty() {
        return TreeSet.create(clazz);
    }
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        TreeSet<E> result = empty();
        for (E element: set) {
            result.add(element);
        }
//    Thread.sleep(10000);
        return result;
    }
    
    @Bench
//  @Ignore
    public Object getContainedElement() {
        for (E element: elements) {
            if (!set.contains(element)) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object getNotContainedKey() {
        for (E nonElement: nonElements) {
            if (set.contains(nonElement)) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object addExistingElement() {
        for (E element: elements) {
            if (set.add(element)) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object addNewElement() {
        TreeSet<E> newSet = empty();
        for (E element: elements) {
            newSet.add(element);
        }
        return newSet;
    }
    
}
