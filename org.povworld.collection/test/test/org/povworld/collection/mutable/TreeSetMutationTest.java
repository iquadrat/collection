package test.org.povworld.collection.mutable;

import org.povworld.collection.Collection;
import org.povworld.collection.mutable.TreeSet;

/**
 * Unit tests for {@link TreeSet}.
 * 
 * @see TreeSetTest
 */
public class TreeSetMutationTest extends AbstractMutableCollectionTest<TreeSet<Integer>> {
    
    @Override
    protected TreeSet<Integer> create() {
        return TreeSet.create(Integer.class);
    }
    
    @Override
    protected boolean add(TreeSet<Integer> collection, Integer element) {
        return collection.add(element);
    }
    
    @Override
    protected int addAll(TreeSet<Integer> collection, Collection<Integer> elements) {
        return collection.addAll(elements);
    }
    
    @Override
    protected boolean remove(TreeSet<Integer> collection, Integer element) {
        return collection.remove(element);
    }
    
    @Override
    protected int removeAll(TreeSet<Integer> collection, Collection<Integer> elements) {
        return collection.removeAll(elements);
    }
    
    @Override
    protected void clear(TreeSet<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected boolean supportsRemove() {
        return true;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return false;
    }
    
}
