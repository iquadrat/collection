package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.immutable.ImmutableArrayList;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableList;
import org.povworld.collection.mutable.AbstractIntrusiveLinkedSequence;

import test.org.povworld.collection.AbstractCollectionTest;
import test.org.povworld.collection.TestUtil;

/**
 * Test base for subclasses of {@link AbstractIntrusiveLinkedSequence}.
 */
public abstract class AbstractIntrusiveLinkedSequenceTest<L extends AbstractIntrusiveLinkedSequence.AbstractLink<L>, C extends AbstractIntrusiveLinkedSequence<L>>
        extends AbstractCollectionTest<L, C> {
    
    /**
     * {@link CollectionBuilder} that allows to clone links.
     */
    protected static interface IntrusiveLinkedCollectionBuilder<L extends AbstractIntrusiveLinkedSequence.AbstractLink<L>, C extends AbstractIntrusiveLinkedSequence<L>>
            extends CollectionBuilder<L, C> {
        
        public L createDetachedLinkWithSameValue(L link);
        
    }
    
    protected final IntrusiveLinkedCollectionBuilder<L, C> builder;
    
    protected final C collection;
    
    protected final L link1;
    
    protected final L link2;
    
    protected final L link3;
    
    public AbstractIntrusiveLinkedSequenceTest(IntrusiveLinkedCollectionBuilder<L, C> builder, L[] elements) {
        super(builder, elements);
        this.builder = builder;
        collection = builder.build();
        builder.reset();
        link1 = builder.createDetachedLinkWithSameValue(elements[0]);
        link2 = builder.createDetachedLinkWithSameValue(elements[1]);
        link3 = builder.createDetachedLinkWithSameValue(elements[2]);
        collection.insertBack(link1);
        collection.insertBack(link2);
        collection.insertBack(link3);
    }
    
    @Override
    protected Iterable<L> expectedOrder(Iterable<L> elements) {
        return elements;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Test
    public void insertFront() {
        C list = builder.build();
        list.insertFront(builder.createDetachedLinkWithSameValue(link3));
        list.insertFront(builder.createDetachedLinkWithSameValue(link2));
        list.insertFront(builder.createDetachedLinkWithSameValue(link1));
        assertEquals(list, collection);
    }
    
    @Test
    public void removeHead() {
        assertSame(link1, collection.removeHead());
        assertEquals(ImmutableCollections.listOf(link2, link3), collection);
        
        assertSame(link2, collection.removeHead());
        assertEquals(ImmutableCollections.listOf(link3), collection);
        
        assertSame(link3, collection.removeHead());
        assertEquals(ImmutableCollections.listOf(), collection);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeHeadFromEmpty() {
        collectionEmpty.removeHead();
    }
    
    @Test
    public void removeTail() {
        assertSame(link3, collection.removeTail());
        assertEquals(ImmutableCollections.listOf(link1, link2), collection);
        
        assertSame(link2, collection.removeTail());
        assertEquals(ImmutableCollections.listOf(link1), collection);
        
        assertSame(link1, collection.removeTail());
        assertEquals(ImmutableCollections.listOf(), collection);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeTailFromEmpty() {
        collectionEmpty.removeTail();
    }
    
    @Test
    public void remove() {
        assertTrue(collection.remove(link2));
        assertFalse(collection.remove(link2));
        assertEquals(ImmutableCollections.listOf(link1, link3), collection);
        
        assertTrue(collection.remove(link1));
        assertEquals(ImmutableCollections.listOf(link3), collection);
        
        assertTrue(collection.remove(link3));
        assertEquals(0, collection.size());
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void getLastOrNull() {
        assertSame(link3, collection.getLastOrNull());
        C empty = builder.reset().build();
        assertNull(empty.getLastOrNull());
    }
    
    @Test
    public void modifyingReverseIterator() {
        CollectionBuilder<L, ? extends ImmutableList<L>> actualBuilder = ImmutableArrayList.<L>newBuilder(3);
        
        Iterable<L> iterableInTest = new Iterable<L>() {
            
            @Override
            public Iterator<L> iterator() {
                return collection.modifyingReverseIterator();
            }
            
        };
        
        ImmutableList<L> actual = TestUtil.verifyIterable(iterableInTest, actualBuilder);
        assertEquals(ImmutableCollections.listOf(link3, link2, link1), actual);
        
        TestUtil.verifyRemoveByIterator(iterableInTest);
    }
    
    @Test
    public void reverseIteratorNoRemove() {
        TestUtil.verifyIteratableRemoveUnsupported(new Iterable<L>() {
            @Override
            public Iterator<L> iterator() {
                return collectionThree.reverseIterator();
            }
        });
    }
    
    @Test
    public void concurrentModificationDuringIteration() {
        Iterator<L> iterator = collection.iterator();
        assertEquals(link1, iterator.next());
        
        collection.remove(link2);
        
        try {
            iterator.next();
            TestUtil.failExpected(ConcurrentModificationException.class);
        } catch (ConcurrentModificationException e) {
            // pass
        }
        
        assertEquals(ImmutableCollections.listOf(link1, link3), collection);
    }
    
    @Test
    public void concurrentModificationDuringReverseIteration() {
        Iterator<L> iterator = collection.reverseIterator();
        assertEquals(link3, iterator.next());
        
        collection.remove(link2);
        
        try {
            iterator.next();
            TestUtil.failExpected(ConcurrentModificationException.class);
        } catch (ConcurrentModificationException e) {
            // pass
        }
        
        assertEquals(ImmutableCollections.listOf(link1, link3), collection);
    }
    
    @Test
    public void concurrentRemoveDuringIteration() {
        Iterator<L> iterator = collection.modifyingIterator();
        assertEquals(link1, iterator.next());
        
        collection.remove(link1);
        
        try {
            iterator.remove();
            TestUtil.failExpected(ConcurrentModificationException.class);
        } catch (ConcurrentModificationException e) {
            // pass
        }
        
        assertEquals(ImmutableCollections.listOf(link2, link3), collection);
    }
    
    @Test
    public void concurrentRemoveDuringReverseIteration() {
        Iterator<L> iterator = collection.modifyingReverseIterator();
        assertEquals(link3, iterator.next());
        
        collection.remove(link3);
        
        try {
            iterator.remove();
            TestUtil.failExpected(ConcurrentModificationException.class);
        } catch (ConcurrentModificationException e) {
            // pass
        }
        
        assertEquals(ImmutableCollections.listOf(link1, link2), collection);
    }
    
}
