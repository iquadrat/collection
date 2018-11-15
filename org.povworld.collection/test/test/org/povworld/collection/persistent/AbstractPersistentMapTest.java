package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.povworld.collection.immutable.ImmutableCollections.listOf;

import org.junit.Test;
import org.povworld.collection.persistent.PersistentMap;

import test.org.povworld.collection.AbstractMapTest;

// TODO test with custom key and value comparators!
public abstract class AbstractPersistentMapTest extends AbstractMapTest<PersistentMap<String, Integer>> {
    
    @Override
    protected PersistentMap<String, Integer> put(PersistentMap<String, Integer> map, String key, Integer value) {
        return map.with(key, value);
    }
    
    @Override
    protected PersistentMap<String, Integer> remove(PersistentMap<String, Integer> map, String key) {
        return map.without(key);
    }
    
    @Override
    protected PersistentMap<String, Integer> clear(PersistentMap<String, Integer> map) {
        return map.cleared();
    }
    
    @Test
    public void with() {
        PersistentMap<String, Integer> map1 = put(mapSingleton, "bar", 41);
        PersistentMap<String, Integer> map2 = put(map1, "moo", 0);
        PersistentMap<String, Integer> map3 = put(map2, "bar", 1);
        
        verifyMap(map1, listOf("foo", "bar"), listOf(42, 41));
        verifyMap(map2, listOf("foo", "bar", "moo"), listOf(42, 41, 0));
        verifyMap(map3, listOf("foo", "bar", "moo"), listOf(42, 1, 0));
        
        PersistentMap<String, Integer> map4 = put(mapLarge, MANY_KEYS.get(4), MANY_VALUES.get(4));
        assertSame(mapLarge, map4);
        
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(1);
        assertNotSame(i1, i2);
        
        PersistentMap<String, Integer> map5 = put(mapEmpty, "foo", i1);
        assertSame(map5, put(map5, "foo", i2));
    }
    
    @Test
    public void withAll() {
        PersistentMap<String, Integer> map1 = mapEmpty.withAll(mapThree);
        assertEquals(mapThree, map1);
        
        PersistentMap<String, Integer> map2a = map1.withAll(mapSingleton);
        PersistentMap<String, Integer> map2b = mapSingleton.withAll(map1);
        verifyMap(map2a, listOf("foo", "one", "two", "three"), listOf(42, 1, 2, 3));
        assertEquals(map2a, map2b);
        
        PersistentMap<String, Integer> map3 = map2a.withAll(mapThree);
        assertSame(map3, map2a);
        
        PersistentMap<String, Integer> map4 = map3.withAll(mapLarge);
        PersistentMap<String, Integer> map5 = mapLarge.withAll(map4);
        assertEquals(map4, map5);
    }
    
}
