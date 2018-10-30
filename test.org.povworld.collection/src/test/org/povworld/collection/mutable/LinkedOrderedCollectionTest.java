package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.junit.Test;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.LinkedOrderedCollection;

import test.org.povworld.collection.AbstractOrderedCollectionTest;

/**
 * Unit tests for {@link LinkedOrderedCollection}.
 */
public class LinkedOrderedCollectionTest extends AbstractOrderedCollectionTest<LinkedOrderedCollection<String>> {
    
    public LinkedOrderedCollectionTest() {
        super(LinkedOrderedCollection.newBuilder());
    }
    
    @Override
    @CheckForNull
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Override
    protected Iterator<String> modifyingIterator(LinkedOrderedCollection<String> collection) {
        return collection.modifyingIterator();
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Test
    public void insertFrontBack() {
        collectionEmpty.insertFront("foo");
        collectionEmpty.insertBack("bla");
        collectionEmpty.insertFront("moo");
        collectionEmpty.insertBack("naa");
        assertEquals(ImmutableCollections.listOf("moo", "foo", "bla", "naa"), collectionEmpty);
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeFirstEmptyThrows() {
        collectionEmpty.removeFirst();
    }
    
    @Test
    public void removeFirst() {
        assertEquals("one", collectionThree.removeFirst());
        assertEquals("two", collectionThree.removeFirst());
        assertEquals("three", collectionThree.removeFirst());
        assertTrue(collectionThree.isEmpty());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void removeLastEmptyThrows() {
        collectionEmpty.removeLast();
    }
    
    @Test
    public void removeLast() {
        assertEquals("three", collectionThree.removeLast());
        assertEquals("two", collectionThree.removeLast());
        assertEquals("one", collectionThree.removeLast());
        assertTrue(collectionThree.isEmpty());
    }
    
}
