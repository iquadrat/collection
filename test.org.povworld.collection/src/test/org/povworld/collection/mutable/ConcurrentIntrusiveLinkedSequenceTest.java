package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ConcurrentIntrusiveLinkedSequence;
import org.povworld.collection.mutable.ConcurrentIntrusiveLinkedSequence.ElementLink;

import test.org.povworld.collection.TestUtil;

/**
 * Unit tests for {@link ConcurrentIntrusiveLinkedSequence}.
 */
public class ConcurrentIntrusiveLinkedSequenceTest extends
        AbstractIntrusiveLinkedSequenceTest<ConcurrentIntrusiveLinkedSequenceTest.StringLink, ConcurrentIntrusiveLinkedSequence<ConcurrentIntrusiveLinkedSequenceTest.StringLink>> {
    
    private static final int INSERTION_COUNT = 10000;
    
    public static class StringLink extends ConcurrentIntrusiveLinkedSequence.AbstractLink<StringLink> {
        
        private final int data;
        
        private final String string;
        
        public StringLink(int data, String string) {
            this.data = data;
            this.string = string;
        }
        
        @Override
        public int hashCode() {
            return 31 * data + string.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            StringLink other = ObjectUtil.castOrNull(obj, StringLink.class);
            if (other == null)
                return false;
            
            return (data == other.data) && string.equals(other.string);
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + data + "," + string + "]";
        }
        
    }
    
    public static class TestBuilder extends AbstractCollectionBuilder<StringLink, ConcurrentIntrusiveLinkedSequence<StringLink>>
            implements IntrusiveLinkedCollectionBuilder<StringLink, ConcurrentIntrusiveLinkedSequence<StringLink>> {
        
        private final CollectionBuilder<StringLink, ConcurrentIntrusiveLinkedSequence<StringLink>> delegate;
        
        public TestBuilder(
                CollectionBuilder<StringLink, ConcurrentIntrusiveLinkedSequence<StringLink>> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        protected void _add(StringLink element) {
            StringLink clone = new StringLink(element.data, element.string);
            delegate.add(clone);
        }
        
        @Override
        protected ConcurrentIntrusiveLinkedSequence<StringLink> _createCollection() {
            return delegate.build();
        }
        
        @Override
        protected void _reset() {
            delegate.reset();
        }
        
        @Override
        public StringLink createDetachedLinkWithSameValue(StringLink link) {
            return new StringLink(link.data, link.string);
        }
    }
    
    private static CollectionBuilder<StringLink, ConcurrentIntrusiveLinkedSequence<StringLink>> createBuilder() {
        return ConcurrentIntrusiveLinkedSequence.<StringLink>newBuilder();
    }
    
    public ConcurrentIntrusiveLinkedSequenceTest() throws CloneNotSupportedException {
        super(new TestBuilder(createBuilder()),
                createManyElements());
    }
    
    private static StringLink[] createManyElements() {
        StringLink[] array = new StringLink[1004];
        for (int i = 0; i < array.length; ++i) {
            array[i] = new StringLink(i, String.valueOf(i));
        }
        return array;
    }
    
    @Override
    protected Iterator<StringLink> modifyingIterator(ConcurrentIntrusiveLinkedSequence<StringLink> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void reinsertLinkIsDisallowed() {
        collection.remove(link3);
        try {
            collection.insertFront(link3);
            TestUtil.failExpected(IllegalStateException.class);
        } catch (IllegalStateException e) {
            // pass
        }
        
        StringLink tail = collection.removeTail();
        try {
            collection.insertFront(tail);
            TestUtil.failExpected(IllegalStateException.class);
        } catch (IllegalStateException e) {
            // pass
        }
    }
    
    @Test
    public void concurrentClearDuringIteration() {
        Iterator<StringLink> iterator = collection.iterator();
        assertEquals(link1, iterator.next());
        
        // Clear must be reflected immediately in the iterator.
        collection.clear();
        assertFalse(iterator.hasNext());
    }
    
    @Test
    public void elementLink() {
        ElementLink<String> link = new ConcurrentIntrusiveLinkedSequence.ElementLink<String>("foo");
        assertEquals("foo", link.getValue());
    }
    
    @Override
    @Test
    public void concurrentModificationDuringIteration() {
        Iterator<StringLink> iterator = collection.iterator();
        assertEquals(link1, iterator.next());
        
        collection.remove(link2);
        
        // has next only called after link2 already removed, so link2 must be skipped in the iteration
        assertTrue(iterator.hasNext());
        assertEquals(link3, iterator.next());
        
        assertEquals(ImmutableCollections.listOf(link1, link3), collection);
        
        iterator = collection.iterator();
        
        // has next called before link1 has been removed, so link1 must be present in the iteration
        assertTrue(iterator.hasNext());
        collection.remove(link1);
        collection.remove(link3);
        
        assertEquals(link1, iterator.next());
        assertFalse(iterator.hasNext());
    }
    
    @Override
    @Test
    public void concurrentModificationDuringReverseIteration() {
        Iterator<StringLink> iterator = collection.reverseIterator();
        assertEquals(link3, iterator.next());
        
        collection.remove(link2);
        
        // has next only called after link2 already removed, so link2 must be skipped in the iteration
        assertTrue(iterator.hasNext());
        assertEquals(link1, iterator.next());
        
        assertEquals(ImmutableCollections.listOf(link1, link3), collection);
        
        iterator = collection.reverseIterator();
        
        // has next called before link3 has been removed, so link3 must be present in the iteration
        assertTrue(iterator.hasNext());
        collection.remove(link3);
        collection.remove(link1);
        
        assertEquals(link3, iterator.next());
        assertFalse(iterator.hasNext());
    }
    
    @Override
    @Test
    public void concurrentRemoveDuringIteration() {
        Iterator<StringLink> iterator = collection.modifyingIterator();
        assertEquals(link1, iterator.next());
        
        collection.remove(link1);
        
        // does nothing as link1 is already removed
        iterator.remove();
        
        assertEquals(ImmutableCollections.listOf(link2, link3), collection);
    }
    
    @Override
    @Test
    public void concurrentRemoveDuringReverseIteration() {
        Iterator<StringLink> iterator = collection.modifyingReverseIterator();
        assertEquals(link3, iterator.next());
        
        collection.remove(link3);
        
        // does nothing as link1 is already removed
        iterator.remove();
        
        assertEquals(ImmutableCollections.listOf(link1, link2), collection);
    }
    
    final AtomicBoolean terminate = new AtomicBoolean(false);
    
    final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
    
    @Test
    public void concurrentIterateInsertRemove() throws Exception {
        
        final CountDownLatch startLatch = new CountDownLatch(3);
        
        final CountDownLatch stopLatch = new CountDownLatch(3);
        
        collection.clear();
        
        new Thread("Iterator") {
            
            @Override
            public void run() {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    
                    while (!terminate.get()) {
                        Iterator<StringLink> iterator = collection.iterator();
                        int last = Integer.MIN_VALUE;
                        while (iterator.hasNext()) {
                            StringLink current = iterator.next();
                            int value = current.data;
                            assertTrue(value + " <= " + last, value > last);
                            last = value;
                        }
                        
                    }
                    
                } catch (Throwable t) {
                    t.printStackTrace();
                    error.set(t);
                    terminate.set(true);
                } finally {
                    stopLatch.countDown();
                }
            };
            
        }.start();
        
        new Thread("Inserter") {
            
            @Override
            public void run() {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    
                    for (int i = 1; i < INSERTION_COUNT; ++i) {
                        if (terminate.get()) break;
                        collection.insertBack(new StringLink(i, ""));
                        collection.insertFront(new StringLink(-i, ""));
                        while (collection.size() > 5000) {
                            Thread.sleep(50);
                        }
                    }
                    
                } catch (Throwable t) {
                    t.printStackTrace();
                    error.set(t);
                } finally {
                    stopLatch.countDown();
                    terminate.set(true);
                }
            };
            
        }.start();
        
        new Thread("Remover") {
            
            @Override
            public void run() {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    
                    while (!terminate.get()) {
                        Iterator<StringLink> iterator = collection.modifyingReverseIterator();
                        while (iterator.hasNext()) {
                            iterator.next();
                            if ((collection.size() > 1000) && (Math.random() > 0.8)) {
                                iterator.remove();
                            }
                        }
                    }
                    
                } catch (Throwable t) {
                    t.printStackTrace();
                    terminate.set(true);
                } finally {
                    stopLatch.countDown();
                }
            };
            
        }.start();
        
        stopLatch.await();
        checkForError();
        
        assertEquals(CollectionUtil.sizeOf(collection), collection.size());
    }
    
    private void checkForError() {
        if (error.get() != null) {
            Assert.fail(error.get());
        }
    }
}
