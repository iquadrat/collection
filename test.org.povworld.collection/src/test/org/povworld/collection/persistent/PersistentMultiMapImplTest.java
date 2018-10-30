package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;

import org.povworld.collection.Collection;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentMultiMap;
import org.povworld.collection.persistent.PersistentMultiMapImpl;

/**
 * Unit tests for {@link PersistentMultiMapImpl}.
 */
public class PersistentMultiMapImplTest extends AbstractPersistentMultiMapTest<PersistentMultiMap<String, Integer>> {
    
    @Override
    protected PersistentMultiMap<String, Integer> create() {
        return empty();
    }
    
    @Override
    protected <K, V> PersistentMultiMap<K, V> empty() {
        return PersistentMultiMapImpl.empty();
    }
    
    @Override
    protected PersistentMultiMap<String, Integer> set(PersistentMultiMap<String, Integer> map, String key,
            Collection<Integer> values) {
        return map.without(key).withAll(key, values);
    }
    
    @Override
    protected void assertValues(Collection<Integer> expectedValues, Collection<Integer> actualValues) {
        assertEquals(ImmutableCollections.asSet(expectedValues), actualValues);
    }
    
}
