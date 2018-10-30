package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.Ignore;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentOrderedSet;

import bench.org.povworld.collection.ElementProducer;

public abstract class PersistentOrderedSetBench<E> {
    
    protected final Random random = new Random(1111);
    
    protected final PersistentOrderedSet<E> set;
    
    protected final List<E> elements;
    
    protected final List<E> nonElements;
    
    public PersistentOrderedSetBench(@DivideBy int elementCount, ElementProducer<E> elementProducer) {
        ArrayList.Builder<E> nonElementBuilder = ArrayList.newBuilder();
        PersistentOrderedSet<E> set = empty();
        for (int i = 0; i < elementCount; ++i) {
            E element = elementProducer.produce();
            set = set.with(element);
            nonElementBuilder.add(elementProducer.produce());
        }
        this.set = set;
        this.elements = ImmutableCollections.asList(CollectionUtil.shuffle(ImmutableCollections.asList(set), random));
        this.nonElements = ImmutableCollections.asList(CollectionUtil.shuffle(nonElementBuilder.build(), random));
    }
    
    protected abstract PersistentOrderedSet<E> empty();
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        PersistentOrderedSet<E> result = empty().withAll(set);
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
    public Object addNewElement() {
        for (E nonElement: nonElements) {
            if (set.with(nonElement) == null) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object addAllNewElement() {
        E[] singleton = ArrayUtil.unsafeCastedNewArray(1);
        Collection<E> newElement = ImmutableCollections.listOf(singleton);
        for (E nonElement: nonElements) {
            singleton[0] = nonElement;
            if (set.withAll(newElement) == null) return null;
        }
        return this;
    }
    
    @Bench
    @Ignore
    public Object removeContainedElement() {
        for (E element: elements) {
            if (set.without(element) == null) return null;
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object duplicate() {
        return set.cleared().withAll(set);
    }
    
}
