package test.org.povworld.collection.mutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.mutable.TreeMap;

import test.org.povworld.collection.AbstractMapTest;

/**
 * Unit tests for {@link TreeMap}.
 */
public class TreeMapTest extends AbstractMapTest<TreeMap<String, Integer>> {
    
    private static final Comparator<String> comparator = CollectionUtil.getDefaultComparator(String.class);
    
    @Override
    protected TreeMap<String, Integer> empty() {
        return new TreeMap<>(comparator);
    }
    
    @Override
    protected TreeMap<String, Integer> put(TreeMap<String, Integer> map, String key, Integer value) {
        map.put(key, value);
        return map;
    }
    
    @Override
    protected TreeMap<String, Integer> remove(TreeMap<String, Integer> map, String key) {
        map.remove(key);
        return map;
    }
    
    @Override
    protected TreeMap<String, Integer> clear(TreeMap<String, Integer> map) {
        map.clear();
        return map;
    }
}
