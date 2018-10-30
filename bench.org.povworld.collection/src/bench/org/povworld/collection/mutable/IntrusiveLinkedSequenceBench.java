package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.mutable.IntrusiveLinkedSequence;

import bench.org.povworld.collection.ElementProducer;
import bench.org.povworld.collection.mutable.IntrusiveLinkedSequenceBench.Link;

/**
 * Benchmarks for {@link IntrusiveLinkedSequence}.
 */
public class IntrusiveLinkedSequenceBench extends AbstractCollectionBench<Link, IntrusiveLinkedSequence<Link>> {
    
    static class Link extends IntrusiveLinkedSequence.AbstractLink<Link> {
        
    }
    
    private static final ElementProducer<Link> LINK_PRODUCER;
    static {
        LINK_PRODUCER = new ElementProducer<Link>() {
            @Override
            public Link produce() {
                return new Link();
            }
        };
    }
    
    public IntrusiveLinkedSequenceBench(@DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000, 1000000}) int elementCount) {
        super(elementCount, LINK_PRODUCER, Link.class);
    }
    
    @Override
    protected CollectionBuilder<Link, IntrusiveLinkedSequence<Link>> newBuilder() {
        return IntrusiveLinkedSequence.newBuilder();
    }
    
    @Bench
    public void removeInsertFront() {
        for (int i = 0; i < elementCount; ++i) {
            collection.remove(containedElements[i]);
        }
        for (int i = 0; i < elementCount; ++i) {
            collection.insertFront(containedElements[i]);
        }
    }
    
    @Bench
    public void insertBackRemove() {
        for (int i = 0; i < elementCount; ++i) {
            collection.insertBack(notContainedElements[i]);
        }
        for (int i = 0; i < elementCount; ++i) {
            collection.remove(notContainedElements[i]);
        }
    }
    
    @Bench
    public void rotateHead() {
        for (int i = 0; i < elementCount; ++i) {
            Link link = collection.removeHead();
            collection.insertBack(link);
        }
    }
    
    @Bench
    public void rotateTail() {
        for (int i = 0; i < elementCount; ++i) {
            Link link = collection.removeTail();
            collection.insertFront(link);
        }
    }
    
    @Bench
    public void removeRandomAddOrdered() {
        for (int i = 0; i < elementCount; ++i) {
            collection.remove(containedElements[permutation[i]]);
        }
        for (int i = 0; i < elementCount; ++i) {
            collection.insertBack(containedElements[i]);
        }
    }
    
    @Bench
    public void moveToFrontOrdered() {
        for (int i = elementCount - 1; i >= 0; --i) {
            collection.moveToFront(containedElements[i]);
        }
    }
    
    @Bench
    public void moveToFrontRandom() {
        for (int i = 0; i < elementCount; ++i) {
            collection.moveToFront(containedElements[permutation[i]]);
        }
        for (int i = elementCount - 1; i >= 0; --i) {
            collection.moveToFront(containedElements[i]);
        }
    }
    
}
