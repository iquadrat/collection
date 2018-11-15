package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.common.ReverseOrderedCollection;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableIndexedSet;
import org.povworld.collection.immutable.ImmutableIndexedSetImpl;
import org.povworld.collection.mutable.IndexedHashSet;
import org.povworld.collection.mutable.MutableCollections;

import test.org.povworld.collection.TestUtil;

/**
 * Unit tests for {@link org.povworld.collection.immutable.ImmutableSingletonOrderedCollection}.
 */
public class ImmutableSingletonUnOrderedCollectionTest {
    
    private ImmutableIndexedSet<String> collection;
    
    @Before
    public void setUp() {
        collection = ImmutableCollections.indexedSetOf("foo");
    }
    
    @Test
    public void isEmpty() {
        assertFalse(collection.isEmpty());
    }
    
    @Test
    public void getFirst() {
        assertEquals("foo", collection.getFirst());
        assertEquals("foo", collection.getFirstOrNull());
    }
    
    @Test
    public void getLast() {
        assertEquals("foo", collection.getLast());
        assertEquals("foo", collection.getLastOrNull());
    }
    
    @Test
    public void iterator() {
        ImmutableIndexedSet<String> iterated = TestUtil.verifyIterable(collection, ImmutableIndexedSetImpl.<String>newBuilder());
        assertEquals(iterated, MutableCollections.orderedSetOf("foo"));
    }
    
    @Test
    public void reverseIterator() {
        ImmutableIndexedSet<String> iterated = TestUtil.verifyIterable(new ReverseOrderedCollection<>(collection),
                ImmutableIndexedSetImpl.<String>newBuilder());
        assertEquals(iterated, MutableCollections.orderedSetOf("foo"));
    }
    
    @Test
    public void get() {
        assertEquals("foo", collection.get(0));
        
        try {
            collection.get(1);
            TestUtil.failExpected(IndexOutOfBoundsException.class);
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
        
        try {
            collection.get(-3);
            TestUtil.failExpected(IndexOutOfBoundsException.class);
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
    }
    
    @Test
    public void contains() {
        assertTrue(collection.contains("foo"));
        assertFalse(collection.contains("foobar"));
    }
    
    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void equalsAndHashCode() {
        IndexedHashSet<String> expectedEqual = MutableCollections.orderedSetOf("foo");
        ImmutableIndexedSet<String> duplicate = ImmutableCollections.indexedSetOf("foo");
        
        assertTrue(collection.equals(expectedEqual));
        assertFalse(collection.equals(null));
        assertFalse(collection.equals(new Object()));
        assertTrue(collection.equals(collection));
        assertTrue(collection.equals(duplicate));
        assertFalse(collection.equals(ImmutableCollections.indexedSetOf("bar")));
        
        assertTrue(collection.hashCode() == collection.hashCode());
        assertTrue(collection.hashCode() == expectedEqual.hashCode());
        assertTrue(collection.hashCode() == duplicate.hashCode());
        assertEquals(new TestUtil.OrderedHasher().hash(collection), collection.hashCode());
    }
    
}
