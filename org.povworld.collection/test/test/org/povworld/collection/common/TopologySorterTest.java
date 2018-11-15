package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.povworld.collection.CollectionUtil.indexOf;

import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.common.TopologySorter;
import org.povworld.collection.common.TopologySorter.Topology;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashMultiMap;

public class TopologySorterTest {
    
    private class TestTopology implements Topology<String> {
        
        @Override
        public Iterable<String> getParents(String child) {
            return parents.get(child);
        }
        
    }
    
    private HashMultiMap<String, String> parents;
    
    private CollectionBuilder<String, ? extends List<String>> resultBuilder;
    
    @Before
    public void setUp() {
        parents = new HashMultiMap<>();
        resultBuilder = ArrayList.newBuilder();
    }
    
    @Test
    public void sortEmpty() {
        assertEquals(ImmutableCollections.<String>listOf(), sort());
    }
    
    @Test
    public void sortSingle() {
        assertEquals(ImmutableCollections.listOf("foo"), sort("foo"));
    }
    
    @Test
    public void sortTree() {
        // a -> b -> c
        //   \> d
        parents.put("b", "a");
        parents.put("d", "a");
        parents.put("c", "b");
        
        List<String> actual = sort("a", "b", "c", "d");
        assertEquals("a", actual.get(0));
        assertTrue(indexOf(actual, "b") < indexOf(actual, "c"));
        assertTrue(indexOf(actual, "d") < indexOf(actual, "c"));
        assertEquals("c", actual.get(3));
    }
    
    @Test
    public void sortMesh() {
        parents.put("a", "a");
        parents.putAll("c", CollectionUtil.wrap("a", "b"));
        parents.put("b", "a");
        parents.putAll("d", CollectionUtil.wrap("b", "c"));
        parents.putAll("e", CollectionUtil.wrap("b", "d", "a"));
        List<String> actual = sort("e");
        assertEquals(ImmutableCollections.listOf("a", "b", "c", "d", "e"), actual);
    }
    
    @Test
    public void sortCyclic() {
        parents.putAll("b", CollectionUtil.wrap("a", "c"));
        parents.putAll("c", CollectionUtil.wrap("a", "b"));
        parents.putAll("d", CollectionUtil.wrap("b"));
        
        List<String> actual = sort("d");
        assertEquals("a", actual.get(0));
        assertEquals("d", actual.get(3));
        assertTrue(CollectionUtil.contains(actual, "b"));
        assertTrue(CollectionUtil.contains(actual, "c"));
    }
    
    @Test
    public void sortMultiCycles() {
        parents.putAll("a", ImmutableCollections.listOf("r", "b"));
        parents.putAll("b", ImmutableCollections.listOf("r", "a"));
        parents.putAll("c", ImmutableCollections.listOf("r", "d", "a"));
        parents.putAll("d", ImmutableCollections.listOf("r", "c"));
        List<String> actual = sort("a", "b", "c", "d");
        assertEquals("r", actual.get(0));
        assertEquals(ImmutableCollections.setOf("a", "b", "c", "d", "r"), ImmutableCollections.asSet(actual));
        assertTrue("" + indexOf(actual, "a") + "<" + indexOf(actual, "c"), indexOf(actual, "a") < indexOf(actual, "c"));
    }
    
    private List<String> sort(String... roots) {
        return TopologySorter.sort(new TestTopology(), CollectionUtil.wrap(roots), resultBuilder);
    }
    
}
