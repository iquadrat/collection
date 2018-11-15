package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.ListMultiMap;
import org.povworld.collection.MultiMap;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.MutableCollections;

public class MutableCollectionsTest {
    
    @Test
    public void createEmpty() {
        assertEquals(ImmutableCollections.indexedSetOf(), MutableCollections.indexedSetOf());
        assertEquals(ImmutableCollections.orderedSetOf(), MutableCollections.orderedSetOf());
        assertEquals(ImmutableCollections.listOf(), MutableCollections.listOf());
        assertEquals(ImmutableCollections.setOf(), MutableCollections.setOf());
    }
    
    @Test
    public void createSingleton() {
        String e = "foobar";
        assertEquals(ImmutableCollections.indexedSetOf(e), MutableCollections.indexedSetOf(e));
        assertEquals(ImmutableCollections.orderedSetOf(e), MutableCollections.orderedSetOf(e));
        assertEquals(ImmutableCollections.listOf(e), MutableCollections.listOf(e));
        assertEquals(ImmutableCollections.setOf(e), MutableCollections.setOf(e));
    }
    
    
    @Test
    public void createMultiMapSingleton() {
        MultiMap<String, String> map = MutableCollections.multiMapOf("foo", "bar");
        assertEquals(1, map.keyCount());
        assertEquals(ImmutableCollections.setOf("bar"), map.get("foo"));
        
        map = MutableCollections.multiMapOf("foo", ImmutableCollections.listOf("one", "two", "three"));
        assertEquals(1, map.keyCount());
        assertEquals(ImmutableCollections.setOf("one", "two", "three"), map.get("foo"));
    }
    
    @Test
    public void createListMultiMapSingleton() {
        ListMultiMap<String, String> map = MutableCollections.listMultiMapOf("foo", "bar");
        assertEquals(1, map.keyCount());
        assertEquals(ImmutableCollections.listOf("bar"), map.get("foo"));
        
        map = MutableCollections.listMultiMapOf("foo", ImmutableCollections.listOf("one", "two", "three"));
        assertEquals(1, map.keyCount());
        assertEquals(ImmutableCollections.listOf("one", "two", "three"), map.get("foo"));
    }
    
    @Test
    public void createFromArray() {
        String[] e = new String[] {"one", "two", "three", "go!"};
        assertEquals(ImmutableCollections.<String>indexedSetOf(e), MutableCollections.<String>indexedSetOf(e));
        assertEquals(ImmutableCollections.<String>orderedSetOf(e), MutableCollections.<String>orderedSetOf(e));
        assertEquals(ImmutableCollections.<String>listOf(e), MutableCollections.<String>listOf(e));
        assertEquals(ImmutableCollections.<String>setOf(e), MutableCollections.<String>setOf(e));
    }
    
    @Test
    public void createFromIterable() {
        Iterable<String> e = CollectionUtil.wrap("one", "two", "three", "go!");
        assertEquals(ImmutableCollections.<String>asIndexedSet(e), MutableCollections.<String>asIndexedSet(e));
        assertEquals(ImmutableCollections.<String>asOrderedSet(e), MutableCollections.<String>asOrderedSet(e));
        assertEquals(ImmutableCollections.<String>asList(e), MutableCollections.<String>asList(e));
        assertEquals(ImmutableCollections.<String>asSet(e), MutableCollections.<String>asSet(e));
    }
    
}
