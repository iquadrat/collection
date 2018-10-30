package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableIndexedSet;
import org.povworld.collection.immutable.ImmutableIndexedSetImpl;
import org.povworld.collection.immutable.ImmutableSet;
import org.povworld.collection.mutable.HashSet;
import org.povworld.collection.mutable.MutableCollections;

import test.org.povworld.collection.TestUtil;

/**
 * Unit tests for {@link org.povworld.collection.immutable.ImmutableSingletonUnOrderedCollection}.
 */
public class ImmutableSingletonOrderedCollectionTest {
    
    private ImmutableSet<String> collection;
    
    @Before
    public void setUp() {
        collection = ImmutableCollections.setOf("foo");
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
    public void iterator() {
        ImmutableIndexedSet<String> iterated = TestUtil.verifyIterable(collection, ImmutableIndexedSetImpl.<String>newBuilder());
        assertEquals(iterated, MutableCollections.orderedSetOf("foo"));
    }
    
    @Test
    public void contains() {
        assertTrue(collection.contains("foo"));
        assertFalse(collection.contains("foobar"));
    }
    
    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void equalsAndHashCode() {
        HashSet<String> expectedEqual = MutableCollections.setOf("foo");
        ImmutableSet<String> duplicate = ImmutableCollections.setOf("foo");
        
        assertTrue(collection.equals(expectedEqual));
        assertFalse(collection.equals(null));
        assertFalse(collection.equals(new Object()));
        assertTrue(collection.equals(collection));
        assertTrue(collection.equals(duplicate));
        assertFalse(collection.equals(ImmutableCollections.indexedSetOf("bar")));
        
        assertTrue(collection.hashCode() == collection.hashCode());
        assertTrue(collection.hashCode() == expectedEqual.hashCode());
        assertTrue(collection.hashCode() == duplicate.hashCode());
        assertEquals(new TestUtil.UnOrderedHasher().hash(collection), collection.hashCode());
    }
}
