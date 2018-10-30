package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.OrderedCollection;
import org.povworld.collection.UnOrderedCollection;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;

@SuppressWarnings("unchecked")
public abstract class AbstractCollectionTest<E, C extends Collection<E>> {
    
    protected final CollectionBuilder<E, ? extends C> builder;
    
    protected final C collectionEmpty;
    
    protected final C collectionSingle;
    
    protected final C collectionThree;
    
    protected final E[] manyElements;
    
    protected final C collectionLarge;
    
    protected final E one;
    
    protected final E two;
    
    protected final E three;
    
    protected final E zero;
    
    public AbstractCollectionTest(CollectionBuilder<E, ? extends C> builder, E[] elements) {
        assertEquals(1004, elements.length);
        this.builder = builder;
        this.zero = elements[0];
        this.one = elements[1];
        this.two = elements[2];
        this.three = elements[3];
        
        this.collectionEmpty = build();
        this.collectionSingle = build(zero);
        this.collectionThree = build(one, two, three);
        this.manyElements = Arrays.copyOfRange(elements, 4, 1004);
        assertEquals(1000, manyElements.length);
        this.collectionLarge = build(manyElements);
    }
    
    @CheckForNull
    protected abstract Iterable<E> expectedOrder(Iterable<E> elements);
    
    @CheckForNull
    protected Iterator<E> modifyingIterator(C collection) {
        return null;
    }
    
    protected abstract boolean allowsDuplicates();
    
    // This method is final as it takes a generic array of objects which
    // can lead to problems when it is overridden.
    protected final C build(E... elements) {
        C collection = builder
                .addAll(Arrays.asList(elements))
                .build();
        builder.reset();
        assertEquals(elements.length, collection.size());
        return collection;
    }
    
