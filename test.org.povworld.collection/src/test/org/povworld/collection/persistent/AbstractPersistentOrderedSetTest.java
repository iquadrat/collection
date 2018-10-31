package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentOrderedSet;

import test.org.povworld.collection.AbstractOrderedSetTest;

public abstract class AbstractPersistentOrderedSetTest<C extends PersistentOrderedSet<String>> extends AbstractOrderedSetTest<C> {
    
    public AbstractPersistentOrderedSetTest(CollectionBuilder<String, C> builder) {
        super(builder);
    }
    
    @Test
    public void findEqualOrNull() {
        String foo1 = new String("11111");
        String foo2 = new String("11111");
        assertNotSame(foo1, foo2);
        
        PersistentOrderedSet<String> set = build(foo1);
        assertTrue(set.contains(foo1));
        assertTrue(set.contains(foo2));
        
        assertSame(foo1, set.findEqualOrNull(foo2));
        assertSame(foo1, set.findEqualOrNull(foo1));
        assertNull(set.cleared().findEqualOrNull(foo2));
        
        set = set.without(foo1).with(foo2);
        assertSame(foo2, set.findEqualOrNull(foo1));
        
        set = set.withAll(collectionLarge);
        assertSame(foo2, set.findEqualOrNull(foo1));
        
        set = set.with(foo1);
        assertSame(foo2, set.findEqualOrNull(foo1));
        
        set = set.without(foo1);
        set = set.with(foo1);
        assertSame(foo1, set.findEqualOrNull(foo2));
    }
    
    @Test
    public void checkInvariants() {
        PersistentOrderedSet<String> set = build();
        checkInvariants(set);
        
        for (String element: manyElements) {
            set = set.with(element);
            checkInvariants(set);
        }
        assertEquals(manyElements.length, set.size());
        
        for (String element: manyElements) {
            set = set.without(element);
            checkInvariants(set);
        }
        assertEquals(0, set.size());
    }
    
    protected void checkInvariants(PersistentOrderedSet<?> set) {}
    
    @Test
    public void with() {
        PersistentOrderedSet<String> set0 = collectionEmpty;
        PersistentOrderedSet<String> set1 = set0.with("hello");
        PersistentOrderedSet<String> set2 = set1.with("foo");
        
        assertTrue(set0.isEmpty());
        assertEquals(setOf("hello"), set1);
        assertEquals(setOf("hello", "foo"), set2);
        
        assertSame(set2, set2.with("foo"));
    }
    
    @Test
    public void withAll() {
        PersistentOrderedSet<String> list1 = collectionEmpty.withAll(ImmutableCollections.listOf("1", "2", "3"));
        PersistentOrderedSet<String> list2 = list1.withAll(ImmutableCollections.listOf("4", "1"));
        assertSame(list2, list2.withAll(ImmutableCollections.<String>listOf()));
        
        assertEquals(setOf("1", "2", "3"), list1);
        assertEquals(setOf("1", "2", "3", "4", "1"), list2);
    }
    
    
    @Test
    public void withoutNonExisting() {
        assertSame(collectionEmpty, collectionEmpty.without("NotPresent"));
        assertSame(collectionSingle, collectionSingle.without("NotPresent"));
        assertSame(collectionThree, collectionThree.without("NotPresent"));
        assertSame(collectionLarge, collectionLarge.without("NotPresent"));
    }
    
    @Test
    public void withoutExisting() {
        assertEquals(collectionEmpty, collectionSingle.without(zero));
        assertEquals(setOf(one, three), collectionThree.without("two"));
        
        int expectedSize = manyElements.length;
        PersistentOrderedSet<String> set = collectionLarge;
        for (String element: CollectionUtil.shuffle(ImmutableCollections.listOf(manyElements), new Random(1))) {
            if (element == manyElements[32]) continue;
            PersistentOrderedSet<String> newSet = set.without(element);
            expectedSize--;
            
            assertEquals(expectedSize, newSet.size());
            assertTrue(set.contains(element));
            assertFalse(newSet.contains(element));
            assertTrue(newSet.contains(manyElements[32]));
            set = newSet;
        }
        
        set = set.without(manyElements[32]);
        assertFalse(set.contains(manyElements[32]));
        assertTrue(set.isEmpty());
    }
    
    @Test
    public void withoutAllEmpty() {
        assertSame(collectionEmpty, collectionEmpty.withoutAll(collectionEmpty));
        assertSame(collectionSingle, collectionSingle.withoutAll(collectionEmpty));
        assertSame(collectionThree, collectionThree.withoutAll(collectionEmpty));
        assertSame(collectionLarge, collectionLarge.withoutAll(collectionEmpty));
    }
    
    @Test
    public void withoutAllNonExisiting() {
        List<String> elements = ImmutableCollections.listOf("not present", "missing");
        
        assertSame(collectionEmpty, collectionEmpty.withoutAll(elements));
        assertSame(collectionSingle, collectionSingle.withoutAll(elements));
        assertSame(collectionThree, collectionThree.withoutAll(elements));
        assertSame(collectionLarge, collectionLarge.withoutAll(elements));
    }
    
    @Test
    public void withoutAllItself() {
        assertEquals(collectionEmpty, collectionSingle.withoutAll(collectionSingle));
        assertEquals(collectionEmpty, collectionThree.withoutAll(collectionThree));
        assertEquals(collectionEmpty, collectionLarge.withoutAll(collectionLarge));
    }
    
    @Test
    public void withoutAll() {
        assertEquals(setOf("one"), collectionThree.withoutAll(ImmutableCollections.listOf("four", "three", "five", "two", "six")));
    }
    
    protected abstract OrderedSet<String> setOf(String... elements);
    
    @Test
    public void cleared() {
        assertEquals(collectionEmpty, collectionLarge.cleared());
    }
    
}
