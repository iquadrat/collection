package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.List;
import org.povworld.collection.OrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.ReverseOrderedCollection;
import org.povworld.collection.immutable.ImmutableCollections;

public abstract class AbstractOrderedCollectionTest<C extends OrderedCollection<String>> extends AbstractStringCollectionTest<C> {
    
    public AbstractOrderedCollectionTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Test
    public void getLast() {
        assertEquals("three", collectionThree.getLast());
        assertEquals("three", collectionThree.getLastOrNull());
        
        assertEquals("foobar", collectionSingle.getLast());
        assertEquals("foobar", collectionSingle.getLastOrNull());
        
        assertEquals(manyElements[manyElements.length - 1], collectionLarge.getLast());
        assertEquals(manyElements[manyElements.length - 1], collectionLarge.getLastOrNull());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void getLastOfEmptyThrows() {
        assertNull(collectionEmpty.getLastOrNull());
        collectionEmpty.getLast();
    }
    
    @Test
    public void reverseIteration() {
        verifyReverseIterator(collectionEmpty, ImmutableCollections.<String>listOf());
        verifyReverseIterator(collectionSingle, ImmutableCollections.listOf("foobar"));
        verifyReverseIterator(collectionThree, ImmutableCollections.listOf("three", "two", "one"));
        
        String[] reverse = manyElements.clone();
        ArrayUtil.reverse(reverse);
        
        verifyReverseIterator(collectionLarge, ImmutableCollections.listOf(reverse));
    }
    
    private void verifyReverseIterator(C collectionInTest, List<String> expectedElements) {
        verifyIteratorStandardPattern(collectionInTest.reverseIterator(), expectedElements.size(), expectedElements);
        verifyIteratorNoHasNext(collectionInTest.reverseIterator(), expectedElements.size(), expectedElements);
        verifyIteratorMultipleHasNext(collectionInTest.reverseIterator(), expectedElements.size(), expectedElements);
    }
    
    @Test
    public void removeByReverseIterator() {
        TestUtil.verifyIteratableRemoveUnsupported(new ReverseOrderedCollection<>(collectionThree));
    }
    
    @Test
    public void string() {
        assertEquals("[]", collectionEmpty.toString());
        assertEquals("[foobar]", collectionSingle.toString());
        assertEquals("[one, two, three]", collectionThree.toString());
        assertEquals(Arrays.toString(manyElements), collectionLarge.toString());
    }
}
