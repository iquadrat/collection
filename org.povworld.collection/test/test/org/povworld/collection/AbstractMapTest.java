package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.povworld.collection.immutable.ImmutableCollections.asSet;
import static org.povworld.collection.immutable.ImmutableCollections.listOf;
import static org.povworld.collection.immutable.ImmutableCollections.setOf;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.List;
import org.povworld.collection.Map;
import org.povworld.collection.immutable.ImmutableArrayList;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableList;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;
import org.povworld.collection.persistent.PersistentHashMap;
import org.povworld.collection.persistent.PersistentMap;

public abstract class AbstractMapTest<M extends Map<String, Integer>> {
    
    protected static final ImmutableList<String> MANY_KEYS;
    
    protected static final ImmutableList<Integer> MANY_VALUES;
    
    static {
        CollectionBuilder<String, ? extends ImmutableList<String>> keyBuilder = ImmutableArrayList.<String>newBuilder(1000);
        CollectionBuilder<Integer, ? extends ImmutableList<Integer>> valueBuilder = ImmutableArrayList.<Integer>newBuilder(1000);
        
        for (int i = 0; i < 1000; ++i) {
            keyBuilder.add(String.valueOf(i));
            valueBuilder.add(i);
        }
        
        MANY_KEYS = keyBuilder.build();
        MANY_VALUES = valueBuilder.build();
    }
    
    protected M mapEmpty;
    
    protected M mapSingleton;
    
    protected M mapThree;
    
    protected M mapLarge;
    
    @Before
    public void initMaps() {
        this.mapEmpty = empty();
        this.mapSingleton = put(empty(), "foo", 42);
        this.mapThree = buildMap(ImmutableCollections.listOf("one", "two", "three"), ImmutableCollections.listOf(1, 2, 3));
        this.mapLarge = buildMap(MANY_KEYS, MANY_VALUES);
    }
    
    protected abstract M empty();
    
    protected abstract M put(M map, String key, Integer value);
    
    protected boolean supportsRemove() {
        return true;
    }
    
    protected abstract M remove(M map, String key);
    
    protected abstract M clear(M map);
    
    private M buildMap(List<String> keys, List<Integer> values) {
        assertEquals(keys.size(), values.size());
        M map = empty();
        for (int i = 0; i < keys.size(); ++i) {
            map = put(map, keys.get(i), values.get(i));
        }
        return map;
    }
    
    @Test
    public void containsKey() {
        assertFalse(mapEmpty.containsKey(""));
        assertFalse(mapSingleton.containsKey("bar"));
        assertTrue(mapSingleton.containsKey("foo"));
        assertTrue(mapThree.containsKey("three"));
        assertFalse(mapThree.containsKey("four"));
        assertTrue(mapLarge.containsKey("42"));
        assertFalse(mapLarge.containsKey("bar"));
    }
    
    @Test
    public void get() {
        assertNull(mapEmpty.get("foo"));
        assertEquals((Integer)42, mapSingleton.get("foo"));
        assertNull(mapSingleton.get("bar"));
        
        assertEquals((Integer)1, mapThree.get("one"));
        assertEquals((Integer)2, mapThree.get("two"));
        assertEquals((Integer)3, mapThree.get("three"));
        assertNull(mapThree.get("four"));
        
        for (int i = 0; i < MANY_KEYS.size(); ++i) {
            assertEquals(MANY_VALUES.get(i), mapLarge.get(MANY_KEYS.get(i)));
        }
        assertNull(mapLarge.get("foo"));
    }
    
    @Test
    public void isEmpty() {
        assertTrue(mapEmpty.isEmpty());
        assertFalse(mapSingleton.isEmpty());
        assertFalse(mapThree.isEmpty());
        assertFalse(mapLarge.isEmpty());
    }
    
    @Test
    public void size() {
        assertEquals(0, mapEmpty.keyCount());
        assertEquals(1, mapSingleton.keyCount());
        assertEquals(3, mapThree.keyCount());
        assertEquals(MANY_KEYS.size(), mapLarge.keyCount());
    }
    
    @Test
    public void put() {
        M map = put(mapSingleton, "bar", 41);
        map = put(map, "moo", 0);
        map = put(map, "bar", 1);
        
        assertEquals(3, map.keyCount());
        verifyMap(map, listOf("foo", "bar", "moo"), listOf(42, 1, 0));
        
        M map4 = put(mapLarge, MANY_KEYS.get(4), MANY_VALUES.get(4));
        assertEquals(MANY_KEYS.size(), map4.keyCount());
        assertEquals(ImmutableCollections.asSet(MANY_KEYS), ImmutableCollections.asSet(map4.keys()));
        assertEquals(ImmutableCollections.asSet(MANY_VALUES), ImmutableCollections.asSet(map4.values()));
    }
    
    @Test(expected = NullPointerException.class)
    public void putNullKey() {
        put(mapThree, null, 1);
    }
    
    @Test(expected = NullPointerException.class)
    public void putNullValue() {
        put(mapThree, "two", null);
    }
    
    protected boolean isKeySetOrdered() {
        return true;
    }
    
