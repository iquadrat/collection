package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentIndexedSet;
import org.povworld.collection.persistent.PersistentIndexedSetImpl;
import org.povworld.collection.persistent.PersistentOrderedSet;

/**
 * Unit tests for {@link PersistentIndexedSetImpl}.
 */
public class PersistentIndexedSetTest extends AbstractPersistentOrderedSetTest<PersistentIndexedSet<String>> {
    
    public PersistentIndexedSetTest() {
        super(PersistentIndexedSetImpl.<String>newBuilder());
    }
    
    @Override
    protected OrderedSet<String> setOf(String... elements) {
        return ImmutableCollections.indexedSetOf(elements);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Override
    protected void checkInvariants(PersistentOrderedSet<?> set) {
        PersistentIndexedSetImpl<?> indexedSet = ObjectUtil.castOrNull(set, PersistentIndexedSetImpl.class);
        if (indexedSet != null) {
            indexedSet.checkInvariants();
        }
    }
    
    @Test
    public void get() {
        assertEquals("one", collectionThree.get(0));
        assertEquals("two", collectionThree.get(1));
        assertEquals("three", collectionThree.get(2));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void getNegativeIndex() {
        collectionLarge.get(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void getSize() {
        collectionLarge.get(collectionLarge.size());
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void emptySetWithOutElementAt() {
        collectionEmpty.withoutElementAt(0);
    }
    
    @Test
    public void withoutElementAt() {
        assertEquals(setOf(), collectionSingle.withoutElementAt(0));
        assertEquals(setOf("two", "three"), collectionThree.withoutElementAt(0));
        assertEquals(setOf("one", "three"), collectionThree.withoutElementAt(1));
        assertEquals(setOf("one", "two"), collectionThree.withoutElementAt(2));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withoutElementAtNegativeIndex() {
        collectionThree.withoutElementAt(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void withoutElementAtSize() {
        collectionThree.withoutElementAt(3);
    }
    
}
