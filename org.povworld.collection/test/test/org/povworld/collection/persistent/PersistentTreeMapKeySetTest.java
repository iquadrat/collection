package test.org.povworld.collection.persistent;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableContainer;
import org.povworld.collection.persistent.PersistentMap;
import org.povworld.collection.persistent.PersistentTreeMap;

import test.org.povworld.collection.AbstractStringCollectionTest;

public class PersistentTreeMapKeySetTest extends AbstractStringCollectionTest<ImmutableContainer<String>> {
    
    private static class KeySetBuilder extends AbstractCollectionBuilder<String, ImmutableContainer<String>> {
        
        private int size = 0;
        
        private PersistentMap<String, Integer> map;
        
        public KeySetBuilder(PersistentMap<String, Integer> emptyMap) {
            PreConditions.paramNotNull(emptyMap);
            this.map = emptyMap;
        }
        
        @Override
        protected void _add(String element) {
            map = map.with(element, size++);
        }
        
        @Override
        protected ImmutableContainer<String> _createCollection() {
            ImmutableContainer<String> result = map.keys();
            map = map.cleared();
            return result;
        }
        
        @Override
        protected void _reset() {
            map = map.cleared();
            size = 0;
        }
        
    }
    
    public PersistentTreeMapKeySetTest() {
        super(new KeySetBuilder(PersistentTreeMap.<String, Integer>empty(String.class)));
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return null;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return false;
    }
    
}
