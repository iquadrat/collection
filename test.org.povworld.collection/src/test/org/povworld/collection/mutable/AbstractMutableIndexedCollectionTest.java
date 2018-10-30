package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.IndexedCollection;

public abstract class AbstractMutableIndexedCollectionTest<C extends IndexedCollection<Integer>> extends AbstractMutableCollectionTest<C> {
    
    protected C collection;
    
    protected AbstractMutableIndexedCollectionTest() {
        this.collection = create(1, 2, 3);
    }
    
    protected abstract boolean supportsSet();
    
    protected abstract Integer set(C collection, Integer value, int index);
    
    protected abstract boolean supportsAddByIndex();
    
    protected abstract void add(C collection, Integer value, int index);
    
    @Test
    public void addByIndex() {
        if (!supportsAddByIndex()) {
            return;
        }
        collection = create();
        
        add(collection, 1, 0);
        assertEquals(create(1), collection);
        
        add(collection, 2, 0);
        assertEquals(create(2, 1), collection);
        
        add(collection, 7, 2);
        assertEquals(create(2, 1, 7), collection);
        
        add(collection, -3, 1);
        assertEquals(create(2, -3, 1, 7), collection);
        
        collection = create(1, 2, 3);
        
        add(collection, 4, 0);
        assertEquals(create(4, 1, 2, 3), collection);
        
        add(collection, 5, 2);
        assertEquals(create(4, 1, 5, 2, 3), collection);
        
        if (allowsDuplicates()) {
            add(collection, 1, 5);
            assertEquals(create(4, 1, 5, 2, 3, 1), collection);
        }
    }
    
    @Test(expected = NullPointerException.class)
    public void addByIndexNull() {
        if (!supportsAddByIndex()) {
            throw new NullPointerException();
        }
        add(collectionLarge, null, 17);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void addNegativeIndex() {
        if (!supportsAddByIndex()) {
            throw new IndexOutOfBoundsException();
        }
        add(collection, -1, 4);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void addTooLargeIndex() {
        if (!supportsAddByIndex()) {
            throw new IndexOutOfBoundsException();
        }
        add(collection, 4, 4);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void addByIndexDuplicate() {
        if (!supportsAddByIndex() || allowsDuplicates()) {
            throw new IllegalArgumentException();
        }
        add(create(1, 2), 2, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void setDuplicate() {
        if (allowsDuplicates() || !supportsSet()) {
            throw new IllegalArgumentException();
        }
        C collection = create(3, 1, 2);
        set(collection, 3, 1);
    }
    
    @Test
    public void set() {
        if (!supportsSet()) {
            return;
        }
        collection = create(1, 2, 3);
        
        set(collection, 4, 0);
        assertEquals(create(4, 2, 3), collection);
        
        set(collection, 9, 2);
        assertEquals(create(4, 2, 9), collection);
        
        set(collection, 9, 2);
        assertEquals(create(4, 2, 9), collection);
        
        set(collectionLarge, -42, 161);
        assertFalse(contains(collectionLarge, 161));
        assertTrue(contains(collectionLarge, -42));
        assertEquals(1000, collectionLarge.size());
        
        collection = create(1, 2, 3, 2, 1);
        
        set(collection, 5, 0);
        if (allowsDuplicates()) {
            assertEquals(create(5, 2, 3, 2, 1), collection);
            set(collection, 7, 4);
            assertEquals(create(5, 2, 3, 2, 7), collection);
            
            set(collection, 0, 2);
            assertEquals(create(5, 2, 0, 2, 7), collection);
            
            set(collection, 2, 0);
            assertEquals(create(2, 2, 0, 2, 7), collection);
        } else {
            assertEquals(create(5, 2, 3), collection);
            set(collection, 7, 2);
            assertEquals(create(5, 2, 7), collection);
            
            set(collection, 0, 2);
            assertEquals(create(5, 2, 0), collection);
        }
        
        collection = create();
        for (int i = 0; i < 77; ++i) {
            add(collection, i);
        }
        
        assertTrue(contains(collection, 55));
        assertEquals(55, (int)collection.get(55));
        
        set(collection, -55, 55);
        assertFalse(contains(collection, 55));
        assertTrue(contains(collection, -55));
        assertTrue(contains(collection, 56));
        
        if (allowsDuplicates()) {
            set(collection, 56, 0);
            assertTrue(contains(collection, 56));
            set(collection, 0, 56);
            assertTrue(contains(collection, 56));
            set(collection, 0, 0);
            assertFalse(contains(collection, 56));
        }
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void setNegativeIndex() {
        if (!supportsSet()) {
            throw new IndexOutOfBoundsException();
        }
        set(collection, 4, -1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void setTooLargeIndex() {
        if (!supportsSet()) {
            throw new IndexOutOfBoundsException();
        }
        set(collection, 4, 3);
    }
    
}
