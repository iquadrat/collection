package test.org.povworld.collection.mutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeMultiMap;

import test.org.povworld.collection.AbstractOrderedSetTest;

public class TreeMultiMapKeysTest extends AbstractOrderedSetTest<OrderedSet<String>> {
    
    public TreeMultiMapKeysTest() {
        super(new AbstractCollectionBuilder<String, OrderedSet<String>>() {
            
            private TreeMultiMap<String, String> map = TreeMultiMap.create(String.class, String.class);
            
            @Override
            protected void _add(String element) {
                map.put(element, "x");
            }
            
            @Override
            protected void _reset() {
                map = TreeMultiMap.create(String.class, String.class);
            }
            
            @Override
            protected OrderedSet<String> _createCollection() {
                return map.keys();
            }
        });
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return CollectionUtil.sort(ImmutableCollections.asList(elements));
    }
    
}
