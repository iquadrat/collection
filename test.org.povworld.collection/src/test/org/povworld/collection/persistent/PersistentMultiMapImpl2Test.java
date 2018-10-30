package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.annotation.CheckForNull;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentCollections;
import org.povworld.collection.persistent.PersistentMultiMap;
import org.povworld.collection.persistent.PersistentMultiMapImpl2;
import org.povworld.collection.persistent.PersistentSet;

import test.org.povworld.collection.ChosenHash;

/**
 * Unit tests for {@link PersistentMultiMapImpl2}.
 */
public class PersistentMultiMapImpl2Test extends AbstractPersistentMultiMapTest<PersistentMultiMap<String, Integer>> {
    
    @Override
    protected PersistentMultiMap<String, Integer> create() {
        return empty();
    }
    
    @Override
    protected <K, V> PersistentMultiMap<K, V> empty() {
        return PersistentMultiMapImpl2.empty();
    }
    
    @Override
    protected PersistentMultiMap<String, Integer> set(PersistentMultiMap<String, Integer> map, String key,
            Collection<Integer> values) {
        PersistentMultiMap<String, Integer> map0 = map.without(key);
        PersistentMultiMap<String, Integer> map1 = map0.withAll(key, values);
        return map1;
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        assertEquals(ImmutableCollections.asSet(expectedValues), actualValues);
    }
    
    private static final int COMPACT_BUCKET_SPLIT_SIZE = PersistentMultiMapImpl2.HASHTABLE_SIZE;
    
    ChosenHash k1 = new ChosenHash("key", 14);
    
    ChosenHash k2 = new ChosenHash("anotherKey", 14);
    
    ChosenHash k3 = new ChosenHash("thirdKey", 14);
    
    ChosenHash k4 = new ChosenHash("Key4", 14 + (1 << 30));
    
    ChosenHash k5 = new ChosenHash("Key4", 14);
    
    ChosenHash v1 = new ChosenHash("v1", 71);
    
    ChosenHash v2 = new ChosenHash("v2", 14124);
    
    ChosenHash v3 = new ChosenHash("v3", 71);
    
    @Test
    public void collidingHashsExtreme1() {
        PersistentMultiMap<ChosenHash, ChosenHash> map = empty();
        
        PersistentSet<ChosenHash> k1Values = PersistentCollections.setOf();
        
        for (int i = 0; i < COMPACT_BUCKET_SPLIT_SIZE; ++i) {
            ChosenHash value = new ChosenHash(String.valueOf(i), (i - 2) * (i - 4));
            map = map.with(k1, value);
            k1Values = k1Values.with(value);
        }
        
        assertEquals(k1Values, map.get(k1));
        
        PersistentMultiMap<ChosenHash, ChosenHash> map1 = map.with(k2, v1);
        assertTrue(map1.contains(k1, new ChosenHash("2", 0)));
        assertValues(map1, k2, v1);
        
        assertEquals(k1Values, map1.get(k1));
        assertEquals(PersistentCollections.setOf(v1), map1.get(k2));
        
        assertEquals(ImmutableCollections.setOf(), map1.get(v1));
        assertEquals(ImmutableCollections.setOf(), map1.get(k5));
        assertTrue(map1.containsKey(k1));
        assertFalse(map1.containsKey(v1));
        assertFalse(map1.containsKey(k5));
        assertTrue(map1.contains(k1, new ChosenHash("2", 0)));
        assertFalse(map1.contains(v2, v2));
        assertFalse(map1.contains(k5, v2));
        assertEquals(2, map1.keyCount());
        
        EntryIterator<ChosenHash, ? extends Set<ChosenHash>> entryIterator = map1.entryIterator();
        int entries = 0;
        while (entryIterator.next()) {
            entries++;
            ChosenHash key = entryIterator.getCurrentKey();
            Set<ChosenHash> values = entryIterator.getCurrentValue();
            if (k1.equals(key)) {
                assertEquals(k1Values, values);
            } else {
                assertEquals(k2, key);
                assertEquals(PersistentCollections.setOf(v1), values);
            }
        }
        assertFalse(entryIterator.next());
        assertEquals(2, entries);
        
        PersistentMultiMap<ChosenHash, ChosenHash> map2 = map1.with(k2, v3);
        assertTrue(map2.contains(k2, v3));
        assertValues(map2, k2, v1, v3);
        assertEquals(2, map2.keyCount());
        
        PersistentMultiMap<ChosenHash, ChosenHash> map3 = map2.with(k3, v2);
        assertValues(map3, k2, v1, v3);
        assertValues(map3, k3, v2);
        assertEquals(3, map3.keyCount());
        assertSame(map3, map3.with(k3, v2));
        
        PersistentMultiMap<ChosenHash, ChosenHash> map4 = map3.with(k4, v1);
        assertValues(map4, k2, v1, v3);
        assertValues(map4, k3, v2);
        assertValues(map4, k4, v1);
        assertEquals(4, map4.keyCount());
        
        map = map.with(k1, v1);
        assertTrue(map.contains(k1, v1));
        assertFalse(map.contains(k1, v2));
        
        assertSame(map3, map3.without(k5));
        assertSame(map3, map3.without(v2));
        map = map3.without(k3);
        assertFalse(map.containsKey(k3));
        assertEquals(2, map.keyCount());
        
        assertSame(map3, map3.without(k1, v1));
        assertSame(map3, map3.without(k5, v1));
        assertSame(map3, map3.without(v1, v1));
        map = map3.without(k1, new ChosenHash("2", 0));
        assertEquals(COMPACT_BUCKET_SPLIT_SIZE - 1, map.get(k1).size());
        
        map = map.without(k2, v3).without(k2, v1);
        assertFalse(map.containsKey(k2));
        
        map = map.without(k3);
        assertEquals(1, map.keyCount());
    }
    
