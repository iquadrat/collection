package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.povworld.collection.immutable.ImmutableCollections.listOf;
import static org.povworld.collection.immutable.ImmutableCollections.setOf;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.Map;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;

public abstract class AbstractMultiMapTest<C extends Collection<Integer>, M extends Map<String, C>> {
    
    private interface Mapping {
        
        public List<Integer> map(String string);
        
    }
    
    private static class SingleMapping implements Mapping {
        
        @Override
        public List<Integer> map(String string) {
            return listOf(1, 2, 3);
        }
        
    }
    
    private static class OneTwoThreeMapping implements Mapping {
        
        @Override
        public List<Integer> map(String string) {
            if ("one".equals(string)) {
                return listOf(2);
            }
            if ("two".equals(string)) {
                return listOf(2, 3);
            }
            if ("three".equals(string)) {
                return listOf(2, 3, 5);
            }
            return listOf();
        }
        
    }
    
    private static class EnumerateMapping implements Mapping {
        
        @Override
        public List<Integer> map(String string) {
            int n = Integer.valueOf(string);
            ArrayList<Integer> values = new ArrayList<Integer>(n);
            for (int i = 1; i <= n; ++i) {
                values.push(i);
            }
            return values;
        }
        
    }
    
    protected abstract M create();
    
    protected abstract M set(M map, String key, Collection<Integer> values);
    
