package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeList;

import test.org.povworld.collection.AbstractListTest;

/**
 * Unit tests for {@link TreeList}.
 */
public class TreeListTest extends AbstractListTest<TreeList<String>> {
    
    public TreeListTest() {
        super(TreeList.<String>newBuilder());
    }
    
    @Override
    protected Iterator<String> modifyingIterator(TreeList<String> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void contains() {
        assertFalse(collectionEmpty.contains("foo"));
        assertTrue(collectionSingle.contains("foobar"));
        assertTrue(collectionThree.contains("one"));
        assertFalse(collectionThree.contains("zero"));
        assertTrue(collectionLarge.contains("131"));
        assertFalse(collectionLarge.contains(""));
    }
    
    @Test
    public void indexOf() {
        assertEquals(-1, collectionEmpty.indexOf("foo"));
        assertEquals(0, collectionSingle.indexOf("foobar"));
        assertEquals(1, collectionThree.indexOf("two"));
        assertEquals(-1, collectionThree.indexOf("zero"));
        assertEquals(131, collectionLarge.indexOf("131"));
        assertEquals(-1, collectionLarge.indexOf(""));
        
        collectionThree.add("four");
        collectionThree.add("three");
        collectionThree.add("two");
        collectionThree.add("one");
        
        assertEquals(0, collectionThree.indexOf("one"));
        assertEquals(1, collectionThree.indexOf("two"));
        assertEquals(2, collectionThree.indexOf("three"));
        assertEquals(3, collectionThree.indexOf("four"));
        
        collectionThree.removeElementAt(0);
        collectionThree.removeElementAt(0);
        collectionThree.removeElementAt(0);
        
        assertEquals(3, collectionThree.indexOf("one"));
        assertEquals(2, collectionThree.indexOf("two"));
        assertEquals(1, collectionThree.indexOf("three"));
        assertEquals(0, collectionThree.indexOf("four"));
    }
    
    @Test
    public void lastIndexOf() {
        assertEquals(-1, collectionEmpty.lastIndexOf("foo"));
        assertEquals(0, collectionSingle.lastIndexOf("foobar"));
        assertEquals(1, collectionThree.lastIndexOf("two"));
        assertEquals(-1, collectionThree.lastIndexOf("zero"));
        assertEquals(131, collectionLarge.lastIndexOf("131"));
        assertEquals(-1, collectionLarge.lastIndexOf(""));
        
        collectionThree.add("four");
        collectionThree.add("three");
        collectionThree.add("two");
        collectionThree.add("one");
        
        // 1 2 3 4 3 2 1 
        
        assertEquals(6, collectionThree.lastIndexOf("one"));
        assertEquals(5, collectionThree.lastIndexOf("two"));
        assertEquals(4, collectionThree.lastIndexOf("three"));
        assertEquals(3, collectionThree.lastIndexOf("four"));
        
        collectionThree.removeElementAt(5);
        collectionThree.removeElementAt(3);
        collectionThree.removeElementAt(0);
        
        // 2 3 3 1 
        
        assertEquals(3, collectionThree.lastIndexOf("one"));
        assertEquals(0, collectionThree.lastIndexOf("two"));
        assertEquals(2, collectionThree.lastIndexOf("three"));
        assertEquals(-1, collectionThree.lastIndexOf("four"));
    }
    
    @Test
    public void addAll() {
        TreeList<String> list = new TreeList<String>();
        list.addAll(collectionThree);
        assertEquals(collectionThree, list);
        
        list.addAll(ImmutableCollections.listOf("1", "2", "3"));
        assertEquals(ImmutableCollections.listOf("one", "two", "three", "1", "2", "3"), list);
    }
    
    @Test
    public void remove() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1));
        assertTrue(list.remove(1));
        assertTrue(list.remove(3));
        assertEquals(ImmutableCollections.listOf(1, 2, 5, 3, 2, 1, 1), list);
        assertFalse(list.remove(4));
        assertTrue(list.remove(3));
        assertFalse(list.remove(3));
        assertEquals(ImmutableCollections.listOf(1, 2, 5, 2, 1, 1), list);
    }
    
    @Test
    public void removeAll() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1));
        list.removeAll(ImmutableCollections.listOf(5, 3, 2, 1, 4, 1, 6, 1, 8));
        assertEquals(ImmutableCollections.listOf(3, 2, 1), list);
    }
    
    @Test
    public void set() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1));
        list.set(0, 7);
        list.set(4, 3);
        list.set(3, 4);
        assertEquals(ImmutableCollections.listOf(7, 1, 2, 4, 3, 3, 2, 1, 1), list);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void setIndexOutOfBounds() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1));
        list.set(list.size(), 2);
    }
    
    @Test
    public void replaceAll() {
        TreeList<Integer> list = TreeList.<Integer>newBuilder()
                .addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1))
                .build();
        
        assertEquals(0, list.replaceAll(9, 2));
        
        list.replaceAll(2, 4);
        assertEquals(ImmutableCollections.listOf(1, 1, 4, 3, 5, 3, 4, 1, 1), list);
        
        list.replaceAll(4, 5);
        assertEquals(ImmutableCollections.listOf(1, 1, 5, 3, 5, 3, 5, 1, 1), list);
        assertEquals(2, list.indexOf(5));
        assertEquals(6, list.lastIndexOf(5));
    }
    
    @Test
    public void clear() {
        TreeList<Integer> list = new TreeList<>();
        list.addAll(ImmutableCollections.listOf(1, 1, 2, 3, 5, 3, 2, 1, 1));
        list.clear();
        assertEquals(collectionEmpty, list);
    }
    
}