    // TODO add explicit test for keySet()
    
    protected void verifyMap(Map<String, Integer> mapInTest, ImmutableList<String> expectedKeys, ImmutableList<Integer> expectedValues) {
        assertEquals(asSet(expectedKeys), asSet(mapInTest.keys()));
        for (int i = 0; i < expectedKeys.size(); ++i) {
            assertEquals(expectedValues.get(i), mapInTest.get(expectedKeys.get(i)));
        }
    }
    
    @Test
    public void values() {
        assertTrue(mapEmpty.values().isEmpty());
        assertEquals(setOf(42), asSet(mapSingleton.values()));
        assertEquals(setOf(1, 2, 3), asSet(mapThree.values()));
        assertEquals(asSet(MANY_VALUES), asSet(mapLarge.values()));
        
        assertEquals(MANY_VALUES.size(), mapLarge.values().size());
        assertEquals((Integer)42, mapSingleton.values().getFirstOrNull());
        assertNull(mapEmpty.values().getFirstOrNull());
        try {
            assertNull(mapEmpty.values().getFirst());
            TestUtil.failExpected(NoSuchElementException.class);
        } catch (NoSuchElementException expected) {}
    }
    
    @Test
    public void iterateValues() {
        assertEquals(setOf(), TestUtil.verifyIterable(mapEmpty.values(), HashSet.<Integer>newBuilder()));
        assertEquals(setOf(42), TestUtil.verifyIterable(mapSingleton.values(), HashSet.<Integer>newBuilder()));
        assertEquals(asSet(MANY_VALUES), TestUtil.verifyIterable(mapLarge.values(), HashSet.<Integer>newBuilder()));
    }
    
    @Test
    public void entryIterator() {
        verifyEntryIterator(mapEmpty.entryIterator(), ImmutableCollections.<String>listOf(), ImmutableCollections.<Integer>listOf());
        verifyEntryIterator(mapSingleton.entryIterator(), ImmutableCollections.listOf("foo"), ImmutableCollections.listOf(42));
        verifyEntryIterator(mapThree.entryIterator(), ImmutableCollections.listOf("one", "two", "three"), ImmutableCollections.listOf(1, 2, 3));
        verifyEntryIterator(mapLarge.entryIterator(), MANY_KEYS, MANY_VALUES);
    }
    
    private void verifyEntryIterator(EntryIterator<String, ? extends Integer> iterator, List<String> expectedKeys, List<Integer> expectedValues) {
        assertEquals(expectedKeys.size(), expectedValues.size());
        verifyInvalidPosition(iterator);
        
        ArrayList<String> actualKeys = new ArrayList<String>();
        ArrayList<Integer> actualValues = new ArrayList<Integer>();
        
        for (int i = 0; i < expectedKeys.size(); ++i) {
            assertTrue(iterator.next());
            String key = iterator.getCurrentKey();
            Integer value = iterator.getCurrentValue();
            actualKeys.push(key);
            actualValues.push(value);
            
            assertSame(key, iterator.getCurrentKey());
            assertSame(value, iterator.getCurrentValue());
        }
        assertFalse(iterator.next());
        // once at false it should stay at false
        assertFalse(iterator.next());
        assertFalse(iterator.next());
        verifyInvalidPosition(iterator);
        
        boolean[] covered = new boolean[expectedKeys.size()];
        
        // verify elements
        int i = 0;
        for (String actualkey: actualKeys) {
            int index = CollectionUtil.indexOf(expectedKeys, actualkey);
            if (index == -1 || covered[index]) {
                failWrongEntries(expectedKeys, expectedValues, actualKeys, actualValues);
            }
            covered[index] = true;
            if (!actualValues.get(i).equals(expectedValues.get(index))) {
                failWrongEntries(expectedKeys, expectedValues, actualKeys, actualValues);
            }
            i++;
        }
    }
    
    private void failWrongEntries(List<String> expectedKeys, List<Integer> expectedValues, ArrayList<String> actualKeys,
            ArrayList<Integer> actualValues) {
        fail("expected: (" + expectedKeys + "," + expectedValues + ") but was: (" + actualKeys + "," + actualValues + ")");
    }
    
