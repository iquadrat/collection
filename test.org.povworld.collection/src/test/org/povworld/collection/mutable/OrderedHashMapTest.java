package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.common.AssertionFailure;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.OrderedHashMap;

import test.org.povworld.collection.AbstractMapTest;
import test.org.povworld.collection.TestUtil;

// TODO check ordering of key/values!
public class OrderedHashMapTest extends AbstractMapTest<OrderedHashMap<String, Integer>> {
    
    private final OrderedHashMap<String, Integer> map;
    
    public OrderedHashMapTest() {
        map = empty();
        map.put("foo", 1);
        map.put("bar", 2);
        map.put("lulu", 3);
    }
    
    @Override
    protected OrderedHashMap<String, Integer> empty() {
        return new OrderedHashMap<String, Integer>();
    }
    
    @Override
    protected OrderedHashMap<String, Integer> put(OrderedHashMap<String, Integer> map, String key, Integer value) {
        map.put(key, value);
        map.testInvariants();
        return map;
    }
    
    @Override
    protected boolean supportsRemove() {
        return false;
    }
    
    @Override
    protected OrderedHashMap<String, Integer> remove(OrderedHashMap<String, Integer> map, String key) {
        throw new AssertionFailure();
    }
    
    @Override
    protected OrderedHashMap<String, Integer> clear(OrderedHashMap<String, Integer> map) {
        map.clear();
        return map;
    }
    
    @Test
    public void valueIterable() {
        ArrayList<Integer> actual = TestUtil.verifyIterable(map.valueIterable(), ArrayList.<Integer>newBuilder());
        assertEquals(ImmutableCollections.listOf(1, 2, 3), actual);
        
        TestUtil.verifyRemoveByIterator(map.valueIterable());
    }
    
    @Test
    public void reverseValueIterable() {
        ArrayList<Integer> actual = TestUtil.verifyIterable(map.reverseValueIterable(), ArrayList.<Integer>newBuilder());
        assertEquals(ImmutableCollections.listOf(3, 2, 1), actual);
        
        TestUtil.verifyRemoveByIterator(map.reverseValueIterable());
    }
    
    @Test
    public void keyIterable() {
        ArrayList<String> actual = TestUtil.verifyIterable(map.keyIterable(), ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("foo", "bar", "lulu"), actual);
        
        TestUtil.verifyRemoveByIterator(map.keyIterable());
    }
    
    @Test
    public void reverseKeyIterable() {
        ArrayList<String> actual = TestUtil.verifyIterable(map.reverseKeyIterable(), ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf("lulu", "bar", "foo"), actual);
        
        TestUtil.verifyRemoveByIterator(map.reverseKeyIterable());
    }
    
}
