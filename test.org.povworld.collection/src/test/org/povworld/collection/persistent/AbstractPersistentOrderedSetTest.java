package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeSet;
import org.povworld.collection.persistent.PersistentOrderedSet;
import org.povworld.collection.persistent.PersistentTreeSet;

import test.org.povworld.collection.AbstractOrderedSetTest;

public abstract class AbstractPersistentOrderedSetTest<C extends PersistentOrderedSet<String>> extends AbstractOrderedSetTest<C> {
    
    public AbstractPersistentOrderedSetTest(CollectionBuilder<String, C> builder) {
        super(builder);
    }
    
//  @Test
//  public void find() {
//    String foo1 = new String("11111");
//    String foo2 = new String("11111");
//    assertNotSame(foo1, foo2);
//    
//    PersistentTreeSet<String> set = (PersistentTreeSet<String>) build(foo1);
//    assertTrue(set.contains(foo1));
//    assertTrue(set.contains(foo2));
//    
//    assertSame(foo1, set.find(foo2));
//    assertSame(foo1, set.find(foo1));
//    assertNull(set.clear().find(foo2));
//    
//    set = set.remove(foo1).add(foo2);
//    assertSame(foo2, set.find(foo1));
//    
//    set = set.addAll(collectionLarge);
//    assertSame(foo2, set.find(foo1));
//    
//    set = set.add(foo1);
//    assertSame(foo2, set.find(foo1));
//    
//    set = set.remove(foo1);
//    set = set.add(foo1);
//    assertSame(foo1, set.find(foo2));
//  }
    
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
    
    private void checkInvariants(PersistentOrderedSet<?> set) {
        PersistentTreeSet<?, ?> treeSet = ObjectUtil.castOrNull(set, PersistentTreeSet.class);
        if (treeSet != null) {
            treeSet.checkInvariants();
        }
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return CollectionUtil.sort(ImmutableCollections.asList(elements));
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
    
    private OrderedSet<String> setOf(String... elements) {
        return TreeSet.newBuilder(String.class).addAll(CollectionUtil.wrap(elements)).build();
    }
    
    @Test
    public void cleared() {
        assertEquals(collectionEmpty, collectionLarge.cleared());
    }
    
}
