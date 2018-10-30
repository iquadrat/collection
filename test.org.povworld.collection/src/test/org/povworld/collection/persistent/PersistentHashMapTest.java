package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentHashMap;
import org.povworld.collection.persistent.PersistentMap;

import test.org.povworld.collection.ChosenHash;

public class PersistentHashMapTest extends AbstractPersistentMapTest {
    
    @Override
    protected org.povworld.collection.persistent.PersistentMap<String, Integer> empty() {
        return PersistentHashMap.<String, Integer>empty();
    }
    
    @Test
    public void getFirstKeyOrNull() {
        PersistentMap<String, Integer> map = PersistentHashMap.empty();
        assertNull(map.getFirstKeyOrNull());
        assertEquals("foo", map.withAll(mapSingleton).getFirstKeyOrNull());
        
        String key1 = map.withAll(mapThree).getFirstKeyOrNull();
        assertNotNull(key1);
        assertTrue(mapThree.containsKey(key1));
        
        PersistentMap<String, Integer> large = map.withAll(mapLarge);
        String key2 = large.getFirstKeyOrNull();
        assertNotNull(key2);
        assertTrue(mapLarge.containsKey(key2));
        
        PersistentMap<String, Integer> map2 = large.without(key2);
        String key3 = map2.getFirstKeyOrNull();
        assertNotNull(key3);
        assertTrue(mapLarge.containsKey(key3));
    }
    
    @Test
    public void collisions() {
        int count = 100;
        
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
        
        PersistentMap<ChosenHash, String> map = PersistentHashMap.empty();
        for (int i = 0; i < count; ++i) {
            map = map.with(keys1.get(i), "X" + i);
        }
        
        assertEquals(count, map.keyCount());
        
        map = map.with(keyA, "A");
        map = map.with(keyB, "B");
        map = map.with(keyC, "C");
        map = map.with(keyD, "D");
        
        assertEquals(count + 4, map.keyCount());
        
        for (int i = 0; i < count; ++i) {
            assertEquals("X" + i, map.get(keys1.get(i)));
        }
        
        for (int i = 0; i < count; ++i) {
            map = map.with(keys2.get(i), "Y" + i);
        }
        
        assertEquals(2 * count + 4, map.keyCount());
        
        assertEquals("A", map.get(keyA));
        assertEquals("B", map.get(keyB));
        assertEquals("C", map.get(keyC));
        assertEquals("D", map.get(keyD));
        
        map = map.without(keyB);
        map = map.without(keyA);
        
        assertEquals(2 * count + 2, map.keyCount());
        
        for (int i = 0; i < count; ++i) {
            assertEquals("X" + i, map.get(keys1.get(i)));
            map = map.without(keys1.get(i));
            assertFalse(map.containsKey(keys1.get(i)));
            assertEquals("Y" + i, map.get(keys2.get(i)));
            map = map.with(keys2.get(i), "Z" + i);
        }
        
        assertFalse(map.containsKey(keyA));
        assertFalse(map.containsKey(keyB));
        assertEquals("C", map.get(keyC));
        assertEquals("D", map.get(keyD));
        
        assertEquals(count + 2, map.keyCount());
        
        for (int i = 0; i < count; ++i) {
            assertEquals("Z" + i, map.get(keys2.get(i)));
            map = map.without(keys2.get(i));
        }
        
        map = map.without(keyD);
        
        assertEquals(1, map.keyCount());
        
        assertEquals(keyC, map.getFirstKeyOrNull());
        assertEquals("C", map.get(keyC));
    }
    
}
