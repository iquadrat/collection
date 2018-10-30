package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.annotations.SingleRun;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.mutable.ConcurrentIntrusiveLinkedSequence;

import bench.org.povworld.collection.ElementProducer;
import bench.org.povworld.collection.mutable.ConcurrentIntrusiveSequenceBench.Link;

/**
 * Benchmarks for {@link ConcurrentIntrusiveLinkedSequence}.
 */
public class ConcurrentIntrusiveSequenceBench extends AbstractCollectionBench<Link, ConcurrentIntrusiveLinkedSequence<Link>> {
    
    static class Link extends ConcurrentIntrusiveLinkedSequence.AbstractLink<Link> {
        
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
    
    public ConcurrentIntrusiveSequenceBench(@DivideBy @ForEachInt({1000, 10000, 100000, 1000000}) int elementCount) {
        super(elementCount, LINK_PRODUCER, Link.class);
    }
    
    @Override
    protected CollectionBuilder<Link, ConcurrentIntrusiveLinkedSequence<Link>> newBuilder() {
        return ConcurrentIntrusiveLinkedSequence.newBuilder();
    }
    
    @Bench
    @SingleRun
    public void insertFront() {
        for (int i = 0; i < elementCount; ++i) {
            collection.insertFront(notContainedElements[i]);
        }
    }
    
    @Bench
    @SingleRun
    public void insertBack() {
        for (int i = 0; i < elementCount; ++i) {
            collection.insertBack(notContainedElements[i]);
        }
    }
    
    @Bench
    @SingleRun
    public void removeHead() {
        for (int i = 0; i < elementCount; ++i) {
            collection.removeHead();
        }
    }
    
    @Bench
    @SingleRun
    public void rotateTail() {
        for (int i = 0; i < elementCount; ++i) {
            collection.removeTail();
        }
    }
    
    @Bench
    @SingleRun
    public void removeRandom() {
        for (int i = 0; i < elementCount; ++i) {
            collection.remove(containedElements[permutation[i]]);
        }
    }
    
}