    protected abstract void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues);
    
    protected M mapEmpty;
    
    protected M mapSingleEntry;
    
    protected M mapSingleKey;
    
    protected M mapThreeKeys;
    
    protected M mapLarge;
    
    protected static final Set<String> MANY_KEYS;
    
    private static final List<Integer> NO_VALUES = ImmutableCollections.listOf();
    
    static {
        HashSet<String> keys = new HashSet<String>();
        for (int i = 1; i <= 1000; ++i) {
            String key = String.valueOf(i);
            keys.add(key);
        }
        MANY_KEYS = keys;
    }
    
    @Before
    public void initMaps() {
        mapEmpty = create();
        mapSingleEntry = fillMap(new OneTwoThreeMapping(), Arrays.asList("one"));
        mapSingleKey = fillMap(new SingleMapping(), listOf("abc"));
        mapThreeKeys = fillMap(new OneTwoThreeMapping(), listOf("one", "two", "three"));
        mapLarge = fillMap(new EnumerateMapping(), MANY_KEYS);
    }
    
    private M fillMap(Mapping mapping, Iterable<String> keys) {
        int i = 0;
        M map = create();
        for (String key: keys) {
            i++;
            map = set(map, key, mapping.map(key));
            assertEquals(i, map.keyCount());
        }
        return map;
    }
    
    @Test
    public void containsKey() {
        assertFalse(mapEmpty.containsKey(""));
        assertFalse(mapSingleKey.containsKey("foo"));
        assertTrue(mapSingleKey.containsKey("abc"));
        assertTrue(mapThreeKeys.containsKey("two"));
        assertFalse(mapThreeKeys.containsKey("four"));
        for (String key: MANY_KEYS) {
            assertTrue("map does not contain " + key, mapLarge.containsKey(key));
        }
        assertFalse(mapLarge.containsKey("one"));
        assertFalse(mapLarge.containsKey("foobar"));
        assertFalse(mapLarge.containsKey("bla"));
        assertFalse(mapLarge.containsKey("-1"));
    }
    
    @Test
    public void get() {
        assertValues(NO_VALUES, mapEmpty.get(""));
        assertValues(NO_VALUES, mapSingleKey.get("ABC"));
        assertValues(NO_VALUES, mapThreeKeys.get("four"));
        assertValues(NO_VALUES, mapLarge.get("foo"));
        
        assertValues(listOf(1, 2, 3), mapSingleKey.get("abc"));
        
        assertValues(listOf(2), mapThreeKeys.get("one"));
        assertValues(listOf(2, 3, 5), mapThreeKeys.get("three"));
        
        EnumerateMapping enumerateMapping = new EnumerateMapping();
        for (String key: MANY_KEYS) {
            assertValues(enumerateMapping.map(key), mapLarge.get(key));
        }
    }
    
    @Test
    public void isEmpty() {
        assertTrue(mapEmpty.isEmpty());
        assertFalse(mapSingleEntry.isEmpty());
        assertFalse(mapSingleKey.isEmpty());
        assertFalse(mapThreeKeys.isEmpty());
        assertFalse(mapLarge.isEmpty());
    }
    
    @Test
    public void iterateKeysAndKeySet() {
        verifyKeys(mapEmpty.keys(), ImmutableCollections.<String>setOf());
        verifyKeys(mapSingleKey.keys(), setOf("abc"));
        verifyKeys(mapThreeKeys.keys(), setOf("one", "three", "two"));
        verifyKeys(mapLarge.keys(), MANY_KEYS);
    }
    
    protected void assertKeySet(Collection<String> expected, Collection<String> actual) {
        assertEquals(ImmutableCollections.asSet(expected), actual);
    }
    
    @Test
    public void keySet() {
        assertKeySet(ImmutableCollections.<String>setOf(), mapEmpty.keys());
        assertKeySet(setOf("abc"), mapSingleKey.keys());
        assertKeySet(setOf("one", "three", "two"), mapThreeKeys.keys());
        assertKeySet(MANY_KEYS, mapLarge.keys());
    }
    
    @Test
    public void numberOfKeys() {
        assertEquals(0, mapEmpty.keyCount());
        assertEquals(1, mapSingleKey.keyCount());
        assertEquals(3, mapThreeKeys.keyCount());
        assertEquals(MANY_KEYS.size(), mapLarge.keyCount());
    }
    
    protected void verifyKeys(Iterable<String> keyIterable, Set<String> expectedKeys) {
        HashSet<String> actualKeys = TestUtil.verifyIterable(keyIterable, HashSet.<String>newBuilder());
        assertEquals(expectedKeys, actualKeys);
    }
    
    @Test
    public void iterarteEntries() {
        verifyEntryIteration(mapEmpty, ImmutableCollections.<String>setOf(), new SingleMapping());
        verifyEntryIteration(mapSingleKey, setOf("abc"), new SingleMapping());
        verifyEntryIteration(mapThreeKeys, setOf("one", "three", "two"), new OneTwoThreeMapping());
        verifyEntryIteration(mapLarge, MANY_KEYS, new EnumerateMapping());
    }
    
    private void verifyEntryIteration(M map, Set<String> expectedKeys, Mapping mapping) {
        EntryIterator<String, ? extends Collection<Integer>> iterator = map.entryIterator();
        
        HashSet<String> actualKeys = new HashSet<String>();
        
        for (int i = 0; i < expectedKeys.size(); ++i) {
            assertTrue("too few keys in iteraton: i=" + i, iterator.next());
            String key = iterator.getCurrentKey();
            Collection<Integer> values = iterator.getCurrentValue();
            assertTrue(actualKeys.add(key));
            assertValues(mapping.map(key), values);
            assertEquals(values, iterator.getCurrentValue());
            assertSame(key, iterator.getCurrentKey());
        }
        
        assertFalse(iterator.next());
        
        assertEquals(expectedKeys, actualKeys);
        
        iterator = map.entryIterator();
        
        try {
            iterator.getCurrentKey();
            fail(NoSuchElementException.class.getSimpleName() + " expected");
        } catch (NoSuchElementException e) {
            // pass
        }
        
        try {
            iterator.getCurrentValue();
            fail(NoSuchElementException.class.getSimpleName() + " expected");
        } catch (NoSuchElementException e) {
            // pass
        }
    }
    
    @Test
    public void valuesAsCollection() {
        Collection<Integer> c = mapEmpty.get("");
        assertNull(c.getFirstOrNull());
        try {
            c.getFirst();
            fail("NoSuchElementException expected!");
        } catch (NoSuchElementException e) {
            // pass
        }
        
        c = mapSingleEntry.get("one");
        assertFalse(c.isEmpty());
        assertEquals((Integer)2, c.getFirst());
        assertEquals(ImmutableCollections.setOf(2), TestUtil.verifyIterable(c, HashSet.<Integer>newBuilder()));
        
        c = mapSingleKey.get("abc");
        assertFalse(c.isEmpty());
        assertTrue(CollectionUtil.contains(c, c.getFirst()));
        assertEquals(ImmutableCollections.setOf(1, 2, 3), TestUtil.verifyIterable(c, HashSet.<Integer>newBuilder()));
        
        c = mapThreeKeys.get("two");
        assertFalse(c.isEmpty());
        assertTrue(CollectionUtil.contains(c, c.getFirst()));
        assertEquals(ImmutableCollections.setOf(2, 3), TestUtil.verifyIterable(c, HashSet.<Integer>newBuilder()));
    }
    
    @Test
    public void equalsImpl() {
        assertFalse(mapThreeKeys.equals(null));
        assertFalse(mapThreeKeys.equals(new Object()));
        assertFalse(mapSingleKey.equals(mapThreeKeys));
        assertFalse(mapSingleKey.equals(mapSingleEntry));
        
        assertTrue(mapSingleEntry.equals(mapSingleEntry));
        assertTrue(mapSingleEntry.equals(fillMap(new OneTwoThreeMapping(), Arrays.asList("one"))));
        
        M otherLarge = fillMap(new EnumerateMapping(), MANY_KEYS);
        assertTrue(mapLarge.equals(otherLarge));
        
        otherLarge = set(otherLarge, otherLarge.keys().getFirst(), ImmutableCollections.<Integer>listOf());
        assertFalse(mapLarge.equals(otherLarge));
    }
    
    @Test
    public void hashCodeImpl() {
        Identificator<? super String> keyHasher = mapSingleKey.getKeyIdentificator();
        Identificator<? super C> valueHasher = mapSingleKey.getValueIdentificator();
        assertEquals(-1, create().hashCode());
        assertEquals(-1 + keyHasher.hashCode("abc") + 255 * valueHasher.hashCode(mapSingleKey.get("abc")), mapSingleKey.hashCode());
    }
    
}
