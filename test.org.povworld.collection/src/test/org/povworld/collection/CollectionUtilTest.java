package test.org.povworld.collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.mockito.Mockito;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.Container;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.OrderedCollection;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.Set;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.ReverseComparator;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableSet;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.TreeSet;
import org.povworld.collection.mutable.IndexedHashSet;

public class CollectionUtilTest {
    
    private final Iterable<String> iterableMock;
    
    private final Iterator<String> iteratorMock;
    
    @SuppressWarnings("unchecked")
    public CollectionUtilTest() {
        iterableMock = Mockito.mock(Iterable.class);
        iteratorMock = Mockito.mock(Iterator.class);
        
        Mockito.when(iterableMock.iterator()).thenReturn(iteratorMock);
    }
    
    @Test
    public void getObjectIdentificator() {
        Identificator<Object> objIdentificator = CollectionUtil.getObjectIdentificator();
        assertTrue(objIdentificator.equals(iterableMock, iterableMock));
        assertFalse(objIdentificator.equals(iterableMock, iteratorMock));
        assertTrue(objIdentificator.isIdentifiable(new Object()));
        assertEquals(ObjectUtil.strengthenedHashcode("foo".hashCode()), objIdentificator.hashCode("foo"));
        assertTrue(objIdentificator.equals(CollectionUtil.getObjectIdentificator()));
        assertEquals(objIdentificator.hashCode(), CollectionUtil.getObjectIdentificator().hashCode());
    }
    
    @Test
    public void getDefaultIdentificatorForObject() {
        Identificator<? super Object> objIdentificator = CollectionUtil.getDefaultIdentificator(Object.class);
        assertEquals(CollectionUtil.getObjectIdentificator(), objIdentificator);
    }
    
    @Test
    public void getDefaultIdentificatorForString() {
        Identificator<? super String> stringIdentificator = CollectionUtil.getDefaultIdentificator(String.class);
        assertTrue(stringIdentificator.equals("foo", new String("foo")));
        assertFalse(stringIdentificator.equals("foo", "Foo"));
        
        assertEquals(CollectionUtil.getDefaultComparator(String.class), stringIdentificator);
    }
    
    @Test
    public void getDefaultComparator() {
        Comparator<String> stringComparator = CollectionUtil.getDefaultComparator(String.class);
        assertTrue(stringComparator.isIdentifiable("foo"));
        assertEquals("foo".hashCode(), stringComparator.hashCode("foo"));
    }
    
