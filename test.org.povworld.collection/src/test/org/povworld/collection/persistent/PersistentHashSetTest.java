package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentHashSet;
import org.povworld.collection.persistent.PersistentSet;

import test.org.povworld.collection.ChosenHash;

/**
 * Unit tests for {@link PersistentHashSet}.
 */
public class PersistentHashSetTest extends AbstractPersistentSetTest<PersistentSet<String>> {
    
    public PersistentHashSetTest() {
        super(PersistentHashSet.<String>newBuilder(CollectionUtil.getObjectIdentificator()));
    }
    
    @Test
    public void consecutiveHashes() {
        int count = 1000;
        ArrayList<ChosenHash> elements = new ArrayList<>(1000);
        for (int i = 0; i < count; ++i) {
            elements.push(new ChosenHash(String.valueOf(i), i));
        }
        
        PersistentSet<ChosenHash> set1 = PersistentHashSet.empty();
        for (ChosenHash element: elements) {
            set1 = set1.with(element);
        }
        assertEquals(count, set1.size());
        for (ChosenHash element: elements) {
            assertTrue(set1.contains(element));
        }
        
        PersistentSet<ChosenHash> set2 = PersistentHashSet.<ChosenHash>empty().withAll(elements);
        assertEquals(set1, set2);
    }
    
    @Test
    public void collisions() {
        int count = 1000;
        
        ArrayList<ChosenHash> keys1 = new ArrayList<ChosenHash>(count);
        ArrayList<ChosenHash> keys2 = new ArrayList<ChosenHash>(count);
        for (int i = 0; i < count; ++i) {
            keys1.push(new ChosenHash(String.valueOf(i), 571));
            keys2.push(new ChosenHash(String.valueOf(i), -1));
        }
        
        ChosenHash keyA = new ChosenHash("A", 56);
        ChosenHash keyB = new ChosenHash("B", 56);
        ChosenHash keyC = new ChosenHash("C", 56);
        ChosenHash keyD = new ChosenHash("D", 13984);
        
        PersistentSet<ChosenHash> set = PersistentHashSet.empty();
        for (int i = 0; i < count; ++i) {
            set = set.with(keys1.get(i));
        }
        
        assertEquals(count, set.size());
        
        set = set.with(keyA);
        set = set.with(keyB);
        set = set.with(keyC);
        set = set.with(keyD);
        
        assertEquals(count + 4, set.size());
        
        for (ChosenHash key: keys1) {
            assertTrue("Set does not contain " + key,
                    set.contains(key));
        }
        
        for (int i = 0; i < count; ++i) {
            set = set.with(keys2.get(i));
        }
        
        assertEquals(2 * count + 4, set.size());
        
        assertTrue(set.contains(keyA));
        assertTrue(set.contains(keyB));
        assertTrue(set.contains(keyC));
        assertTrue(set.contains(keyD));
        
        set = set.without(keyB);
        set = set.without(keyA);
        
        assertEquals(2 * count + 2, set.size());
        
        for (int i = 0; i < count; ++i) {
            assertTrue(set.contains(keys1.get(i)));
            set = set.without(keys1.get(i));
            assertFalse("Set still contains " + keys1.get(i), set.contains(keys1.get(i)));
            assertTrue("Set does not contain " + keys2.get(i), set.contains(keys2.get(i)));
            set = set.with(keys2.get(i));
        }
        
        assertFalse(set.contains(keyA));
        assertFalse(set.contains(keyB));
        assertTrue(set.contains(keyC));
        assertTrue(set.contains(keyD));
        
        assertEquals(count + 2, set.size());
        
        for (int i = 0; i < count; ++i) {
            assertTrue(set.contains(keys2.get(i)));
            set = set.without(keys2.get(i));
        }
        
        set = set.without(keyD);
        
        assertEquals(1, set.size());
        
        assertEquals(keyC, set.getFirstOrNull());
        assertTrue(set.contains(keyC));
    }
    
}
