package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.povworld.collection.common.ReverseList;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;

import test.org.povworld.collection.TestUtil;

public class ReverseListTest {
    
    private final ReverseList<String> empty;
    
    private final ReverseList<String> three;
    
    public ReverseListTest() {
        empty = new ReverseList<>(ImmutableCollections.<String>listOf());
        three = new ReverseList<>(ImmutableCollections.listOf("a", "b", "c"));
    }
    
    @Test
    public void iterator() {
        ArrayList<String> actual = TestUtil.verifyIterable(three, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("c", "b", "a"), actual);
    }
    
    @Test
    public void size() {
        assertEquals(0, empty.size());
        assertEquals(3, three.size());
    }
    
    @Test
    public void getFristOrNull() {
        assertNull(empty.getFirstOrNull());
        assertEquals("c", three.getFirstOrNull());
    }
    
    @Test
    public void getLastOrNull() {
        assertNull(empty.getLastOrNull());
        assertEquals("a", three.getLastOrNull());
    }
    
    @Test
    public void reverseIterator() {
        ArrayList<String> actual = TestUtil.verifyIteratorStandardPattern(three.reverseIterator(), 3, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("a", "b", "c"), actual);
    }
    
    @Test
    public void get() {
        assertEquals("c", three.get(0));
        assertEquals("b", three.get(1));
        assertEquals("a", three.get(2));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void getIndexOutOfBounds() {
        three.get(3);
    }
    
    @Test
    public void iteratorRemoveNotSupported() {
        TestUtil.verifyIteratableRemoveUnsupported(three);
    }
    
}
