package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentArrayList;
import org.povworld.collection.persistent.PersistentList;

import test.org.povworld.collection.AbstractListTest;

public abstract class AbstractPersistentListTest<C extends PersistentList<String>> extends AbstractListTest<C> {
    
    public AbstractPersistentListTest(CollectionBuilder<String, C> builder) {
        super(builder);
    }
    
    @Test
    public void with() {
        PersistentList<String> list0 = PersistentArrayList.<String>empty();
        PersistentList<String> list1 = list0.with("hello");
        PersistentList<String> list2 = list1.with("foo");
        
        assertTrue(list0.isEmpty());
        assertEquals(ImmutableCollections.listOf("hello"), list1);
        assertEquals(ImmutableCollections.listOf("hello", "foo"), list2);
        
        PersistentList<String> list3 = list2.with("middle", 1);
        PersistentList<String> list4a = list3.with("first", 0);
        PersistentList<String> list4b = list3.with("last", 3);
        
        assertEquals(ImmutableCollections.listOf("hello", "middle", "foo"), list3);
        assertEquals(ImmutableCollections.listOf("first", "hello", "middle", "foo"), list4a);
        assertEquals(ImmutableCollections.listOf("hello", "middle", "foo", "last"), list4b);
        
        PersistentList<String> list5 = list2.with("foo");
        assertEquals(ImmutableCollections.listOf("hello", "foo", "foo"), list5);
    }
    
    @Test
    public void withAll() {
        PersistentList<String> list1 = collectionEmpty.withAll(ImmutableCollections.listOf("1", "2", "3"));
        PersistentList<String> list2 = list1.withAll(ImmutableCollections.listOf("4", "1"));
        PersistentList<String> list3 = list2.withAll(ImmutableCollections.<String>listOf());
        
        assertEquals(ImmutableCollections.listOf("1", "2", "3"), list1);
        assertEquals(ImmutableCollections.listOf("1", "2", "3", "4", "1"), list2);
        assertSame(list2, list3);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withIndexTooLarge() {
        collectionThree.with("four", 4);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withNegativeIndex() {
        collectionThree.with("four", -1);
    }
    
    @Test(expected = NullPointerException.class)
    public void withNullThrowsNPE() {
        collectionThree.with(null);
    }
    
    @Test
    public void withReplacementAt() {
        PersistentList<String> list0 = collectionThree.withReplacementAt("four", 2);
        PersistentList<String> list1 = list0.withReplacementAt("zero", 0);
        PersistentList<String> listTest = list0.with("zero", 0);
        
        assertEquals(ImmutableCollections.listOf("one", "two", "four"), list0);
        assertEquals(ImmutableCollections.listOf("zero", "two", "four"), list1);
        assertEquals(ImmutableCollections.listOf("zero", "one", "two", "four"), listTest);
        
        assertSame(list1, list1.withReplacementAt(list1.get(0), 0));
        assertSame(collectionLarge, collectionLarge.withReplacementAt(collectionLarge.get(87), 87));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withReplacementAtIndexTooLarge() {
        collectionThree.withReplacementAt("four", 3);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withReplacementAtNegativeIndex() {
        collectionThree.withReplacementAt("four", -1);
    }
    
    @Test
    public void without() {
        PersistentList<String> list0 = collectionThree.without(1);
        PersistentList<String> list1a = list0.without(0);
        PersistentList<String> list1b = list0.without(1);
        
        assertEquals(ImmutableCollections.listOf("one", "three"), list0);
        assertEquals(ImmutableCollections.listOf("three"), list1a);
        assertEquals(ImmutableCollections.listOf("one"), list1b);
        
        String element = collectionLarge.get(666);
        PersistentList<String> list2 = collectionLarge.without(666);
        assertEquals(999, list2.size());
        assertEquals(-1, CollectionUtil.indexOf(list2, element));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withoutIndexTooLarge() {
        collectionThree.without(3);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withoutNegativeIndex() {
        collectionThree.without(-1);
    }
    
    @Test
    public void withAllReplaced() {
        PersistentList<String> list0 = collectionEmpty.withAll(ImmutableCollections.listOf("0", "1", "1", "0", "2", "1", "0", "3", "0"));
        PersistentList<String> list1 = list0.withAllReplaced("1", "L");
        PersistentList<String> list2 = list0.withAllReplaced("A", "B");
        
        assertEquals(ImmutableCollections.listOf("0", "L", "L", "0", "2", "L", "0", "3", "0"), list1);
        assertSame(list2, list0);
    }
    
    @Test
    public void cleared() {
        PersistentList<String> list1 = collectionThree.cleared();
        assertTrue(list1.isEmpty());
        assertEquals(collectionEmpty, list1);
        assertEquals(3, collectionThree.size());
        
        assertSame(list1, list1.cleared());
    }
    
}
