package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.HashListMultiMap;
import org.povworld.collection.persistent.PersistentCollections;
import org.povworld.collection.persistent.PersistentListMultiMap;

import test.org.povworld.collection.AbstractMultiMapTest;

/**
 * Unit tests for {@link HashListMultiMap}.
 *
 */
public class HashListMultiMapTest extends AbstractMultiMapTest<List<Integer>, HashListMultiMap<String, Integer>> {
    
    @Override
    protected HashListMultiMap<String, Integer> create() {
        return new HashListMultiMap<String, Integer>(1, 
                CollectionUtil.getObjectIdentificator(),
                CollectionUtil.getObjectIdentificator());
    }
    
    @Override
    protected HashListMultiMap<String, Integer> set(HashListMultiMap<String, Integer> map, String key, Collection<Integer> values) {
        map.remove(key);
        map.putAll(key, values);
        return map;
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        assertEquals(ImmutableCollections.asOrderedSet(expectedValues), actualValues);
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
    public void copyConstructor() {
        PersistentListMultiMap<String, Integer> empty = PersistentCollections.listMultiMapOf();
        HashListMultiMap<String, Object> map = new HashListMultiMap<String, Object>(empty);
        assertTrue(map.isEmpty());
        
        PersistentListMultiMap<String, Integer> base = empty.withAtEnd("foo", 3);
        base = base.withAtEnd("sugar", 5);
        base = base.withAll("computer", ImmutableCollections.listOf(8, 7, 6, 5));
        map = new HashListMultiMap<String, Object>(base);
        assertEquals(3, map.keyCount());
        assertEquals(ImmutableCollections.listOf(5), map.get("sugar"));
    }
    
    @Test
    public void putAtEnd() {
        HashListMultiMap<String, Integer> map = create();
        map.putAtEnd("noob", 1);
        map.putAtEnd("foo", 2);
        map.putAtEnd("bar", 3);
        
        assertEquals(ImmutableCollections.listOf(2), map.get("foo"));
        
        map.putAtEnd("foo", 21);
        assertEquals(ImmutableCollections.listOf(2, 21), map.get("foo"));
    }
    
    @Test
    public void putAll() {
        HashListMultiMap<String, Integer> map = create();
        map.putAll("void", ImmutableCollections.<Integer>listOf());
        
        assertEquals(0, map.keyCount());
        assertEquals(0, map.numberOfValues("void"));
    }
    
    @Test
    public void putAtBegin() {
        HashListMultiMap<String, Integer> map = create();
        map.putAtEnd("s", 1);
        
        map.putAtBegin("s", 2);
        map.putAtBegin("s", 3);
        
        assertEquals(ImmutableCollections.listOf(3, 2, 1), map.get("s"));
    }
    
    @Test
    public void putAt() {
        HashListMultiMap<String, Integer> map = create();
        
        map.putAll("x", ImmutableCollections.listOf(1, 2, 3, 2, 1));
        
        map.putAt("x", 0, 0);
        map.putAt("x", 4, 3);
        map.putAt("x", 4, 3);
        
        assertEquals(ImmutableCollections.listOf(0, 1, 2, 4, 4, 3, 2, 1), map.get("x"));
        
        map.putAt("x", 5, 2);
        map.putAt("x", 6, 9);
        assertEquals(ImmutableCollections.listOf(0, 1, 5, 2, 4, 4, 3, 2, 1, 6), map.get("x"));
        
        try {
            map.putAt("x", 0, -1);
            fail("expected " + IndexOutOfBoundsException.class.getName());
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
        try {
            map.putAt("x", 0, 42);
            fail("expected " + IndexOutOfBoundsException.class.getName());
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
    }
    
    @Test
    public void removeKeyValue() {
        HashListMultiMap<String, Integer> map = create();
        map.putAll("x", ImmutableCollections.listOf(1, 2, 3, 2));
        
        assertFalse(map.remove("y", 2));
        
        assertTrue(map.remove("x", 2));
        assertEquals(ImmutableCollections.listOf(1, 3, 2), map.get("x"));
        
        assertTrue(map.remove("x", 2));
        assertEquals(ImmutableCollections.listOf(1, 3), map.get("x"));
        
        assertFalse(map.remove("x", 2));
        assertEquals(ImmutableCollections.listOf(1, 3), map.get("x"));
        
        assertTrue(map.remove("x", 1));
        assertTrue(map.remove("x", 3));
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void removeKey() {
        HashListMultiMap<String, Integer> map = create();
        map.putAtEnd("x", 1);
        map.putAtEnd("y", 2);
        map.putAtEnd("y", 3);
        
        assertFalse(map.remove("a"));
        assertTrue(map.remove("y"));
        assertTrue(map.remove("x"));
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void removeAll() {
        HashListMultiMap<String, Integer> map = create();
        map.putAll("x", ImmutableCollections.listOf(1, 2, 3, 2, 1));
        
        assertFalse(map.removeAll("y", ImmutableCollections.listOf(2)));
        assertFalse(map.removeAll("x", ImmutableCollections.<Integer>listOf()));
        assertTrue(map.removeAll("x", ImmutableCollections.listOf(2)));
        
        assertEquals(ImmutableCollections.listOf(1, 3, 2, 1), map.get("x"));
        assertTrue(map.removeAll("x", ImmutableCollections.listOf(1, 1, 2, 4, 5)));
        assertEquals(ImmutableCollections.listOf(3), map.get("x"));
        
        assertTrue(map.removeAll("x", ImmutableCollections.listOf(1, 2, 3, 4, 5)));
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void replaceAll() {
        HashListMultiMap<String, Integer> map = create();
        map.putAll("x", ImmutableCollections.listOf(1, 2, 3, 2, 1));
        
        assertEquals(0, map.replaceAll("y", 3, 4));
        assertEquals(0, map.replaceAll("x", 13, 4));
        assertEquals(1, map.replaceAll("x", 3, 1));
        assertEquals(ImmutableCollections.listOf(1, 2, 1, 2, 1), map.get("x"));
        assertEquals(3, map.replaceAll("x", 1, 2));
        assertEquals(ImmutableCollections.listOf(2, 2, 2, 2, 2), map.get("x"));
        
        Integer A = new Integer(2);
        Integer B = new Integer(2);
        assertNotSame(A, B);
        
        map.putAtEnd("y", A);
        
        map.replaceAll("y", A, B);
        assertSame(B, map.get("y").getFirst());
    }
    
    @Test
    public void clear() {
        mapLarge.clear();
        mapEmpty.clear();
        mapSingleEntry.clear();
        
        assertTrue(mapLarge.isEmpty());
        assertTrue(mapEmpty.isEmpty());
        assertTrue(mapSingleEntry.isEmpty());
    }
    
    @Test
    public void identificatorIsDefault() {
        assertEquals(CollectionUtil.getObjectIdentificator(), mapLarge.getKeyIdentificator());
    }
    
}
