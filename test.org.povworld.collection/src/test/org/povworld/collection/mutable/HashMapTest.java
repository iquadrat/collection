package test.org.povworld.collection.mutable;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.mutable.HashMap;

import test.org.povworld.collection.AbstractMapTest;

/**
 * Unit tests for {@link HashMap}.
 */
public class HashMapTest extends AbstractMapTest<HashMap<String, Integer>> {
    
    @Override
    protected HashMap<String, Integer> empty() {
        return new HashMap<String, Integer>();
    }
    
    @Override
    protected HashMap<String, Integer> put(HashMap<String, Integer> map, String key, Integer value) {
        map.put(key, value);
        return map;
    }
    
    @Override
    protected HashMap<String, Integer> remove(HashMap<String, Integer> map, String key) {
        map.remove(key);
        return map;
    }
    
    @Override
    protected HashMap<String, Integer> clear(HashMap<String, Integer> map) {
        map.clear();
        return map;
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void valueIteratorRemoveNotSupported() {
        Iterator<Integer> iterator = mapLarge.values().iterator();
        iterator.next();
        iterator.remove();
    }
    
}
