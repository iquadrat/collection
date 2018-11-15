package test.org.povworld.collection.mutable;

import org.povworld.collection.Collection;
import org.povworld.collection.common.AssertionFailure;
import org.povworld.collection.mutable.IndexedHashSet;

public class OrderedHashSetMutationTest extends AbstractMutableIndexedCollectionTest<IndexedHashSet<Integer>> {
    @Override
    protected boolean supportsRemove() {
        return false;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return false;
    }
    
    @Override
    protected boolean supportsAddByIndex() {
        return false;
    }
    
    @Override
    protected boolean supportsSet() {
        return true;
    }
    
    @Override
    protected boolean add(IndexedHashSet<Integer> collection, Integer element) {
        return collection.add(element);
    }
    
    @Override
    protected void add(IndexedHashSet<Integer> collection, Integer value, int index) {
        throw new AssertionFailure();
    }
    
    @Override
    protected int addAll(IndexedHashSet<Integer> collection, Collection<Integer> elements) {
        return collection.addAll(elements);
    }
    
    @Override
    protected boolean remove(IndexedHashSet<Integer> collection, Integer element) {
        throw new AssertionFailure();
    }
    
    @Override
    protected int removeAll(IndexedHashSet<Integer> collection, Collection<Integer> elements) {
        throw new AssertionFailure();
    }
    
    @Override
    protected void clear(IndexedHashSet<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected IndexedHashSet<Integer> create() {
        return new IndexedHashSet<Integer>();
    }
    
    @Override
    protected Integer set(IndexedHashSet<Integer> collection, Integer value, int index) {
        // TODO exchange index and value
        return collection.set(index, value);
    }
}
