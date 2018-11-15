package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.NoSuchElementException;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.ReverseOrderedCollection;
import org.povworld.collection.immutable.ImmutableCollections;

public abstract class AbstractOrderedSetTest<C extends OrderedSet<String>> extends AbstractNoDuplicatesTest<C> {
    
    public AbstractOrderedSetTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Test
    public void getLastOrNull() {
        assertNull(collectionEmpty.getLastOrNull());
        assertEquals("foobar", collectionSingle.getLastOrNull());
        assertEquals(CollectionUtil.lastElement(expectedOrder(collectionThree)), collectionThree.getLastOrNull());
        assertEquals(CollectionUtil.lastElement(expectedOrder(collectionLarge)), collectionLarge.getLastOrNull());
    }
    
    @Test
    public void getLast() {
        try {
            collectionEmpty.getLast();
            fail("expected " + NoSuchElementException.class.getSimpleName());
        } catch (NoSuchElementException e) {
            // pass
        }
        assertEquals("foobar", collectionSingle.getLast());
        assertEquals(CollectionUtil.lastElement(expectedOrder(collectionThree)), collectionThree.getLast());
        assertEquals(CollectionUtil.lastElement(expectedOrder(collectionLarge)), collectionLarge.getLast());
    }
    
    @Test
    public void reverseIterator() {
        verifyReverseIterator(collectionEmpty, ImmutableCollections.<String>listOf());
        verifyReverseIterator(collectionSingle, ImmutableCollections.asList(collectionSingle));
        verifyReverseIterator(collectionThree, ImmutableCollections.listOf(one, two, three));
        verifyReverseIterator(collectionLarge, ImmutableCollections.listOf(manyElements));
    }
    
    protected void verifyReverseIterator(OrderedSet<String> orderedSet, List<String> expectedElements) {
        Iterable<String> ordered = expectedOrder(expectedElements);
        Iterable<String> expectedSequence = null;
        if (ordered != null) {
            expectedSequence = new ReverseOrderedCollection<>(ImmutableCollections.asList(ordered));
        }
        verifyIteratorStandardPattern(orderedSet.reverseIterator(), expectedElements.size(), expectedSequence);
        verifyIteratorNoHasNext(orderedSet.reverseIterator(), expectedElements.size(), expectedSequence);
        verifyIteratorMultipleHasNext(orderedSet.reverseIterator(), expectedElements.size(), expectedSequence);
    }
    
}