    @Test
    public void buildEmpty() {
        C collection = builder.build();
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void builder() {
        builder.add(one).add(two).add(three);
        C collection = builder.build();
        assertEquals(3, collection.size());
        
        builder
                .reset()
                .addAll(Arrays.asList(one, two, three))
                .addAll(collectionEmpty);
        C collection2 = builder.build();
        
        assertTrue(CollectionUtil.iteratesEqualSequence(collection, collection2));
    }
    
    @Test(expected = IllegalStateException.class)
    public void buildTwiceWithoutResetThrows() {
        builder.build();
        builder.build();
    }
    
    @Test
    public void builderResetWithoutBuild() {
        builder.add(one);
        builder.reset();
        C collection = builder.build();
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void builderDuplicate() {
        builder.add(one);
        builder.add(two);
        builder.add(one);
        builder.addAll(Arrays.asList(one, two));
        
        C collection = builder.build();
        int expectedSize = allowsDuplicates() ? 5 : 2;
        assertEquals(expectedSize, collection.size());
    }
    
    @Test
    public void iterate() {
        verifyIterator(collectionEmpty, 0, ImmutableCollections.<E>listOf());
        verifyIterator(collectionSingle, 1, ImmutableCollections.listOf(zero));
        verifyIterator(collectionThree, 3, expectedOrder(ImmutableCollections.listOf(one, two, three)));
        verifyIterator(collectionLarge, manyElements.length, expectedOrder(ImmutableCollections.listOf(manyElements)));
    }
    
    protected void verifyIterator(Iterable<E> iterable, int size, @CheckForNull Iterable<E> expectedSequence) {
        verifyIteratorStandardPattern(iterable.iterator(), size, expectedSequence);
        verifyIteratorNoHasNext(iterable.iterator(), size, expectedSequence);
        verifyIteratorMultipleHasNext(iterable.iterator(), size, expectedSequence);
    }
    
    protected void verifyIteratorStandardPattern(Iterator<E> iterator, int size, @CheckForNull Iterable<E> expectedSequence) {
        ArrayList<E> actualElements = new ArrayList<E>();
        for (int i = 0; i < size; ++i) {
            assertTrue("premature end of iteration, i=" + i, iterator.hasNext());
            actualElements.push(iterator.next());
        }
        if (iterator.hasNext()) {
            fail("iterable has more elements than exepected: " + iterator.next());
        }
        verifyIteratedElements(actualElements, expectedSequence);
        
        // calling next() after reaching end must throw error
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            // pass
        }
    }
    
    protected void verifyIteratorNoHasNext(Iterator<E> iterator, int size, @CheckForNull Iterable<E> expectedSequence) {
        ArrayList<E> actualElements = new ArrayList<E>();
        for (int i = 0; i < size; ++i) {
            actualElements.push(iterator.next());
        }
        if (iterator.hasNext()) {
            fail("iterable has more elements than exepected: " + iterator.next());
        }
        verifyIteratedElements(actualElements, expectedSequence);
    }
    
    protected Iterator<E> verifyIteratorMultipleHasNext(Iterator<E> iterator, int size, @CheckForNull Iterable<E> expectedSequence) {
        ArrayList<E> actualElements = new ArrayList<E>();
        for (int i = 0; i < size; ++i) {
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            actualElements.push(iterator.next());
        }
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        verifyIteratedElements(actualElements, expectedSequence);
        return iterator;
    }
    
    private void verifyIteratedElements(List<E> actualSequence, @CheckForNull Iterable<E> expectedSequence) {
        if (expectedSequence == null) return;
        if (!CollectionUtil.iteratesEqualSequence(actualSequence, expectedSequence)) {
            fail("expected: " + expectedSequence + " but was: " + actualSequence);
        }
    }
    
    @Test
    public void removeByIteratorFails() {
        TestUtil.verifyIteratableRemoveUnsupported(collectionThree);
    }
    
    @Test
    public void removeByModifyingIterator() {
        if (modifyingIterator(collectionThree) != null) {
            TestUtil.verifyRemoveByIterator(new Iterable<E>() {
                @Override
                public Iterator<E> iterator() {
                    return modifyingIterator(collectionThree);
                }
            });
        }
    }
    
    @Test
    public void size() {
        assertEquals(0, collectionEmpty.size());
        assertEquals(1, collectionSingle.size());
        assertEquals(3, collectionThree.size());
        assertEquals(manyElements.length, collectionLarge.size());
    }
    
    @Test(expected = NoSuchElementException.class)
    public void getFirstEmptyFails() {
        assertNull(collectionEmpty.getFirstOrNull());
        collectionEmpty.getFirst();
    }
    
    @Test
    public void getFirst() {
        verifyFirst(collectionSingle, CollectionUtil.wrap(zero));
        verifyFirst(collectionThree, CollectionUtil.wrap(one, two, three));
        verifyFirst(collectionLarge, CollectionUtil.wrap(manyElements));
    }
    
    private void verifyFirst(Collection<E> collection, Collection<E> elements) {
        E actual = collection.getFirst();
        if (expectedOrder(collection) != null) {
            E expected = CollectionUtil.firstElement(expectedOrder(elements));
            assertEquals(expected, actual);
        }
        assertEquals(collection.getFirstOrNull(), actual);
        assertEquals(CollectionUtil.firstElement(collection), actual);
    }
    
    @Test
    public void isEmpty() {
        assertTrue(collectionEmpty.isEmpty());
        assertFalse(collectionSingle.isEmpty());
        assertFalse(collectionThree.isEmpty());
        assertFalse(collectionLarge.isEmpty());
    }
    
    @Test
    public void equalsAndHashcode() {
        if (collectionThree instanceof UnOrderedCollection<?>) {
            verifyEqualsAndHashcodeForUnOrdered();
        } else if (collectionThree instanceof OrderedCollection<?>) {
            verifyEqualsAndHashcodeForOrdered();
        }
    }
    
    protected interface Hasher {
        
        public <E> int hash(Collection<E> objects);
        
    }
    
    private static class TestList<E> extends ArrayList<E> {
        
        private final Identificator<? super E> identificator;
        
        public TestList(Identificator<? super E> identificator) {
            this.identificator = identificator;
        }
        
        public TestList(Identificator<? super E> identificator, Iterable<E> elements) {
            this.identificator = identificator;
            pushAll(ImmutableCollections.asList(elements));
        }
        
        @Override
        public Identificator<? super E> getIdentificator() {
            return identificator;
        }
        
    }
    
    protected void verifyEqualsAndHashcodeForOrdered() {
        Identificator<? super E> identificator = ((OrderedCollection<E>)collectionEmpty).getIdentificator();
        Hasher hasher = new TestUtil.OrderedHasher();
        
        verifyEqualsAndHashCode(hasher, collectionEmpty, new TestList<E>(identificator), collectionLarge);
        verifyEqualsAndHashCode(hasher, collectionEmpty, new TestList<E>(identificator), ImmutableCollections.<E>setOf());
        if (expectedOrder(collectionThree) != null
                && !CollectionUtil.iteratesEqualSequence(expectedOrder(collectionThree), expectedOrder(build(two, one, three)))) {
            verifyEqualsAndHashCode(hasher, collectionThree, new TestList<E>(identificator, expectedOrder(collectionThree)), build(two, one, three));
        }
        verifyEqualsAndHashCode(hasher, collectionLarge, new TestList<E>(identificator, collectionLarge), collectionThree);
    }
    
    protected void verifyEqualsAndHashcodeForUnOrdered() {
        Hasher hasher = new TestUtil.UnOrderedHasher();
        
        verifyEqualsAndHashCode(hasher, collectionEmpty, ImmutableCollections.<E>setOf(), collectionLarge);
        verifyEqualsAndHashCode(hasher, collectionEmpty, ImmutableCollections.<E>setOf(), ImmutableCollections.<E>indexedSetOf());
        verifyEqualsAndHashCode(hasher, collectionThree, ImmutableCollections.setOf(two, one, three), build(zero, one, three));
        verifyEqualsAndHashCode(hasher, collectionLarge, ImmutableCollections.asSet(collectionLarge), collectionThree);
    }
    
    protected void verifyEqualsAndHashCode(Hasher hasher, C collectionInTest, Collection<E> expectedEqual, Collection<E> expectedNotEqual) {
        builder.addAll(collectionInTest);
        C duplicate = builder.build();
        builder.reset();
        
        assertFalse(collectionInTest.equals(null));
        assertFalse(collectionInTest.equals(new Object()));
        assertTrue(collectionInTest.equals(collectionInTest));
        assertTrue(collectionInTest.equals(expectedEqual));
        assertTrue(collectionInTest.equals(duplicate));
        assertFalse(collectionInTest.equals(expectedNotEqual));
        
        assertEquals(collectionInTest.hashCode(), collectionInTest.hashCode());
        assertEquals(collectionInTest.hashCode(), expectedEqual.hashCode());
        assertEquals(collectionInTest.hashCode(), duplicate.hashCode());
        assertEquals(hasher.hash(collectionInTest), collectionInTest.hashCode());
    }
    
}
