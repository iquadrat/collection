package test.org.povworld.collection.mutable;

import javax.annotation.Nullable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeMap;

import test.org.povworld.collection.AbstractOrderedSetTest;

public class TreeMapKeySetTest extends AbstractOrderedSetTest<OrderedSet<String>> {
    
    public TreeMapKeySetTest() {
        super(new Builder());
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return CollectionUtil.sort(ImmutableCollections.asList(elements));
    }
    
    private static class Builder extends AbstractCollectionBuilder<String, OrderedSet<String>> {
        
        private int size = 0;
        
        @Nullable
        private TreeMap<String, Integer> map = TreeMap.create(String.class);
        
        @Override
        protected void _add(String element) {
            map.put(element, size++);
        }
        
        @Override
        protected OrderedSet<String> _createCollection() {
            OrderedSet<String> result = map.keys();
            map = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            map = TreeMap.create(String.class);
            size = 0;
        }
        
    }
    
}
