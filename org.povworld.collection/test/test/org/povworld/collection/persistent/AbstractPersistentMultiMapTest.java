package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentMultiMap;
import org.povworld.collection.persistent.PersistentSet;

import test.org.povworld.collection.AbstractMultiMapTest;
import test.org.povworld.collection.TestUtil;

/**
 * Test base for implementations of {@link PersistentMultiMap}.
 */
public abstract class AbstractPersistentMultiMapTest<M extends PersistentMultiMap<String, Integer>>
        extends AbstractMultiMapTest<Set<Integer>, M> {
    
    @Test
    public void numberOfValues() {
        assertEquals(0, mapEmpty.numberOfValues("foo"));
        assertEquals(3, mapSingleKey.numberOfValues("abc"));
        assertEquals(2, mapThreeKeys.numberOfValues("two"));
        assertEquals(0, mapThreeKeys.numberOfValues("four"));
        assertEquals(42, mapLarge.numberOfValues("42"));
    }
    
    @Test
    public void withKeyValue() {
        PersistentMultiMap<String, Integer> map;
        map = mapEmpty.with("foo", 22);
        map = map.with("bar", 11);
        map = map.with("foo", 11);
        
        assertEquals(ImmutableCollections.setOf(22, 11), map.get("foo"));
        assertEquals(ImmutableCollections.setOf(11), map.get("bar"));
        
        assertSame(map, map.with("foo", 11));
        
        map = mapEmpty.with("s", 42);
        assertSame(map, map.with("s", 42));
        
        map = map.with("s", 24);
        assertSame(map, map.with("s", 42));
        
        map = mapEmpty;
        for (int i = 0; i < 100; ++i) {
            map = map.with("key" + String.valueOf(i), i);
        }
        assertEquals(100, map.keyCount());
        for (int i = 0; i < 100; ++i) {
            assertValues(ImmutableCollections.listOf(i), map.get("key" + String.valueOf(i)));
        }
        
        assertSame(mapLarge, mapLarge.with("10", 3));
        map = mapLarge.with("3", 12);
        assertValues(map, "3", 1, 2, 3, 12);
        
    }
    
    @Test
    public void withAll() {
        PersistentMultiMap<String, Integer> map = mapEmpty;
        map = map.withAll("foo", ImmutableCollections.<Integer>listOf());
        assertSame(map, mapEmpty);
        
        map = map.withAll("foo", ImmutableCollections.listOf(1, 1, 2));
        map = map.withAll("bar", ImmutableCollections.listOf(3));
        assertEquals(ImmutableCollections.setOf(1, 2), map.get("foo"));
        assertEquals(ImmutableCollections.setOf(3), map.get("bar"));
        
        PersistentMultiMap<String, Integer> map2 = map.withAll("bar", ImmutableCollections.listOf(3, 3, 3));
        assertSame(map, map2);
    }
    
    @Test
    public void withoutKeyValue() {
        assertSame(mapEmpty, mapEmpty.without("three", 174));
        assertSame(mapThreeKeys, mapThreeKeys.without("three", 174));
        assertSame(mapThreeKeys, mapThreeKeys.without("four", 174));
        
        assertEquals(mapEmpty, mapEmpty.with("foo", 2).without("foo", 2));
        assertEquals(mapEmpty, mapSingleKey.without("abc", 2).without("abc", 1).without("abc", 3));
        
        PersistentMultiMap<String, Integer> map;
        map = mapThreeKeys.without("one", 2);
        assertFalse(map.containsKey("one"));
        
        map = map.without("three", 5);
        assertTrue(map.containsKey("three"));
        assertFalse(map.contains("three", 5));
        map = map.without("three", 2);
        map = map.without("three", 3);
        assertFalse(map.containsKey("three"));
        assertFalse(map.contains("three", 5));
        
        map = map.without("two", 2);
        assertEquals(ImmutableCollections.setOf(3), map.get("two"));
        
        assertSame(mapSingleKey, mapSingleKey.without("abc", 42));
        assertSame(mapSingleKey, mapSingleKey.without("bla", 1));
        map = mapSingleKey.without("abc", 2);
        assertValues(ImmutableCollections.setOf(1, 3), map.get("abc"));
        assertEquals(mapEmpty, map.without("abc", 1).without("abc", 3));
        
        assertSame(mapSingleEntry, mapSingleEntry.without("one", 13));
        assertSame(mapSingleEntry, mapSingleEntry.without("bar", 13));
        assertEquals(mapEmpty, mapSingleEntry.without("one", 2));
        
        assertSame(mapLarge, mapLarge.without("foo", 4));
        assertSame(mapLarge, mapLarge.without("10", 141));
        map = mapLarge.without("10", 7);
        assertFalse(map.contains("10", 7));
        assertTrue(map.contains("10", 3));
        
        map = map.without("1", 1);
        assertFalse(map.containsKey("1"));
    }
    
    @Test
    public void without() {
        assertSame(mapEmpty, mapEmpty.without("four"));
        assertSame(mapThreeKeys, mapThreeKeys.without("four"));
        PersistentMultiMap<String, Integer> map = mapThreeKeys.without("two");
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
        
        PersistentMultiMap<String, Integer> map = mapThreeKeys.withoutAll("one", ImmutableCollections.listOf(2, 3));
        assertFalse(map.containsKey("one"));
        
        map = map.withoutAll("two", ImmutableCollections.listOf(1, 2));
        assertEquals(ImmutableCollections.setOf(3), map.get("two"));
        
        assertEquals(mapEmpty, mapSingleKey.withoutAll("abc", mapSingleKey.get("abc")));
        assertSame(mapLarge, mapLarge.withoutAll("1", ImmutableCollections.<Integer>listOf()));
    }
    
    @SafeVarargs
    protected final <K, V> void assertValues(PersistentMultiMap<K, V> map, K key, V... values) {
        assertEquals(ImmutableCollections.setOf(values), map.get(key));
    }
    
    @Test
    public void cleared() {
        assertSame(mapEmpty, mapEmpty.cleared());
        assertEquals(mapEmpty, mapThreeKeys.cleared());
    }
    
    @Test
    public void contains() {
        assertFalse(mapEmpty.contains("foo", 21));
        
        assertFalse(mapSingleKey.contains("foo", 21));
        assertTrue(mapSingleKey.contains("abc", 2));
        assertFalse(mapSingleKey.contains("abc", 12));
        
        assertFalse(mapThreeKeys.contains("foo", 21));
        assertTrue(mapThreeKeys.contains("one", 2));
        assertTrue(mapThreeKeys.contains("three", 2));
        assertTrue(mapThreeKeys.contains("three", 3));
        
        assertTrue(mapLarge.contains("173", 31));
        assertFalse(mapLarge.contains("173", -1));
        assertFalse(mapLarge.contains("foobar", 1));
        assertFalse(mapLarge.contains("bla", 2));
    }
    
    @Test
    public void valuesAsPersistentSet() {
        PersistentSet<Integer> set = mapEmpty.with("foo", 1).get("foo");
        assertEquals(ImmutableCollections.setOf(1), set);
        
        assertEquals(1, (int)set.getFirst());
        assertEquals(Integer.valueOf(1), set.getFirstOrNull());
        
        assertSame(set, set.with(1));
        
        PersistentSet<Integer> set1 = set.with(2);
        assertEquals(ImmutableCollections.setOf(1, 2), set1);
        
        assertSame(set, set.withAll(ImmutableCollections.listOf(1)));
        
        set1 = set.withAll(ImmutableCollections.listOf(3, 1, 4, 3));
        assertEquals(ImmutableCollections.setOf(1, 3, 4), set1);
        
        assertSame(set, set.without(2));
        set1 = set.without(1);
        assertEquals(ImmutableCollections.setOf(), set1);
        
        assertSame(set, set.withoutAll(ImmutableCollections.listOf(3, 4)));
        set1 = set.withoutAll(ImmutableCollections.listOf(3, 1, 1, 5));
        assertEquals(ImmutableCollections.setOf(), set1);
        
        set1 = set.cleared();
        assertEquals(ImmutableCollections.setOf(), set1);
        
        assertEquals(ImmutableCollections.setOf(1), set);
        
        set = mapThreeKeys.get("two");
        assertTrue(set.contains(2));
        assertFalse(set.contains(5));
        
        TestUtil.verifyIteratableRemoveUnsupported(set);
        TestUtil.verifyIteratableRemoveUnsupported(mapSingleKey.get("abc"));
        TestUtil.verifyIteratableRemoveUnsupported(mapThreeKeys.get("two"));
        TestUtil.verifyIteratableRemoveUnsupported(mapLarge.get("111"));
    }
    
    @Test
    public void string() {
        assertEquals("{}", mapEmpty.toString());
        assertEquals("{one={2}}", mapSingleEntry.toString());
    }
    
    protected abstract <K, V> PersistentMultiMap<K, V> empty();
    
    @Test
    public void collidingStringHashs() {
        // The strings {zWUu, yvVV, yvUu, zWVV, zVuV, yutu, zVtu, yuuV} all have hash value 3720861.
        // We use them to test collisions.
        String[] C = new String[] {"zWUu", "yvVV", "yvUu", "zWVV", "zVuV", "yutu", "zVtu", "yuuV"};
        PersistentMultiMap<String, String> map = empty();
        map = map.with(C[0], C[2]);
        
        assertValues(map, C[0], C[2]);
        assertValues(map, C[1]);
        assertValues(map, C[2]);
        assertValues(map, "foo");
        
        map = map.with(C[1], C[2]);
        
        assertValues(map, C[0], C[2]);
        assertValues(map, C[1], C[2]);
        assertValues(map, C[2]);
        assertValues(map, "foo");
        
        map = map.with("foo", C[1]);
        
        assertValues(map, C[0], C[2]);
        assertValues(map, C[1], C[2]);
        assertValues(map, C[2]);
        assertValues(map, "foo", C[1]);
        
        map = map.with(C[0], C[3]);
        map = map.with(C[1], C[4]);
        
        assertValues(map, C[0], C[2], C[3]);
        assertValues(map, C[1], C[2], C[4]);
        assertValues(map, C[2]);
        assertValues(map, "foo", C[1]);
        
        assertSame(map, map.with(C[0], C[2]));
        assertSame(map, map.with(C[1], C[4]));
        assertTrue(map.contains(C[1], C[2]));
        assertFalse(map.contains(C[1], C[1]));
        
        // restart
        map = map.cleared();
        map = map.with(C[0], C[0]);
        assertValues(map, C[0], C[0]);
        assertTrue(map.contains(C[0], C[0]));
        assertFalse(map.contains(C[0], C[1]));
        assertFalse(map.contains(C[1], C[1]));
        
        map = map.with(C[1], "x");
        assertValues(map, C[0], C[0]);
        assertValues(map, C[1], "x");
        
        map = map.without(C[1], "x");
        assertValues(map, C[0], C[0]);
        assertValues(map, C[1]);
        
        map = map.with(C[0], C[3]);
        assertValues(map, C[0], C[0], C[3]);
        assertSame(map, map.without(C[0], "x"));
        
        map = map.without(C[0], C[0]);
        assertValues(map, C[0], C[3]);
        assertSame(map, map.without(C[0], ""));
        
        assertEquals(empty(), map.without(C[0]));
    }
    
    @Test
    public void modifyEntySet() {
        PersistentSet<Integer> entrySet = mapThreeKeys.get("three");
        
        assertSame(entrySet, entrySet.with(2));
        assertEquals(ImmutableCollections.setOf(2, 3, 4, 5), entrySet.with(4));
        
        assertSame(entrySet, entrySet.withAll(ImmutableCollections.listOf(2, 3)));
        assertEquals(ImmutableCollections.setOf(2, 3, 5, 8, 13, 21), entrySet.withAll(ImmutableCollections.listOf(5, 8, 13, 21)));
        
        assertSame(entrySet, entrySet.without(1));
        assertEquals(ImmutableCollections.setOf(2, 5), entrySet.without(3));
        
        assertSame(entrySet, entrySet.withoutAll(ImmutableCollections.listOf(0, 1, -1)));
        assertEquals(ImmutableCollections.setOf(5), entrySet.withoutAll(ImmutableCollections.listOf(3, 2)));
        
        assertEquals(ImmutableCollections.setOf(), entrySet.cleared());
    }
    
    @Test
    public void modifyEntrySet_empty() {
        PersistentSet<Integer> entrySet = mapThreeKeys.get("missing");
        
        assertSame(entrySet, entrySet.cleared());
    }
    
}
