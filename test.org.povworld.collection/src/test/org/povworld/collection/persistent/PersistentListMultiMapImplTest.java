package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentListMultiMap;
import org.povworld.collection.persistent.PersistentListMultiMapImpl;

import test.org.povworld.collection.AbstractMultiMapTest;

/**
 * Unit tests for {@link PersistentListMultiMapImpl}.
 *
 */
public class PersistentListMultiMapImplTest extends AbstractMultiMapTest<List<Integer>, PersistentListMultiMap<String, Integer>> {
    
    private PersistentListMultiMap<String, Integer> map;
    
    public PersistentListMultiMapImplTest() {
        map = create();
    }
    
    @Override
    protected PersistentListMultiMap<String, Integer> create() {
        return PersistentListMultiMapImpl.empty();
    }
    
    @Override
    protected PersistentListMultiMap<String, Integer> set(PersistentListMultiMap<String, Integer> map, String key, Collection<Integer> values) {
        return map.without(key).withAll(key, values);
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        assertEquals(ImmutableCollections.asIndexedSet(expectedValues), actualValues);
    }
    
    @Test
    public void numberOfValues() {
        assertEquals(0, mapEmpty.numberOfValues("foo"));
        assertEquals(3, mapSingleKey.numberOfValues("abc"));
        assertEquals(2, mapThreeKeys.numberOfValues("two"));
        assertEquals(0, mapThreeKeys.numberOfValues("four"));
        assertEquals(42, mapLarge.numberOfValues("42"));
    }
    
    @Test
    public void withAtEnd() {
        map = map.withAtEnd("foo", 1);
        map = map.withAtEnd("foo", 2);
        map = map.withAtEnd("foo", 3);
        assertEquals(ImmutableCollections.listOf(1, 2, 3), map.get("foo"));
    }
    
    @Test
    public void withAtBegin() {
        map = map.withAtBegin("foo", 1);
        map = map.withAtBegin("foo", 2);
        map = map.withAtBegin("foo", 3);
        assertEquals(ImmutableCollections.listOf(3, 2, 1), map.get("foo"));
    }
    
