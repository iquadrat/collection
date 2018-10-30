package test.org.povworld.collection.mutable;

import org.povworld.collection.Collection;
import org.povworld.collection.mutable.HashSet;

public class HashSetMutationTest extends AbstractMutableCollectionTest<HashSet<Integer>> {
    @Override
    protected boolean allowsDuplicates() {
        return false;
    }
    
    @Override
    protected boolean supportsRemove() {
        return true;
    }
    
    @Override
    protected boolean add(HashSet<Integer> collection, Integer element) {
        return collection.add(element);
    }
    
    @Override
    protected int addAll(HashSet<Integer> collection, Collection<Integer> elements) {
        return collection.addAll(elements);
    }
    
    @Override
    protected boolean remove(HashSet<Integer> collection, Integer element) {
        return collection.remove(element);
    }
    
    @Override
    protected int removeAll(HashSet<Integer> collection, Collection<Integer> elements) {
        return collection.removeAll(elements);
    }
    
    @Override
    protected void clear(HashSet<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected HashSet<Integer> create() {
        return new HashSet<Integer>();
    }
}
