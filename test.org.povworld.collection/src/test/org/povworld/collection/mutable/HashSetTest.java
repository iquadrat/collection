package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Random;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.HashSet;
import org.povworld.collection.mutable.MutableCollections;

import test.org.povworld.collection.AbstractSetTest;

public class HashSetTest extends AbstractSetTest<HashSet<String>> {
    
    public HashSetTest() {
        super(HashSet.<String>newBuilder());
    }
    
    @Override
    protected Iterator<String> modifyingIterator(HashSet<String> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void remove() {
        assertFalse(collectionEmpty.remove("foo"));
        
        assertFalse(collectionSingle.remove(one));
        assertTrue(collectionSingle.remove(zero));
        assertTrue(collectionSingle.isEmpty());
        assertFalse(collectionSingle.contains(zero));
        assertFalse(collectionSingle.remove(zero));
        
        assertFalse(collectionThree.remove(zero));
        assertTrue(collectionThree.remove(two));
        assertEquals(2, collectionThree.size());
        assertFalse(collectionThree.contains(two));
        assertTrue(collectionThree.contains(one));
        
        Iterable<String> removeOrder = CollectionUtil.shuffle(MutableCollections.listOf(manyElements), new Random(1));
        int expectedSize = collectionLarge.size();
        for (String element: removeOrder) {
            assertTrue(collectionLarge.contains(element));
            collectionLarge.remove(element);
            assertFalse(collectionLarge.contains(element));
            assertEquals(--expectedSize, collectionLarge.size());
        }
    }
    
    @Test
    public void clear() {
        collectionEmpty.clear();
        assertTrue(collectionEmpty.isEmpty());
        
        collectionLarge.clear();
        assertEquals(collectionEmpty, collectionLarge);
    }
    
    @Test
    public void getCurrentOrNull() {
        String a = new String("a");
        String b = new String("b");
        HashSet<String> set = HashSet.<String>newBuilder().addAll(CollectionUtil.wrap(a, b)).build();
        assertSame(a, set.getCurrentOrNull(a));
        assertNull(set.getCurrentOrNull("foo"));
        assertSame(b, set.getCurrentOrNull(new String("b")));
    }
    
    @Test
    public void removeAndReturnRemoved() {
        String a = new String("a");
        String b = new String("b");
        HashSet<String> set = HashSet.<String>newBuilder().addAll(CollectionUtil.wrap(a, b)).build();
        
        assertNull(set.removeAndReturnRemoved("fo"));
        assertSame(a, set.removeAndReturnRemoved(new String("a")));
        assertSame(b, set.removeAndReturnRemoved(b));
    }
    
    @Test
    public void removeAll() {
        assertEquals(0, collectionThree.removeAll(ImmutableCollections.<String>listOf()));
        assertEquals(2, collectionThree.removeAll(ImmutableCollections.listOf("one", "ONE", "two", "one")));
        assertEquals(ImmutableCollections.setOf("three"), collectionThree);
        
        assertEquals(1000, collectionLarge.removeAll(ImmutableCollections.asList(collectionLarge)));
        assertEquals(collectionEmpty, collectionLarge);
    }
    
}
