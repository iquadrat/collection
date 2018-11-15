package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.HashSet;

import test.org.povworld.collection.AbstractCollectionTest.Hasher;

public class TestUtil {
    
    public static class OrderedHasher implements Hasher {
        
        @Override
        public <E> int hash(Collection<E> objects) {
            Identificator<? super E> identificator = objects.getIdentificator();
            int hashCode = 1;
            for (E object: objects) {
                hashCode = 31 * hashCode + identificator.hashCode(object);
            }
            return hashCode;
        }
        
    }
    
    public static class UnOrderedHasher implements Hasher {
        
        @Override
        public <E> int hash(Collection<E> objects) {
            Identificator<? super E> identificator = objects.getIdentificator();
            int hashCode = 0;
            for (E object: objects) {
                hashCode += identificator.hashCode(object);
            }
            return hashCode;
        }
        
    }
    
    private TestUtil() {}
    
    /**
     * Verifies the behavior of {@link Iterator}s created from the given {@link Iterable}.
     * Uses the given {@link CollectionBuilder} to create a collection with all the elements iterated by the iterable.
     * @return a collection with all the elements iterated
     */
    public static <E, C extends Collection<E>, B extends CollectionBuilder<E, C>> C verifyIterable(Iterable<E> iterable, B collectionBuilder) {
        int size = CollectionUtil.sizeOf(iterable);
        
        C elements1 = verifyIteratorStandardPattern(iterable.iterator(), size, collectionBuilder);
        C elements2 = verifyIteratorNoHasNext(iterable.iterator(), size, collectionBuilder);
        C elements3 = verifyIteratorMultipleHasNext(iterable.iterator(), size, collectionBuilder);
        
        assertEquals(elements1, elements2);
        assertEquals(elements1, elements3);
        
        return elements1;
    }
    
    public static <E, C extends Collection<E>, B extends CollectionBuilder<E, C>> C verifyIteratorStandardPattern(Iterator<E> iterator, int size,
            B collectionBuilder) {
        collectionBuilder.reset();
        for (int i = 0; i < size; ++i) {
            assertTrue("premature end of iteration, i=" + i, iterator.hasNext());
            collectionBuilder.add(iterator.next());
        }
        if (iterator.hasNext()) {
            fail("iterable has more elements than exepected: " + iterator.next());
        }
        
        // calling next() after reaching end must throw error
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch (NoSuchElementException e) {
            // pass
        }
        
        return collectionBuilder.build();
    }
    
    public static <E, C extends Collection<E>, B extends CollectionBuilder<E, C>> C verifyIteratorNoHasNext(Iterator<E> iterator, int size,
            B collectionBuilder) {
        collectionBuilder.reset();
        for (int i = 0; i < size; ++i) {
            collectionBuilder.add(iterator.next());
        }
        if (iterator.hasNext()) {
            fail("iterable has more elements than exepected: " + iterator.next());
        }
        return collectionBuilder.build();
    }
    
    public static <E, C extends Collection<E>, B extends CollectionBuilder<E, C>> C verifyIteratorMultipleHasNext(Iterator<E> iterator, int size,
            B collectionBuilder) {
        collectionBuilder.reset();
        for (int i = 0; i < size; ++i) {
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            assertTrue(iterator.hasNext());
            collectionBuilder.add(iterator.next());
        }
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
        return collectionBuilder.build();
    }
    
    public static <E> void verifyRemoveByIterator(Iterable<E> iterable) {
        PreConditions.paramCheck(iterable, "Must have exactly 3 elements", CollectionUtil.sizeOf(iterable) == 3);
        
        Iterator<E> iterator = iterable.iterator();
        try {
            iterator.remove();
            failExpected(IllegalStateException.class);
        } catch (IllegalStateException e) {
            // pass
        }
        
        HashSet<E> expected = HashSet.<E>newBuilder(3).addAll(iterable).build();
        assertTrue(iterator.hasNext());
        assertTrue(expected.remove(iterator.next()));
        iterator.remove();
        assertEquals(expected, ImmutableCollections.asSet(iterable));
        
        try {
            iterator.remove();
            failExpected(IllegalStateException.class);
        } catch (IllegalStateException e) {
            // pass
        }
        
        iterator.next();
        assertTrue(expected.remove(iterator.next()));
        iterator.remove();
        assertEquals(expected, ImmutableCollections.asSet(iterable));
        assertFalse(iterator.hasNext());
    }
    
    public static <E> void verifyIteratableRemoveUnsupported(Iterable<E> iterable) {
        try {
            Iterator<E> iter = iterable.iterator();
            iter.next();
            iter.remove();
            failExpected(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            // pass
        }
    }
    
    public static void failExpected(Class<? extends Exception> e) {
        fail("Expected " + e.getSimpleName() + "!");
    }
    
}
