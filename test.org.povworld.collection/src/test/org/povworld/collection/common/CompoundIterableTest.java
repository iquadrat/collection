package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.common.CompoundIterable;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableList;
import org.povworld.collection.mutable.ArrayList;

import test.org.povworld.collection.TestUtil;

public class CompoundIterableTest {
    
    @Test
    public void none() {
        Iterable<String> none = CompoundIterable.<String>create();
        
        ArrayList<String> elements = TestUtil.verifyIterable(none, ArrayList.<String>newBuilder());
        assertTrue(elements.isEmpty());
    }
    
    @Test
    public void singleEmpty() {
        ImmutableList<String> iterable = ImmutableCollections.<String>listOf();
        
        Iterable<String> none = CompoundIterable.create(iterable);
        
        ArrayList<String> elements = TestUtil.verifyIterable(none, ArrayList.<String>newBuilder());
        assertTrue(elements.isEmpty());
    }
    
    @Test
    public void singleOfSingle() {
        ImmutableList<String> iterable = ImmutableCollections.listOf("one");
        
        Iterable<String> none = CompoundIterable.create(iterable);
        
        ArrayList<String> elements = TestUtil.verifyIterable(none, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("one"), elements);
    }
    
    @Test
    public void multiEmpty() {
        ImmutableList<String> iterable1 = ImmutableCollections.<String>listOf();
        ImmutableList<String> iterable2 = ImmutableCollections.<String>listOf();
        ImmutableList<String> iterable3 = ImmutableCollections.<String>listOf();
        
        Iterable<String> none = CompoundIterable.create(iterable1, iterable2, iterable3);
        
        ArrayList<String> elements = TestUtil.verifyIterable(none, ArrayList.<String>newBuilder());
        assertTrue(elements.isEmpty());
    }
    
    @Test
    public void multiMixed() {
        ImmutableList<String> iterable1 = ImmutableCollections.listOf("one", "two", "three");
        ImmutableList<String> iterable2 = ImmutableCollections.listOf("four");
        ImmutableList<String> iterable3 = ImmutableCollections.listOf();
        ImmutableList<String> iterable4 = ImmutableCollections.listOf("five", "six");
        
        Iterable<String> none = CompoundIterable.create(Arrays.asList(iterable1, iterable2, iterable3, iterable4));
        
        ArrayList<String> elements = TestUtil.verifyIterable(none, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("one", "two", "three", "four", "five", "six"), elements);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void removeNotSupported() {
        Iterator<String> iterator = CompoundIterable.create(Arrays.asList("foo")).iterator();
        iterator.remove();
    }
    
}
