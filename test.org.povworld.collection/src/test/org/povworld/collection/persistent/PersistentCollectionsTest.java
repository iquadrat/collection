package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.MutableCollections;
import org.povworld.collection.persistent.PersistentCollections;
import org.povworld.collection.persistent.PersistentList;

public class PersistentCollectionsTest {
    
    @Test
    public void createEmpty() {
        assertEquals(ImmutableCollections.indexedSetOf(), PersistentCollections.indexedSetOf());
        assertEquals(ImmutableCollections.orderedSetOf(), PersistentCollections.orderedSetOf());
        assertEquals(ImmutableCollections.listOf(), PersistentCollections.listOf());
        assertEquals(ImmutableCollections.setOf(), PersistentCollections.setOf());
        // TODO implement ImmutableTreeSet
        assertEquals(MutableCollections.treeSetOf(Integer.class), PersistentCollections.treeSetOf(Integer.class));
        assertEquals(MutableCollections.treeSetOf(Integer.class), PersistentCollections.treeSetOf(String.class));
        
        assertTrue(PersistentCollections.hashMapOf().isEmpty());
        assertTrue(PersistentCollections.mapOf().isEmpty());
        assertTrue(PersistentCollections.listMultiMapOf().isEmpty());
        assertTrue(PersistentCollections.multiMapOf().isEmpty());
        assertTrue(PersistentCollections.treeMapOf(String.class).isEmpty());
    }
    
    @Test
    public void createSingleton() {
        String e = "foobar";
        assertEquals(ImmutableCollections.indexedSetOf(e), PersistentCollections.indexedSetOf(e));
        assertEquals(ImmutableCollections.orderedSetOf(e), PersistentCollections.orderedSetOf(e));
        assertEquals(ImmutableCollections.listOf(e), PersistentCollections.listOf(e));
        assertEquals(ImmutableCollections.setOf(e), PersistentCollections.setOf(e));
        assertEquals(MutableCollections.treeSetOf(String.class, e), PersistentCollections.treeSetOf(String.class, e));
    }
    
    @Test
    public void createFromArray() {
        String[] e = new String[] {"one", "two", "three", "go!"};
        assertEquals(ImmutableCollections.<String>indexedSetOf(e), PersistentCollections.<String>indexedSetOf(e));
        assertEquals(ImmutableCollections.<String>orderedSetOf(e), PersistentCollections.<String>orderedSetOf(e));
        assertEquals(ImmutableCollections.<String>listOf(e), PersistentCollections.<String>listOf(e));
        assertEquals(ImmutableCollections.<String>setOf(e), PersistentCollections.<String>setOf(e));
        assertEquals(MutableCollections.treeSetOf(String.class, e), PersistentCollections.treeSetOf(String.class, e));
    }
    
    @Test
    public void createFromIterable() {
        Iterable<String> e = CollectionUtil.wrap("one", "two", "three", "go!");
        assertEquals(ImmutableCollections.<String>asIndexedSet(e), PersistentCollections.<String>asIndexedSet(e));
        assertEquals(ImmutableCollections.<String>asOrderedSet(e), PersistentCollections.<String>asOrderedSet(e));
        assertEquals(ImmutableCollections.<String>asList(e), PersistentCollections.<String>asList(e));
        assertEquals(ImmutableCollections.<String>asSet(e), PersistentCollections.<String>asSet(e));
        assertEquals(MutableCollections.asTreeSet(String.class, e), PersistentCollections.asTreeSet(String.class, e));
    }
    
    @Test
    public void createFromCollection() {
        Collection<String> e = CollectionUtil.wrap("one", "two", "three", "go!");
        assertEquals(ImmutableCollections.<String>asIndexedSet(e), PersistentCollections.<String>asIndexedSet(e));
        assertEquals(ImmutableCollections.<String>asOrderedSet(e), PersistentCollections.<String>asOrderedSet(e));
        assertEquals(ImmutableCollections.<String>asList(e), PersistentCollections.<String>asList(e));
        assertEquals(ImmutableCollections.<String>asSet(e), PersistentCollections.<String>asSet(e));
        assertEquals(MutableCollections.asTreeSet(String.class, e), PersistentCollections.asTreeSet(String.class, e));
    }
    
    @Test
    public void removeAll() {
        PersistentList<String> collectionThree = PersistentCollections.listOf("one", "two", "three");
        
        PersistentList<String> list0 = PersistentCollections.removeAll(collectionThree, ImmutableCollections.listOf("three", "one"));
        PersistentList<String> list1 = PersistentCollections.removeAll(list0, ImmutableCollections.listOf("one", "two"));
        
        assertEquals(ImmutableCollections.listOf("two"), list0);
        assertEquals(ImmutableCollections.<String>listOf(), list1);
        
        PersistentList<String> list2 = PersistentCollections.listOf("0", "1", "1", "0", "2", "1", "0", "3", "0");
        PersistentList<String> list3 = PersistentCollections.removeAll(list2, ImmutableCollections.listOf("0", "0", "1", "1", "0"));
        PersistentList<String> list4 = PersistentCollections.removeAll(list3, ImmutableCollections.listOf("5", "6"));
        
        assertEquals(ImmutableCollections.listOf("2", "1", "3", "0"), list3);
        assertSame(list3, list4);
    }
    
}
