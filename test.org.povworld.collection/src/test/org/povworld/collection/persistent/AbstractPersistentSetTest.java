package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.povworld.collection.immutable.ImmutableCollections.listOf;

import java.util.Arrays;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentSet;

import test.org.povworld.collection.AbstractSetTest;

public abstract class AbstractPersistentSetTest<C extends PersistentSet<String>> extends AbstractSetTest<C> {
    
    public AbstractPersistentSetTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Test
    public void with() {
        PersistentSet<String> set0 = collectionEmpty;
        PersistentSet<String> set1 = set0.with("hello");
        PersistentSet<String> set2 = set1.with("foo");
        
        assertTrue(set0.isEmpty());
        assertEquals(ImmutableCollections.setOf("hello"), set1);
        assertEquals(ImmutableCollections.setOf("hello", "foo"), set2);
    }
    
    @Test
    public void withExisting() {
        assertSame(collectionSingle, collectionSingle.with(collectionSingle.getFirst()));
        assertSame(collectionThree, collectionThree.with("three"));
        assertSame(collectionLarge, collectionLarge.with(manyElements[44]));
    }
    
    @Test(expected = NullPointerException.class)
    public void withNullThrowsNPE() {
        collectionThree.with(null);
    }
    
    @Test
    public void withAll() {
        PersistentSet<String> set1 = collectionEmpty.withAll(ImmutableCollections.listOf("1", "2", "3"));
        PersistentSet<String> set2 = set1.withAll(ImmutableCollections.listOf("4", "1", "5"));
        PersistentSet<String> set3 = set2.withAll(ImmutableCollections.<String>listOf());
        PersistentSet<String> set4 = set2.withAll(ImmutableCollections.listOf("1", "4"));
        PersistentSet<String> set5 = set1.withAll(ImmutableCollections.listOf("1", "1", "1", "1", "1", "1", "1"));
        
        assertEquals(ImmutableCollections.setOf("1", "2", "3"), set1);
        assertEquals(ImmutableCollections.setOf("1", "2", "3", "4", "5"), set2);
        assertSame(set2, set3);
        assertSame(set2, set4);
        assertSame(set1, set5);
    }
    
    @Test(expected = NullPointerException.class)
    public void withAllWithNullThrowsNPE() {
        collectionThree.withAll(listOf("one", "four", null));
    }
    
    @Test
    public void cleared() {
        assertSame(collectionEmpty, collectionEmpty.cleared());
        assertEquals(collectionEmpty, collectionSingle.cleared());
        assertEquals(collectionEmpty, collectionThree.cleared());
        assertEquals(collectionEmpty, collectionLarge.cleared());
    }
    
    @Test
    public void withoutInexistingElement() {
        assertSame(collectionEmpty, collectionEmpty.without("foo"));
        assertSame(collectionSingle, collectionSingle.without("bar"));
        assertSame(collectionLarge, collectionLarge.without("bar"));
        assertSame(collectionLarge, collectionLarge.without("foo"));
    }
    
    @Test
    public void withoutExistingElement() {
        PersistentSet<String> set1 = collectionSingle.without(collectionSingle.getFirst());
        assertEquals(ImmutableCollections.<String>setOf(), set1);
        
        PersistentSet<String> set2 = collectionThree.without("two");
        PersistentSet<String> set3 = set2.without("three");
        PersistentSet<String> set4 = set3.without("one");
        assertEquals(ImmutableCollections.setOf("one", "three"), set2);
        assertEquals(ImmutableCollections.setOf("one"), set3);
        assertTrue(set4.isEmpty());
        
        PersistentSet<String> set5 = collectionLarge;
        int size = collectionLarge.size();
        for (String element: manyElements) {
            PersistentSet<String> newSet = set5.without(element);
            assertFalse("Element " + element + " was not removed!", newSet == set5);
            set5 = newSet;
            assertEquals(--size, set5.size());
        }
        assertTrue(set5.isEmpty());
    }
    
    @Test(expected = NullPointerException.class)
    public void withoutThrowsNPE() {
        collectionEmpty.without(null);
    }
    
    @Test
    public void withoutAll() {
        PersistentSet<String> set1 = collectionThree.withoutAll(Arrays.asList(manyElements));
        assertSame(collectionThree, set1);
        
        PersistentSet<String> set2 = collectionThree.withoutAll(Arrays.asList("two", "three"));
        assertEquals(ImmutableCollections.setOf("one"), set2);
        
        PersistentSet<String> set3 = collectionLarge.withoutAll(Arrays.asList(Arrays.copyOfRange(manyElements, 10, 42)));
        assertEquals(manyElements.length - 32, set3.size());
        assertTrue(set3.contains(manyElements[9]));
        assertTrue(set3.contains(manyElements[42]));
        assertFalse(set3.contains(manyElements[15]));
        
        assertEquals(collectionEmpty, set3.withoutAll(collectionLarge));
        PersistentSet<String> collectionThreeWithLarge = collectionLarge.withAll(collectionThree);
        assertEquals(collectionEmpty, collectionThree.withoutAll(collectionThreeWithLarge));
        assertEquals(collectionEmpty, collectionLarge.withoutAll(collectionThreeWithLarge));
    }
    
    @Test(expected = NullPointerException.class)
    public void withoutAllWithNullThrowsNPE() {
        collectionThree.withoutAll(listOf("one", "four", null));
    }
    
}
