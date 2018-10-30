package bench.org.povworld.collection.mutable;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;

import bench.org.povworld.collection.ElementProducer;

public abstract class AbstractHashSetBench<E> {
    
    private final Random random = new Random(1111);
    
    private final HashSet<E> set;
    
    private final List<E> elements;
    
    private final List<E> nonElements;
    
    public AbstractHashSetBench(@DivideBy int elementCount, ElementProducer<E> elementProducer) {
        ArrayList.Builder<E> nonElementBuilder = ArrayList.newBuilder();
        HashSet<E> set = empty();
        for (int i = 0; i < elementCount; ++i) {
            E element = elementProducer.produce();
            set.add(element);
            nonElementBuilder.add(elementProducer.produce());
        }
        this.set = set;
        this.elements = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(set), random));
        this.nonElements = ImmutableCollections.asList(CollectionUtil.shuffle(nonElementBuilder.build(), random));
    }
    
    protected HashSet<E> empty() {
        return new HashSet<E>();
    }
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        HashSet<E> result = new HashSet<E>(elements.size());
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
        HashSet<E> newSet = new HashSet<>(set.size());
        for (E element: elements) {
            newSet.add(element);
        }
        return newSet;
    }
    
}
