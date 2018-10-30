package test.org.povworld.collection.persistent;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.persistent.PersistentHashMap;
import org.povworld.collection.persistent.PersistentSet;

public class PersistentHashMapKeySetTest extends AbstractPersistentSetTest<PersistentSet<String>> {
    private static class KeySetBuilder extends AbstractCollectionBuilder<String, PersistentSet<String>> {
        
        private int size = 0;
        
        private PersistentHashMap<String, Integer> map;
        
        public KeySetBuilder(PersistentHashMap<String, Integer> emptyMap) {
            PreConditions.paramNotNull(emptyMap);
            this.map = emptyMap;
        }
        
        @Override
        protected void _add(String element) {
            map = map.with(element, size++);
        }
        
        @Override
        protected PersistentSet<String> _createCollection() {
            PersistentSet<String> result = map.keys();
            map = map.cleared();
            return result;
        }
        
        @Override
        protected void _reset() {
            map = map.cleared();
            size = 0;
        }
        
    }
    
    public PersistentHashMapKeySetTest() {
        super(new KeySetBuilder(PersistentHashMap.<String, Integer>empty()));
    }
}
