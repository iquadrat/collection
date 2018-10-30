package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.mutable.IdentityHashMap;

public class IdentityHashMapTest {
    
    private static class Key {
        @Override
        public int hashCode() {
            return 42;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Key))
                return false;
            return true;
        }
        
    }
    
    @Test
    public void testPutEqualButNotSameKeys() {
        IdentityHashMap<Key, String> map = new IdentityHashMap<>();
        
        Key key1 = new Key();
        Key key2 = new Key();
        Key key3 = new Key();
        
        map.put(key1, "hello");
        map.put(key2, "world");
        map.put(key3, "!");
        
        assertEquals(3, map.keyCount());
        assertEquals("hello", map.get(key1));
        assertEquals("world", map.get(key2));
        
        assertEquals("world", map.put(key2, "universe"));
        assertEquals(3, map.keyCount());
        assertEquals("universe", map.get(key2));
    }
    
}
