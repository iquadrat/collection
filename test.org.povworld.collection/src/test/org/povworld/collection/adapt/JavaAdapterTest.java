package test.org.povworld.collection.adapt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.povworld.collection.adapt.JavaAdapters;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;

import test.org.povworld.collection.TestUtil;

public class JavaAdapterTest {
    
    @Test
    public void adaptList() {
        ArrayList<String> list = new ArrayList<>();
        List<String> adaptedList = JavaAdapters.asList(list);
        
        assertEquals(0, adaptedList.size());
        assertTrue(adaptedList.isEmpty());
        
        list.push("foo");
        list.push("bar");
        
        assertEquals(2, adaptedList.size());
        assertFalse(adaptedList.isEmpty());
        assertEquals("foo", adaptedList.get(0));
        assertEquals(Arrays.asList("foo", "bar"), adaptedList);
    }
    
    @Test
    public void adaptSet() {
        HashSet<String> set = new HashSet<>();
        Set<String> adaptedSet = JavaAdapters.asSet(set);
        
        assertEquals(0, adaptedSet.size());
        assertTrue(adaptedSet.isEmpty());
        assertFalse(adaptedSet.contains("foo"));
        
        set.add("foo");
        set.add("bar");
        
        assertEquals(2, adaptedSet.size());
        assertFalse(adaptedSet.isEmpty());
        assertTrue(adaptedSet.contains("foo"));
        assertEquals(new LinkedHashSet<String>(Arrays.asList("foo", "bar")), adaptedSet);
    }
    
    @Test
    public void adaptCollection() {
        ArrayList<String> list = new ArrayList<>();
        java.util.Collection<String> adaptedList = JavaAdapters.asCollection(list);
        
        assertEquals(0, adaptedList.size());
        assertTrue(adaptedList.isEmpty());
        
        list.push("foo");
        list.push("bar");
        
        assertEquals(2, adaptedList.size());
        assertFalse(adaptedList.isEmpty());
        ArrayList<String> actualElements = TestUtil.verifyIteratorStandardPattern(adaptedList.iterator(), 2, ArrayList.<String>newBuilder());
        assertEquals(list, actualElements);
        assertEquals(JavaAdapters.asCollection(actualElements), adaptedList);
        assertEquals(list.hashCode(), adaptedList.hashCode());
        assertFalse(JavaAdapters.asCollection(ImmutableCollections.setOf("foo", "bar")).equals(adaptedList));
        assertFalse(adaptedList.equals(new Object()));
        assertFalse(adaptedList.equals(null));
    }
    
    @Test
    public void adaptFastContainment() {
        HashSet<String> set = new HashSet<>();
        java.util.Collection<String> adaptedSet = JavaAdapters.asCollection(set);
        
        assertEquals(0, adaptedSet.size());
        assertTrue(adaptedSet.isEmpty());
        assertFalse(adaptedSet.contains("foo"));
        
        set.add("foo");
        set.add("bar");
        
        assertEquals(2, adaptedSet.size());
        assertFalse(adaptedSet.isEmpty());
        assertTrue(adaptedSet.contains("foo"));
        assertEquals(set.hashCode(), adaptedSet.hashCode());
        assertFalse(JavaAdapters.asCollection(ImmutableCollections.orderedSetOf("foo", "bar")).equals(adaptedSet));
    }
    
}
