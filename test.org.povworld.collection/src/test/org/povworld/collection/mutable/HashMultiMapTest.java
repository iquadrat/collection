package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.HashMultiMap;
import org.povworld.collection.persistent.PersistentCollections;

import test.org.povworld.collection.AbstractMultiMapTest;

/**
 * Unit tests for {@link HashMultiMap}.
 */
public class HashMultiMapTest extends AbstractMultiMapTest<Set<Integer>, HashMultiMap<String, Integer>> {
    
    @Override
    protected HashMultiMap<String, Integer> create() {
        return new HashMultiMap<String, Integer>();
    }
    
    @Override
    protected HashMultiMap<String, Integer> set(HashMultiMap<String, Integer> map, String key, Collection<Integer> values) {
        map.remove(key);
        map.putAll(key, values);
        return map;
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        assertEquals(ImmutableCollections.asSet(expectedValues), actualValues);
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
    public void putKeyValue() {
        HashMultiMap<String, Integer> map = create();
        map.put("foo", 22);
        map.put("bar", 11);
        map.put("foo", 11);
        
        assertEquals(ImmutableCollections.setOf(22, 11), map.get("foo"));
        assertEquals(ImmutableCollections.setOf(11), map.get("bar"));
        
        map = create();
        for (int i = 0; i < 100; ++i) {
            map.put("key" + String.valueOf(i), i);
        }
        assertEquals(100, map.keyCount());
        for (int i = 0; i < 100; ++i) {
            assertValues(ImmutableCollections.listOf(i), map.get("key" + String.valueOf(i)));
        }
    }
    
    @Test
    public void putAll() {
        HashMultiMap<String, Integer> map = new HashMultiMap<String, Integer>(mapEmpty);
        map.putAll("foo", ImmutableCollections.<Integer>listOf());
        assertEquals(map, mapEmpty);
        
        map.putAll("foo", ImmutableCollections.listOf(1, 1, 2));
        map.putAll("bar", ImmutableCollections.listOf(3));
        assertEquals(ImmutableCollections.setOf(1, 2), map.get("foo"));
        assertEquals(ImmutableCollections.setOf(3), map.get("bar"));
        
        HashMultiMap<String, Integer> map2 = new HashMultiMap<String, Integer>(map);
        map2.putAll("bar", ImmutableCollections.listOf(3, 3, 3));
        assertEquals(map, map2);
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
    public void removeKey() {
        assertFalse(mapEmpty.remove("four"));
        assertFalse(mapThreeKeys.remove("four"));
        HashMultiMap<String, Integer> map = new HashMultiMap<String, Integer>(mapThreeKeys);
        map.remove("two");
        assertFalse(map.containsKey("two"));
        
        assertFalse(mapSingleKey.remove("hello"));
        mapSingleKey.remove("abc");
        assertEquals(mapEmpty, mapSingleKey);
        
        assertFalse(mapSingleEntry.remove("bar"));
        assertTrue(mapSingleEntry.remove("one"));
        assertEquals(mapEmpty, mapSingleEntry);
        
        assertFalse(mapLarge.remove("bar"));
        assertTrue(mapLarge.containsKey("10"));
        mapLarge.remove("10");
        assertFalse(map.containsKey("10"));
    }
    
    @Test
    public void removeKeyValue() {
        assertFalse(mapEmpty.remove("three", 174));
        assertFalse(mapThreeKeys.remove("three", 174));
        assertFalse(mapThreeKeys.remove("four", 174));
        mapEmpty.put("foo", 2);
        mapEmpty.remove("foo", 2);
        
        assertEquals(PersistentCollections.multiMapOf(), mapEmpty);
        
        HashMultiMap<String, Integer> map = new HashMultiMap<String, Integer>(mapSingleKey);
        map.remove("abc", 2);
        map.remove("abc", 1);
        map.remove("abc", 3);
        assertEquals(mapEmpty, map);
        
        map = new HashMultiMap<String, Integer>(mapThreeKeys);
        assertTrue(map.remove("one", 2));
        assertFalse(map.containsKey("one"));
        
        assertTrue(map.remove("three", 5));
        assertTrue(map.containsKey("three"));
        assertFalse(map.contains("three", 5));
        assertTrue(map.remove("three", 2));
        assertTrue(map.remove("three", 3));
        assertFalse(map.containsKey("three"));
        assertFalse(map.contains("three", 5));
        
        assertTrue(map.remove("two", 2));
        assertEquals(ImmutableCollections.setOf(3), map.get("two"));
        
        map = new HashMultiMap<String, Integer>(mapSingleKey);
        
        assertFalse(map.remove("abc", 42));
        assertFalse(map.remove("bla", 1));
        assertTrue(map.remove("abc", 2));
        assertValues(ImmutableCollections.setOf(1, 3), map.get("abc"));
        assertTrue(map.remove("abc", 1));
        assertTrue(map.remove("abc", 3));
        assertEquals(mapEmpty, map);
        
        assertFalse(mapSingleEntry.remove("one", 13));
        assertFalse(mapSingleEntry.remove("bar", 13));
        assertTrue(mapSingleEntry.remove("one", 2));
        assertEquals(mapEmpty, mapSingleEntry);
        
        assertFalse(mapLarge.remove("foo", 4));
        assertFalse(mapLarge.remove("10", 141));
        mapLarge.remove("10", 7);
        assertFalse(mapLarge.contains("10", 7));
        assertTrue(mapLarge.contains("10", 3));
        
        mapLarge.remove("1", 1);
        assertFalse(mapLarge.containsKey("1"));
    }
    
    @Test
    public void removeAll() {
        HashMultiMap<String, Integer> map = new HashMultiMap<String, Integer>(mapThreeKeys);
        
        assertFalse(mapThreeKeys.removeAll("foo", ImmutableCollections.listOf(1, 1, 2)));
        assertFalse(mapThreeKeys.removeAll("three", ImmutableCollections.listOf(7, 8, 9)));
        assertEquals(mapThreeKeys, map);
        
        assertTrue(map.removeAll("one", ImmutableCollections.listOf(2, 3)));
        assertFalse(map.containsKey("one"));
        
        assertTrue(map.removeAll("two", ImmutableCollections.listOf(1, 2)));
        assertEquals(ImmutableCollections.setOf(3), map.get("two"));
        
        assertTrue(mapSingleKey.removeAll("abc", mapSingleKey.get("abc")));
        assertEquals(mapEmpty, mapSingleKey);
        assertFalse(mapLarge.removeAll("1", ImmutableCollections.<Integer>listOf()));
    }
    
    @Test
    public void clear() {
        mapEmpty.clear();
        mapLarge.clear();
        mapSingleEntry.clear();
        mapThreeKeys.clear();
        mapSingleKey.clear();
        
        assertTrue(mapEmpty.isEmpty());
        assertEquals(0, mapLarge.keys().size());
        assertEquals(mapEmpty, mapSingleEntry);
        assertEquals(0, mapThreeKeys.keyCount());
        assertTrue(mapSingleKey.isEmpty());
    }
    
    @Test
    public void string() {
        assertEquals("{}", mapEmpty.toString());
        assertEquals("{one={2}}", mapSingleEntry.toString());
        
        StringBuilder expected = new StringBuilder();
        expected.append("{");
        EntryIterator<String, ? extends Set<Integer>> iterator = mapThreeKeys.entryIterator();
        while (iterator.next()) {
            expected.append(iterator.getCurrentKey());
            expected.append("=");
            expected.append(CollectionUtil.toSetString(iterator.getCurrentValue()));
            expected.append(", ");
        }
        expected.setLength(expected.length() - 2);
        expected.append("}");
        assertEquals(expected.toString(), mapThreeKeys.toString());
    }
}