    private void verifyInvalidPosition(EntryIterator<String, ? extends Integer> iterator) {
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
    public void verifyToString() {
        assertEquals("{}", mapEmpty.toString());
        assertEquals("{foo=42}", mapSingleton.toString());
        
        StringBuilder expected = new StringBuilder();
        expected.append("{");
        EntryIterator<String, ? extends Integer> iterator = mapThree.entryIterator();
        while (iterator.next()) {
            expected.append(iterator.getCurrentKey());
            expected.append("=");
            expected.append(iterator.getCurrentValue());
            expected.append(", ");
        }
        expected.setLength(expected.length() - 2);
        expected.append("}");
        assertEquals(expected.toString(), mapThree.toString());
    }
    
    @Test
    public void remove() {
        if (!supportsRemove()) {
            return;
        }
        assertSame(mapEmpty, remove(mapEmpty, "foo"));
        assertSame(mapLarge, remove(mapLarge, "foo"));
        
        assertEquals(mapEmpty, remove(mapSingleton, "foo"));
        assertSame(mapSingleton, remove(mapSingleton, "bar"));
        
        M map1 = remove(mapThree, "two");
        verifyMap(map1, listOf("one", "three"), listOf(1, 3));
        
        map1 = remove(map1, "one");
        verifyMap(map1, listOf("three"), listOf(3));
        
        map1 = remove(map1, "three");
        assertTrue(map1.isEmpty());
        
        M map4 = mapLarge;
        for (int i = 10; i < 100; ++i) {
            map4 = remove(map4, MANY_KEYS.get(i));
            M map5 = remove(map4, MANY_KEYS.get(i));
            assertSame(map4, map5);
        }
        assertEquals(MANY_KEYS.size() - 90, map4.keyCount());
        for (int i = 0; i < MANY_KEYS.size(); ++i) {
            if (i >= 10 && i < 100) {
                assertFalse(map4.containsKey(MANY_KEYS.get(i)));
            } else {
                assertEquals(MANY_VALUES.get(i), map4.get(MANY_KEYS.get(i)));
            }
        }
    }
    
    @Test
    public void clear() {
        assertSame(mapEmpty, clear(mapEmpty));
        
        M map = clear(mapLarge);
        assertTrue(map.isEmpty());
        assertTrue(map.isEmpty());
        assertEquals(0, map.keyCount());
        assertEquals(mapEmpty, map);
        
        map = clear(mapThree);
        assertEquals(mapEmpty, map);
        assertFalse(map.containsKey("one"));
        
        assertEquals(mapEmpty, clear(mapLarge));
    }
    
    private M copy(M map) {
        return copyIf(map, s -> true);
    }
    
    private M copyIf(M map, Predicate<String> keyFilter) {
        M result = empty();
        EntryIterator<String, ? extends Integer> it = map.entryIterator();
        while (it.next()) {
            if (keyFilter.test(it.getCurrentKey())) {
                result = put(result, it.getCurrentKey(), it.getCurrentValue());
            }
        }
        return result;
    }
    
    @Test
    public void equalsAndHashCode() {
        PersistentMap<String, Integer> defaultMap =
                PersistentHashMap.empty(mapEmpty.getKeyIdentificator(), mapEmpty.getValueIdentificator());
        
        verifyEqualsAndHashCode(mapEmpty, defaultMap, mapSingleton);
        verifyEqualsAndHashCode(mapSingleton, defaultMap.withAll(mapSingleton), mapThree);
        verifyEqualsAndHashCode(mapThree, defaultMap.withAll(mapThree), put(copy(mapThree), "one", 4));
        verifyEqualsAndHashCode(mapLarge, defaultMap.withAll(mapLarge), put(copyIf(mapLarge, s -> !s.equals(MANY_KEYS.get(42))), "hello", 1));
    }
    
    @Test
    public void equalsToOtherTypeMap() {
        PersistentMap<Object, Object> emptyOOMap = PersistentHashMap.<Object, Object>empty();
        
        EntryIterator<String, ? extends Integer> entryIterator = mapSingleton.entryIterator();
        assertTrue(entryIterator.next());
        
        PersistentMap<Object, Object> singleEntryOOMap = emptyOOMap.with(entryIterator.getCurrentKey(), entryIterator.getCurrentValue());
        if (mapSingleton.getKeyIdentificator().equals(singleEntryOOMap.getKeyIdentificator()) && mapSingleton.getValueIdentificator().equals(singleEntryOOMap.getValueIdentificator())) {
            assertTrue(mapEmpty.equals(emptyOOMap));
            assertTrue(mapSingleton.equals(singleEntryOOMap));
            assertTrue(mapEmpty.hashCode() == emptyOOMap.hashCode());
            assertTrue(mapSingleton.hashCode() == singleEntryOOMap.hashCode());
        } else {
            assertFalse(mapEmpty.equals(emptyOOMap));
            assertFalse(mapSingleton.equals(singleEntryOOMap));
        }
        
        singleEntryOOMap = emptyOOMap.with(new Object(), new Object());
        assertFalse(mapSingleton.equals(singleEntryOOMap));
        assertFalse(singleEntryOOMap.equals(mapSingleton));
    }
    
    private void verifyEqualsAndHashCode(M collectionInTest, Map<String, Integer> expectedEqual,
            Object expectedNotEqual) {
        M duplicate = copy(collectionInTest);
        
        assertFalse(collectionInTest.equals(null));
        assertFalse(collectionInTest.equals(new Object()));
        assertTrue(collectionInTest.equals(collectionInTest));
        assertTrue(collectionInTest.equals(expectedEqual));
        assertTrue(collectionInTest.equals(duplicate));
        assertFalse(collectionInTest.equals(expectedNotEqual));
        
        assertTrue(collectionInTest.hashCode() == collectionInTest.hashCode());
        assertTrue(collectionInTest.hashCode() == expectedEqual.hashCode());
        assertTrue(collectionInTest.hashCode() == duplicate.hashCode());
    }
    
}
