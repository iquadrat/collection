package test.org.povworld.collection.mutable;

import junit.framework.AssertionFailedError;

import org.povworld.collection.Collection;
import org.povworld.collection.common.AssertionFailure;
import org.povworld.collection.mutable.HashList;

/**
 * Mutation tests for {@link HashList}.
 *
 * @see HashListMutationTest
 * @see HashListTest
 */
public class HashListMutationTest extends AbstractMutableIndexedCollectionTest<HashList<Integer>> {
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Override
    protected boolean supportsRemove() {
        return false;
    }
    
    @Override
    protected boolean supportsAddByIndex() {
        return false;
    }
    
    @Override
    protected boolean supportsSet() {
        return false;
    }
    
    @Override
    protected Integer set(HashList<Integer> collection, Integer value, int index) {
        throw new AssertionFailure();
    }
    
    @Override
    protected boolean add(HashList<Integer> collection, Integer element) {
        collection.add(element);
        return true;
    }
    
    @Override
    protected void add(HashList<Integer> collection, Integer value, int index) {
        throw new AssertionFailedError();
    }
    
    @Override
    protected int addAll(HashList<Integer> collection, Collection<Integer> elements) {
        collection.addAll(elements);
        return elements.size();
    }
    
    @Override
    protected boolean remove(HashList<Integer> collection, Integer element) {
        throw new AssertionFailure();
    }
    
    @Override
    protected int removeAll(HashList<Integer> collection, Collection<Integer> elements) {
        throw new AssertionFailure();
    }
    
    @Override
    protected void clear(HashList<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected HashList<Integer> create() {
        return new HashList<Integer>();
    }
    
    @Override
    protected boolean contains(HashList<Integer> collection, Integer element) {
        return collection.contains(element);
    }
}