    @Test
    public void isEmpty() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        assertTrue(CollectionUtil.isEmpty(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        assertFalse(CollectionUtil.isEmpty(iterableMock));
    }
    
    @Test
    public void sizeOf() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        assertEquals(0, CollectionUtil.sizeOf(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        assertEquals(1, CollectionUtil.sizeOf(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, true, true, false);
        assertEquals(3, CollectionUtil.sizeOf(iterableMock));
    }
    
    @Test
    public void hasMinElementCount() {
        assertTrue(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf().iterator(), 0));
        assertFalse(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf().iterator(), 1));
        assertTrue(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf("foo").iterator(), 1));
        assertFalse(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf("foo").iterator(), 2));
        assertTrue(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf("foo", "bar").iterator(), 2));
        assertFalse(CollectionUtil.hasMinRemainingElements(ImmutableCollections.listOf("foo", "bar").iterator(), 3));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void firstElementOfEmpty() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        CollectionUtil.firstElement(iterableMock);
    }
    
    @Test
    public void firstElement() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first");
        assertEquals("first", CollectionUtil.firstElement(iterableMock));
    }
    
    @Test
    public void firstElementOrNullIterable() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        assertNull(CollectionUtil.firstElementOrNull(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first");
        assertEquals("first", CollectionUtil.firstElementOrNull(iterableMock));
    }
    
    @Test
    public void firstElementOrNullArray() {
        assertNull(CollectionUtil.firstElementOrNull(new String[] {}));
        assertEquals("first", CollectionUtil.firstElementOrNull(new String[] {"first", "second"}));
    }
    
    @Test
    public void lastElementOrNullIterable() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        assertNull(CollectionUtil.lastElementOrNull(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first");
        assertEquals("first", CollectionUtil.lastElementOrNull(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first", "second");
        assertEquals("second", CollectionUtil.lastElementOrNull(iterableMock));
    }
    
    @Test
    public void lastElementOrNullArray() {
        assertNull(CollectionUtil.lastElementOrNull(new String[] {}));
        assertEquals("first", CollectionUtil.lastElementOrNull(new String[] {"first"}));
        assertEquals("second", CollectionUtil.lastElementOrNull(new String[] {"first", "second"}));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void lastElementOfEmpty() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(false);
        CollectionUtil.lastElement(iterableMock);
    }
    
    @Test
    public void lastElement() {
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first");
        assertEquals("first", CollectionUtil.lastElement(iterableMock));
        
        Mockito.when(iteratorMock.hasNext()).thenReturn(true, true, false);
        Mockito.when(iteratorMock.next()).thenReturn("first", "second");
        assertEquals("second", CollectionUtil.lastElement(iterableMock));
    }
    
    @Test
    public void countIterable() {
        assertEquals(0, CollectionUtil.count(ImmutableCollections.listOf(), "x"));
        assertEquals(0, CollectionUtil.count(ImmutableCollections.listOf("a", "b", "c"), "x"));
        assertEquals(1, CollectionUtil.count(ImmutableCollections.listOf("x"), "x"));
        assertEquals(3, CollectionUtil.count(ImmutableCollections.listOf("x", "o", "x", "o", "x"), "x"));
    }
    
    @Test
    public void iteratesEqualSequence() {
        assertTrue(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf(), ImmutableCollections.listOf()));
        assertTrue(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf("foo"), ImmutableCollections.listOf("foo")));
        assertTrue(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf(1, 1, 2, 3, 5), ImmutableCollections.listOf(1, 1, 2, 3, 5)));
        assertTrue(CollectionUtil.iteratesEqualSequence(Arrays.asList("foo", null, "bar"), Arrays.asList("foo", null, "bar")));
        
        assertFalse(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf("foo"), ImmutableCollections.listOf()));
        assertFalse(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf(), ImmutableCollections.listOf("")));
        assertFalse(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf("foo"), ImmutableCollections.listOf("bar")));
        assertFalse(CollectionUtil.iteratesEqualSequence(ImmutableCollections.listOf("foo"), ImmutableCollections.listOf("foo", "bar")));
        assertFalse(CollectionUtil.iteratesEqualSequence(Arrays.asList("foo"), Arrays.asList((Object)null)));
    }
    
    private boolean isEven(int i) {
        return i % 2 == 0;
    }
    
    @Test
    public void filterIterable() {
        assertEquals(ImmutableCollections.listOf(), CollectionUtil.filter(i -> isEven(i), ImmutableCollections.<Integer>listOf()));
        assertEquals(ImmutableCollections.listOf(6, 4, 6, 2),
                CollectionUtil.filter(i -> isEven(i), ImmutableCollections.<Integer>listOf(6, 3, 1, 4, 6, 1, 2, 1, 9)));
    }
    
    @Test
    public void indexOfArray() {
        assertEquals(-1, CollectionUtil.indexOf(new String[] {}, "foo"));
        assertEquals(-1, CollectionUtil.indexOf(new Integer[] {}, null));
        assertEquals(-1, CollectionUtil.indexOf(new Integer[] {1, 1, 2, 3, 5}, 0));
        assertEquals(0, CollectionUtil.indexOf(new Integer[] {1, 1, 2, 3, 5}, 1));
        assertEquals(4, CollectionUtil.indexOf(new Integer[] {1, 1, 2, 3, 5}, 5));
        assertEquals(2, CollectionUtil.indexOf(new Integer[] {1, 1, null, 3, 5}, null));
    }
    
    @Test
    public void indexOfIndexedIterable() {
        assertEquals(-1, CollectionUtil.indexOf((Iterable<Object>)ImmutableCollections.listOf(), "foo"));
        assertEquals(-1, CollectionUtil.indexOf((Iterable<Object>)ImmutableCollections.listOf(), null));
        assertEquals(-1, CollectionUtil.indexOf((Iterable<Integer>)ImmutableCollections.listOf(1, 1, 2, 3, 5), 0));
        assertEquals(0, CollectionUtil.indexOf((Iterable<Integer>)ImmutableCollections.listOf(1, 1, 2, 3, 5), 1));
        assertEquals(4, CollectionUtil.indexOf((Iterable<Integer>)ImmutableCollections.listOf(1, 1, 2, 3, 5), 5));
    }
    
    @Test
    public void indexOfContainer() {
        assertEquals(-1, CollectionUtil.indexOf((Container<Object>)ImmutableCollections.orderedSetOf(), "foo"));
        assertEquals(-1, CollectionUtil.indexOf((Container<Integer>)ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 0));
        assertEquals(0, CollectionUtil.indexOf((Container<Integer>)ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 1));
        assertEquals(4, CollectionUtil.indexOf((Container<Integer>)ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 5));
    }
    
    @Test
    public void indexOfOrderedCollection() {
        assertEquals(-1, CollectionUtil.indexOf((OrderedCollection<Object>)ImmutableCollections.listOf(), "foo"));
        assertEquals(-1, CollectionUtil.indexOf((OrderedCollection<Integer>)ImmutableCollections.listOf(1, 11, 2, 3, 5), 0));
        assertEquals(0, CollectionUtil.indexOf((OrderedCollection<Integer>)ImmutableCollections.listOf(1, 11, 2, 3, 5), 1));
        assertEquals(4, CollectionUtil.indexOf((OrderedCollection<Integer>)ImmutableCollections.listOf(1, 11, 2, 3, 5), 5));
    }
    
    @Test
    public void indexOfOrderedSet() {
        assertEquals(-1, CollectionUtil.indexOf(ImmutableCollections.<String>orderedSetOf(), "foo"));
        assertEquals(-1, CollectionUtil.indexOf(ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 0));
        assertEquals(0, CollectionUtil.indexOf(ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 1));
        assertEquals(4, CollectionUtil.indexOf(ImmutableCollections.orderedSetOf(1, 11, 2, 3, 5), 5));
    }
    
    @Test
    public void indexOfIndexedCollection() {
        assertEquals(-1, CollectionUtil.indexOf(ImmutableCollections.<String>listOf(), "foo"));
        assertEquals(-1, CollectionUtil.indexOf(ImmutableCollections.listOf(1, 1, 2, 3, 5), 0));
        assertEquals(0, CollectionUtil.indexOf(ImmutableCollections.listOf(1, 1, 2, 3, 5), 1));
        assertEquals(4, CollectionUtil.indexOf(ImmutableCollections.listOf(1, 1, 2, 3, 5), 5));
    }
    
    @Test
    public void contains() {
        Iterable<String> iterable = CollectionUtil.wrap("hello", "world");
        assertFalse(CollectionUtil.contains(iterable, "foo"));
        assertTrue(CollectionUtil.contains(iterable, "world"));
    }
    
    @Test(expected = NullPointerException.class)
    public void wrapNullThrowsOnIteration() {
        Collection<String> iterable = CollectionUtil.wrap("null", null);
        CollectionUtil.contains(iterable, "foo");
    }
    
    @Test
    public void sortEmpty() {
        ArrayList<String> sorted = CollectionUtil.sort(ImmutableCollections.<String>setOf(), ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf(), sorted);
    }
    
    @Test
    public void sort() {
        ImmutableSet<String> collection = ImmutableCollections.setOf("one", "two", "three", "four");
        OrderedSet<String> sorted = CollectionUtil.sort(collection, IndexedHashSet.<String>newBuilder());
        assertEquals(ImmutableCollections.orderedSetOf("four", "one", "three", "two"), sorted);
    }
    
    @Test
    public void sortList() {
        List<String> collection = ImmutableCollections.listOf("one", "two", "three", "four");
        ArrayList<String> sorted = CollectionUtil.sort(collection);
        assertEquals(ImmutableCollections.orderedSetOf("four", "one", "three", "two"), sorted);
    }
    
    @Test
    public void sortCustomComparator() {
        ImmutableSet<String> collection = ImmutableCollections.setOf("one", "two", "three", "four");
        OrderedSet<String> sorted = CollectionUtil.sort(collection, IndexedHashSet.<String>newBuilder(), new ReverseComparator<String>(
                CollectionUtil.getDefaultComparator(String.class)));
        assertEquals(ImmutableCollections.orderedSetOf("two", "three", "one", "four"), sorted);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void firstNNegative() {
        CollectionUtil.firstN(ImmutableCollections.listOf(), -4);
    }
    
    @Test
    public void firstN() {
        List<String> list = ImmutableCollections.listOf("a", "b", "c", "b", "a");
        assertEquals(ImmutableCollections.listOf(), CollectionUtil.firstN(list, 0));
        assertEquals(ImmutableCollections.listOf("a"), CollectionUtil.firstN(list, 1));
        assertEquals(ImmutableCollections.listOf("a", "b", "c"), CollectionUtil.firstN(list, 3));
        assertSame(list, CollectionUtil.firstN(list, 6));
        assertSame(list, CollectionUtil.firstN(list, 612));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void intersectDifferentIdentificators() {
        TreeSet<String> set1 = new TreeSet<>(CollectionUtil.getDefaultComparator(String.class));
        TreeSet<String> set2 = new TreeSet<>(new Comparator<Object>() {
            
            @Override
            public int compare(Object object1, Object object2) {
                return 0;
            }
            
            @Override
            public boolean isIdentifiable(Object object) {
                return true;
            }
            
            @Override
            public boolean equals(Object object1, Object object2) {
                return object1 == object2;
            }
            
            @Override
            public int hashCode(Object object) {
                return System.identityHashCode(object);
            }
        });
        CollectionUtil.intersectOrdered(set1, set2);
    }
    
    @Test
    public void intersect() {
        Set<String> empty = ImmutableCollections.setOf();
        Set<String> set1 = ImmutableCollections.setOf("a", "b", "c", "d", "e");
        Set<String> set2 = ImmutableCollections.setOf("f", "c", "g", "b", "e", "y");
        assertEquals(empty, CollectionUtil.intersect(empty, empty));
        assertEquals(empty, CollectionUtil.intersect(set1, empty));
        assertEquals(set1, CollectionUtil.intersect(set1, set1));
        assertEquals(ImmutableCollections.setOf("c", "b", "e"), CollectionUtil.intersect(set1, set2));
        assertEquals(ImmutableCollections.setOf("c", "b", "e"), CollectionUtil.intersect(set2, set1));
    }
    
    @Test
    public void toSetString() {
        assertEquals("{}", CollectionUtil.toSetString(CollectionUtil.wrap()));
        assertEquals("{hello}", CollectionUtil.toSetString(CollectionUtil.wrap("hello")));
        assertEquals("{}", CollectionUtil.toSetString(CollectionUtil.wrap("")));
        assertEquals("{one, two}", CollectionUtil.toSetString(CollectionUtil.wrap("one", "two")));
        assertEquals("{, }", CollectionUtil.toSetString(CollectionUtil.wrap("", "")));
        assertEquals("{1, 2, 3}", CollectionUtil.toSetString(CollectionUtil.wrap(1, 2, 3)));
    }
    
    @Test
    public void toListString() {
        assertEquals("[]", CollectionUtil.toListString(CollectionUtil.wrap()));
        assertEquals("[hello]", CollectionUtil.toListString(CollectionUtil.wrap("hello")));
        assertEquals("[]", CollectionUtil.toListString(CollectionUtil.wrap("")));
        assertEquals("[one, two]", CollectionUtil.toListString(CollectionUtil.wrap("one", "two")));
        assertEquals("[, ]", CollectionUtil.toListString(CollectionUtil.wrap("", "")));
        assertEquals("[1, 2, 3]", CollectionUtil.toListString(CollectionUtil.wrap(1, 2, 3)));
    }
    
    @Test
    public void toArray() {
        assertArrayEquals(new Object[0], CollectionUtil.toObjectArray(ImmutableCollections.<String>listOf()));
        assertArrayEquals(new Object[] {"foo", "bar"}, CollectionUtil.toObjectArray(ImmutableCollections.listOf("foo", "bar")));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void toArrayConcurrentModification() {
        Collection<String> collectionMock = Mockito.mock(Collection.class);
        Iterator<String> iteratorMock = Mockito.mock(Iterator.class);
        
        when(collectionMock.size()).thenReturn(3);
        when(collectionMock.iterator()).thenReturn(iteratorMock);
        when(iteratorMock.hasNext()).thenReturn(true, true, false);
        when(iteratorMock.next()).thenReturn("foo", "bar");
        
        try {
            CollectionUtil.toObjectArray(collectionMock);
            TestUtil.failExpected(ConcurrentModificationException.class);
        } catch (ConcurrentModificationException expected) {}
        
        Mockito.verify(collectionMock).iterator();
        Mockito.verify(iteratorMock, times(3)).hasNext();
        Mockito.verify(iteratorMock, times(2)).next();
    }
    
}
