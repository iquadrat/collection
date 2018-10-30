package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableCollections;

public abstract class AbstractMutableCollectionTest<C extends Collection<Integer>> {
    
    protected C collectionLarge;
    
    protected AbstractMutableCollectionTest() {
        collectionLarge = create();
        for (int i = 0; i < 1000; ++i) {
            add(collectionLarge, i);
        }
    }
    
    protected abstract C create();
    
    protected abstract boolean add(C collection, Integer element);
    
    protected abstract int addAll(C collection, Collection<Integer> elements);
    
    protected abstract boolean remove(C collection, Integer element);
    
    protected abstract int removeAll(C collection, Collection<Integer> elements);
    
    protected abstract void clear(C collection);
    
    protected abstract boolean supportsRemove();
    
    protected abstract boolean allowsDuplicates();
    
    protected boolean contains(C collection, Integer element) {
        return CollectionUtil.contains(collection, element);
    }
    
    protected int count(C collection, Integer element) {
        return CollectionUtil.count(collection, element);
    }
    
    protected C create(Integer... elements) {
        C collection = create();
        addAll(collection, ImmutableCollections.listOf(elements));
        return collection;
    }
    
    @Test
    public void remove() {
        if (!supportsRemove()) {
            return;
        }
        
        if (allowsDuplicates()) {
            C collection = create(1, 2, 3, 2, 1);
            
            assertFalse(remove(collection, 4));
            
            assertTrue(remove(collection, 1));
            assertEquals(create(2, 3, 2, 1), collection);
            
            assertTrue(remove(collection, 3));
            assertEquals(create(2, 2, 1), collection);
            
            assertTrue(remove(collection, 1));
            assertEquals(create(2, 2), collection);
            
            assertTrue(remove(collection, 2));
            assertTrue(remove(collection, 2));
            assertFalse(remove(collection, 2));
            assertTrue(collection.isEmpty());
            
            collection = create();
            add(collection, 51);
            for (int i = 0; i < 91; ++i) {
                add(collection, i);
            }
            
            assertFalse(remove(collection, -4));
            
            assertTrue(remove(collection, 6));
            assertFalse(remove(collection, 6));
            
            for (int i = 0; i < 91; ++i) {
                boolean succ = remove(collection, i);
                assertEquals(i != 6, succ);
            }
            
            assertEquals(create(51), collection);
            assertTrue(remove(collection, 51));
            assertTrue(collection.isEmpty());
        }
        
        C collection = create();
        
        assertFalse(remove(collection, 42));
        
        collection = create(1, 2, 3);
        
        assertTrue(remove(collection, 2));
        assertFalse(remove(collection, 2));
        assertTrue(remove(collection, 3));
        assertEquals(create(1), collection);
        
        assertTrue(remove(collectionLarge, 174));
        assertFalse(remove(collectionLarge, 174));
        assertEquals(999, collectionLarge.size());
        
        assertTrue(add(collectionLarge, -11));
        int removed = 0;
        while (!collectionLarge.isEmpty()) {
            assertTrue(remove(collectionLarge, collectionLarge.getFirst()));
            assertTrue(remove(collectionLarge, CollectionUtil.lastElement(collectionLarge)));
            assertFalse(remove(collectionLarge, 174));
            removed += 2;
        }
        assertEquals(1000, removed);
    }
    
    @Test
    public void addAll() {
        C collection = create(3, 4, 5);
        int added = addAll(collection, ImmutableCollections.listOf(1, 2, 3, 2, 1));
        
        if (allowsDuplicates()) {
            assertEquals(5, added);
            assertEquals(8, collection.size());
            assertEquals(0, count(collection, 0));
            assertEquals(2, count(collection, 1));
            assertEquals(2, count(collection, 2));
            assertEquals(2, count(collection, 3));
            assertEquals(1, count(collection, 4));
        } else {
            assertEquals(2, added);
            assertEquals(5, collection.size());
            assertTrue(contains(collection, 2));
            assertTrue(contains(collection, 3));
            assertTrue(contains(collection, 4));
            assertFalse(contains(collection, 0));
        }
        
        collection = create();
        
        assertEquals(0, addAll(collection, ImmutableCollections.<Integer>listOf()));
        assertTrue(collection.isEmpty());
        
        assertEquals(3, addAll(collection, create(-1, -2, -3)));
        assertEquals(3, collection.size());
        assertEquals(create(-1, -2, -3), collection);
        
        if (allowsDuplicates()) {
            assertEquals(1000, addAll(collection, collectionLarge));
            assertEquals(1003, collection.size());
            
            assertEquals(3, addAll(collection, create(1, 2, 3)));
            assertEquals(1006, collection.size());
        } else {
            assertEquals(1000, addAll(collection, collectionLarge));
            assertEquals(1003, collection.size());
            
            assertEquals(0, addAll(collection, create(-1, -2, -3)));
            assertEquals(1003, collection.size());
        }
        
    }
    
    @Test(expected = NullPointerException.class)
    public void addNull() {
        skipDynamicNPCheckTest();
        C collection = create(1, 2, 3);
        add(collection, null);
    }
    
    @Test(expected = NullPointerException.class)
    public void addAllNull() {
        skipDynamicNPCheckTest();
        C collection = create(1, 2, 3, 4, 5, 6, 0, -1, 1, 2);
        addAll(collection, ImmutableCollections.listOf(9, 12, null, 15));
    }
    
    private void skipDynamicNPCheckTest() {
        if (!PreConditions.dynamicNullPointerChecksEnabled()) throw new NullPointerException();
    }
    
    @Test
    public void removeAll() {
        if (!supportsRemove()) {
            return;
        }
        C collection = create(-1, -2, -3);
        
        assertEquals(0, removeAll(collection, create()));
        assertEquals(0, removeAll(collection, create(42)));
        assertEquals(0, removeAll(collection, collectionLarge));
        assertEquals(3, removeAll(collection, create(-2, -3, -1)));
        assertTrue(collection.isEmpty());
        
        assertEquals(5, removeAll(collectionLarge, ImmutableCollections.listOf(74, 2, 31, 2, 131, 99)));
        
        collection = create(1, 2, 3, 2, 1);
        
        int removed = removeAll(collection, create(3, 2, 0, 2, 3));
        if (allowsDuplicates()) {
            assertEquals(3, removed);
            assertEquals(2, collection.size());
            assertEquals(2, count(collection, 1));
            assertEquals(0, count(collection, 3));
            assertEquals(0, count(collection, 4));
        } else {
            assertEquals(2, removed);
            assertEquals(1, collection.size());
            assertTrue(contains(collection, 1));
            assertFalse(contains(collection, 2));
        }
        
    }
    
    @Test
    public void clear() {
        C collection = create();
        addAll(collection, ImmutableCollections.listOf(1, 2, 3, 2, 1));
        clear(collection);
        assertEquals(0, collection.size());
        assertTrue(collection.isEmpty());
        assertEquals(collection.hashCode(), create().hashCode());
        
        addAll(collection, collectionLarge);
        assertEquals(collectionLarge, collection);
        clear(collection);
        assertTrue(collection.isEmpty());
    }
    
}