    @Test
    public void withAfter() {
        map = map.withAtBegin("foo", 2);
        map = map.withAfter("foo", 2, 2);
        map = map.withAfter("foo", 3, 2);
        map = map.withAfter("foo", 4, 3);
        assertEquals(ImmutableCollections.listOf(2, 3, 4, 2), map.get("foo"));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void withAfterInexisting() {
        map = map.withAtBegin("foo", 2);
        map = map.withAtBegin("foo", 4);
        map.withAfter("foo", 2, 3);
    }
    
    @Test
    public void withBefore() {
        map = map.withAtBegin("foo", 2);
        map = map.withBefore("foo", 2, 2);
        map = map.withBefore("foo", 3, 2);
        map = map.withBefore("foo", 4, 3);
        assertEquals(ImmutableCollections.listOf(4, 3, 2, 2), map.get("foo"));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void withBeforeInexisting() {
        map = map.withAtBegin("foo", 2);
        map = map.withAtBegin("foo", 4);
        map.withBefore("foo", 2, 3);
    }
    
    @Test
    public void withAll() {
        map = map.withAll("foo", ImmutableCollections.<Integer>listOf());
        assertFalse(map.containsKey("foo"));
        
        map = map.withAll("bar", ImmutableCollections.listOf(1, 2, 3));
        map = map.withAll("bar", ImmutableCollections.listOf(4, 5, 6));
        assertEquals(ImmutableCollections.listOf(1, 2, 3, 4, 5, 6), map.get("bar"));
    }
    
    @Test
    public void withoutKeyValue() {
        assertSame(mapEmpty, mapEmpty.without("three", 174));
        assertSame(mapThreeKeys, mapThreeKeys.without("three", 174));
        assertSame(mapThreeKeys, mapThreeKeys.without("four", 174));
        
        assertEquals(mapEmpty, mapEmpty.withAtEnd("foo", 2).without("foo", 2));
        assertEquals(mapEmpty, mapSingleKey.without("abc", 2).without("abc", 1).without("abc", 3));
        
        map = mapThreeKeys.without("one", 2);
        assertFalse(map.containsKey("one"));
        
        map = map.without("three", 5);
        assertTrue(map.containsKey("three"));
        assertEquals(ImmutableCollections.listOf(2, 3), map.get("three"));
        
        map = map.without("three", 2);
        map = map.without("three", 3);
        assertFalse(map.containsKey("three"));
        
        map = map.without("two", 2);
        assertEquals(ImmutableCollections.listOf(3), map.get("two"));
        
        assertSame(mapSingleKey, mapSingleKey.without("abc", 42));
        assertSame(mapSingleKey, mapSingleKey.without("bla", 1));
        map = mapSingleKey.without("abc", 2);
        assertEquals(ImmutableCollections.listOf(1, 3), map.get("abc"));
        assertEquals(mapEmpty, map.without("abc", 1).without("abc", 3));
        
        assertSame(mapSingleEntry, mapSingleEntry.without("one", 13));
        assertSame(mapSingleEntry, mapSingleEntry.without("bar", 13));
        assertEquals(mapEmpty, mapSingleEntry.without("one", 2));
        
        assertSame(mapLarge, mapLarge.without("foo", 4));
        assertSame(mapLarge, mapLarge.without("10", 141));
        map = mapLarge.without("10", 7);
        assertFalse(CollectionUtil.contains(map.get("10"), 7));
        assertTrue(CollectionUtil.contains(map.get("10"), 3));
        
        map = map.without("1", 1);
        assertFalse(map.containsKey("1"));
    }
    
    @Test
    public void without() {
        assertSame(mapEmpty, mapEmpty.without("four"));
        assertSame(mapThreeKeys, mapThreeKeys.without("four"));
        map = mapThreeKeys.without("two");
        assertFalse(map.containsKey("two"));
        
        assertEquals(mapSingleKey, mapSingleKey.without("hello"));
        assertEquals(mapEmpty, mapSingleKey.without("abc"));
        
        assertSame(mapSingleEntry, mapSingleEntry.without("bar"));
        assertEquals(mapEmpty, mapSingleEntry.without("one"));
        
        assertSame(mapLarge, mapLarge.without("bar"));
        assertTrue(mapLarge.containsKey("10"));
        map = mapLarge.without("10");
        assertFalse(map.containsKey("10"));
    }
    
    @Test
    public void withoutAll() {
        assertSame(mapThreeKeys, mapThreeKeys.withoutAll("foo", ImmutableCollections.listOf(1, 1, 2)));
        assertSame(mapThreeKeys, mapThreeKeys.withoutAll("three", ImmutableCollections.listOf(7, 8, 9)));
        
        map = mapThreeKeys.withoutAll("one", ImmutableCollections.listOf(2, 3));
        assertFalse(map.containsKey("one"));
        
        map = map.withoutAll("two", ImmutableCollections.listOf(1, 2));
        assertEquals(ImmutableCollections.listOf(3), map.get("two"));
        
        assertEquals(mapEmpty, mapSingleKey.withoutAll("abc", mapSingleKey.get("abc")));
        assertSame(mapLarge, mapLarge.withoutAll("1", ImmutableCollections.<Integer>listOf()));
    }
    
    @Test
    public void withReplacement() {
        map = map.withAll("x", ImmutableCollections.listOf(1, 2, 3, 2, 1));
        
        assertSame(map, map.withReplacement("y", 3, 4));
        assertSame(map, map.withReplacement("x", 13, 4));
        map = map.withReplacement("x", 3, 1);
        assertEquals(ImmutableCollections.listOf(1, 2, 1, 2, 1), map.get("x"));
        map = map.withReplacement("x", 1, 2);
        assertEquals(ImmutableCollections.listOf(2, 2, 2, 2, 2), map.get("x"));
        
        Integer A = new Integer(2);
        Integer B = new Integer(2);
        assertNotSame(A, B);
        
        map = map.withAtEnd("y", A);
        
        map = map.withReplacement("y", A, B);
        assertSame(B, map.get("y").getFirst());
    }
    
    @Test
    public void cleared() {
        assertSame(mapEmpty, mapEmpty.cleared());
        assertEquals(mapEmpty, mapThreeKeys.cleared());
    }
    
}
