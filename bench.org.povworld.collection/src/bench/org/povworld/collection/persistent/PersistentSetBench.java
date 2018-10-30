package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.Ignore;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.common.Assert;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableList;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentSet;

import bench.org.povworld.collection.ElementProducer;

public abstract class PersistentSetBench<E> {
    
    private final Random random = new Random(1111);
    
    private final List<E> elements;
    
    private final List<E> nonElements;
    
    protected final Iterable<E> nonElementsIterable;
    
    protected final PersistentSet<E> set;
    
    private final boolean pertube;
    
    private final ArrayList<E> pertubeElements;
    
    public PersistentSetBench(@DivideBy int elementCount, ElementProducer<E> elementProducer) {
        this(elementCount, elementProducer, true);
    }
    
    public PersistentSetBench(@DivideBy int elementCount, ElementProducer<E> elementProducer, boolean pertube) {
        this.pertube = pertube;
        ArrayList.Builder<E> nonElementBuilder = ArrayList.newBuilder();
        CollectionBuilder<E, ? extends PersistentSet<E>> builder = newBuilder();
        for (int i = 0; i < elementCount; ++i) {
            E element = elementProducer.produce();
            builder.add(element);
            nonElementBuilder.add(elementProducer.produce());
        }
        if (pertube) {
            ArrayList.Builder<E> pertubeElementsBuilder = ArrayList.newBuilder();
            for (int i = 0; i < 100000; ++i) {
                pertubeElementsBuilder.add(elementProducer.produce());
            }
            pertubeElements = pertubeElementsBuilder.build();
            this.set = pertube(builder.build());
        } else {
            pertubeElements = null;
            this.set = builder.build();
        }
        ImmutableList<E> list = ImmutableCollections.asList(set);
        this.elements = ImmutableCollections.asList(CollectionUtil.shuffle(list, random));
        this.nonElements = ImmutableCollections.asList(CollectionUtil.shuffle(nonElementBuilder.build(), random));
        this.nonElementsIterable = ImmutableCollections.asList(nonElements);
    }
    
    private PersistentSet<E> pertube(PersistentSet<E> set) {
        for (int i = 0; i < 10; ++i) {
            set = set.withAll(CollectionUtil.shuffle(pertubeElements));
            set = set.withoutAll(CollectionUtil.shuffle(pertubeElements));
        }
        return set;
    }
    
    protected abstract CollectionBuilder<E, ? extends PersistentSet<E>> newBuilder();
    
    @MemoryBench
//  @Ignore
    public Object memory() throws InterruptedException {
        PersistentSet<E> result = newBuilder().addAll(set).build();
        if (pertube) {
            result = pertube(result);
        }
//    Thread.sleep(10000);
        return result;
    }
    
    @Bench
//  @Ignore
    public Object getContainedElement() {
        for (E element: elements) {
            if (!set.contains(element)) {
                Assert.fail("Element should be contained in the set: " + element);
            }
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object getNotContainedKey() {
        for (E nonElement: nonElements) {
            if (set.contains(nonElement)) {
                Assert.fail("Element should not be contained in the set: " + nonElement);
            }
        }
        return this;
    }
    
    @Bench
//  @Ignore
    public Object addNewElement() {
        for (E nonElement: nonElements) {
            if (set.with(nonElement) == set) {
                Assert.fail("Element was already contained in the set: " + nonElement);
            }
        }
        return this;
    }
    
    @Bench
    public Object removeElement() {
        for (E element: elements) {
            if (set.without(element) == set) {
                Assert.fail("Element was not contained in the set: " + element);
            }
        }
        return this;
    }
    
    @Bench
    public Object removeAllElement() {
        PersistentSet<E> empty = set.withoutAll(set);
        Assert.assertTrue(empty.isEmpty(), "Not an empty set!");
        return empty;
    }
    
    @Bench
    public Object _build() {
        return newBuilder().addAll(elements).build();
    }
    
    @Bench
//  @Ignore
    public Object addAllSelf() {
        return set.withAll(set);
    }
    
    @Bench
//  @Ignore
    public Object addAllMerge() {
        return set.withAll(nonElementsIterable);
    }
    
    @Ignore
    private void noBench() {}
    
}