    @Test
    public void collidingHashsExtreme2() {
        PersistentMultiMap<ChosenHash, ChosenHash> map = empty();
        
        int bits = PersistentMultiMapImpl2.HASH_BITS;
        assertEquals(PersistentMultiMapImpl2.HASHTABLE_SIZE, 1 << bits);
        
        ChosenHash k1 = new ChosenHash("intruder", 0);
        for (int i = 0; i < COMPACT_BUCKET_SPLIT_SIZE / 2; ++i) {
            map = map.with(k1, new ChosenHash("value" + i, 0));
        }
        
        for (int level = 0; level < 32; level += bits) {
            for (int i = 0; i < PersistentMultiMapImpl2.HASHTABLE_SIZE; ++i) {
                ChosenHash key = key(level, i);
                map = map.with(key, key);
            }
        }
        
        for (int i = 0; i < COMPACT_BUCKET_SPLIT_SIZE; ++i) {
            map = map.with(new ChosenHash("intruder" + i, 0), v1);
            map = map.with(new ChosenHash("intruder" + i, 0), v2);
            map = map.with(new ChosenHash("intruder" + i, 0), v3);
        }
        
        for (int i = 0; i < COMPACT_BUCKET_SPLIT_SIZE / 2; ++i) {
            assertTrue("value " + i + " not contained", map.contains(k1, new ChosenHash("value" + i, 0)));
        }
        
        for (int level = 0; level < 32; level += bits) {
            for (int i = 0; i < PersistentMultiMapImpl2.HASHTABLE_SIZE; ++i) {
                ChosenHash key = key(level, i);
                assertValues(map, key, key);
            }
        }
        
        for (int i = 0; i < COMPACT_BUCKET_SPLIT_SIZE; ++i) {
            assertValues(map, new ChosenHash("intruder" + i, 0), v1, v2, v3);
        }
        
    }
    
    private ChosenHash key(int level, int index) {
        return new ChosenHash(level + "/" + index, index << level);
    }
    
    private static class TestIdentificator implements Identificator<String> {
        
        @Override
        public boolean isIdentifiable(Object object) {
            return object instanceof String;
        }
        
        @Override
        public boolean equals(String object1, String object2) {
            return object1.toLowerCase().equals(object2.toLowerCase());
        }
        
        @Override
        public int hashCode(String object) {
            return object.toLowerCase().hashCode();
        }
        
        @Override
        public boolean equals(@CheckForNull Object obj) {
            return super.equals(obj);
        }
        
    }
    
    @Test
    public void customIdentificator() {
        TestIdentificator testIdentificator = new TestIdentificator();
        PersistentMultiMapImpl2<String, Integer> map = PersistentMultiMapImpl2.<String, Integer>empty(testIdentificator);
        assertTrue(map.isEmpty());
        assertEquals(ImmutableCollections.<Integer>setOf(), map.get("Foo"));
        
        map = map.withAll("Foo", CollectionUtil.wrap(1, 2, 3));
        assertFalse(map.isEmpty());
        assertEquals(ImmutableCollections.<Integer>setOf(1, 2, 3), map.get("Foo"));
        assertEquals(ImmutableCollections.<Integer>setOf(1, 2, 3), map.get("fOo"));
        
        assertSame(map, map.without("fooo"));
        assertTrue(map.without("foo").isEmpty());
        
        assertSame(testIdentificator, map.getKeyIdentificator());
    }
}
