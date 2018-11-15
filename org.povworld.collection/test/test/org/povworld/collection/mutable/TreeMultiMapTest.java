package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.Set;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.TreeMultiMap;

import com.google.common.truth.Truth;

import test.org.povworld.collection.AbstractMultiMapTest;
import test.org.povworld.collection.TestUtil;

/**
 * Unit tests for {@link TreeMultiMap}
 */
public class TreeMultiMapTest extends AbstractMultiMapTest<Set<Integer>, TreeMultiMap<String, Integer>> {
    
    @Override
    protected TreeMultiMap<String, Integer> create() {
        return TreeMultiMap.create(String.class, Integer.class);
    }
    
    @Override
    protected TreeMultiMap<String, Integer> set(TreeMultiMap<String, Integer> map, String key, Collection<Integer> values) {
        map.remove(key);
        map.putAll(key, values);
        return map;
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        Truth.assertThat(actualValues).containsExactlyElementsIn(actualValues);
    }
    
    @Override
    protected void assertKeySet(Collection<String> expected, Collection<String> actual) {
        List<String> sortedExpectations = CollectionUtil.sort(expected);
        assertEquals(sortedExpectations, actual);
    }
    
    @Test
    public void removeKeyValue() {
        assertFalse(mapThreeKeys.remove("hello", 42));
        assertFalse(mapThreeKeys.remove("one", 0));
        
        assertTrue(mapThreeKeys.remove("one", 2));
        assertFalse(mapThreeKeys.containsKey("one"));
        assertEquals(2, mapThreeKeys.keyCount());
        assertEquals(5, mapThreeKeys.valueCount());
        
        assertTrue(mapThreeKeys.remove("three", 3));
        assertTrue(mapThreeKeys.remove("three", 2));
        assertEquals(ImmutableCollections.setOf(5), mapThreeKeys.get("three"));
        assertEquals(2, mapThreeKeys.keyCount());
        assertEquals(3, mapThreeKeys.valueCount());
    }
    
    @Test
    public void removeKey() {
        assertEquals(0, mapThreeKeys.remove("hello"));
        assertEquals(1, mapThreeKeys.remove("one"));
        assertEquals(3, mapThreeKeys.remove("three"));
        assertEquals(1, mapThreeKeys.keyCount());
        assertEquals(2, mapThreeKeys.valueCount());
    }
    
    @Test
    public void removeAll() {
        assertEquals(0, mapThreeKeys.removeAll("hello", ImmutableCollections.listOf(1,2,3,4)));
        assertEquals(1, mapThreeKeys.removeAll("one", ImmutableCollections.listOf(1,2,3,4)));
        assertEquals(2, mapThreeKeys.removeAll("three", ImmutableCollections.listOf(1,2,3,4)));
        assertEquals(ImmutableCollections.setOf(5), mapThreeKeys.get("three"));
        assertEquals(2, mapThreeKeys.keyCount());
        assertEquals(3, mapThreeKeys.valueCount());
    }
    
    @Test
    public void clear() {
        mapThreeKeys.clear();
        assertTrue(mapThreeKeys.isEmpty());
        assertEquals(0, mapThreeKeys.keyCount());
        assertEquals(0, mapThreeKeys.valueCount());
    }
    
    @Test
    public void iterateFlatValues() {
        ArrayList<Integer> iterated = TestUtil.verifyIterable(mapThreeKeys.flatValues(), ArrayList.<Integer>newBuilder());
        // Key order: one, three, two
        assertEquals(ImmutableCollections.listOf(2, 2, 3, 5, 2, 3), iterated);
    }
    
}
