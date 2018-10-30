package test.org.povworld.collection.mutable;

import org.povworld.collection.Collection;
import org.povworld.collection.common.AssertionFailure;
import org.povworld.collection.mutable.ArrayList;

/**
 * Unit tests for {@link ArrayList}.
 * 
 * @see ArrayListTest
 */
public class ArrayListMutationTest extends AbstractMutableIndexedCollectionTest<ArrayList<Integer>> {
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
        return true;
    }
    
    @Override
    protected boolean add(ArrayList<Integer> collection, Integer element) {
        collection.push(element);
        return true;
    }
    
    @Override
    protected void add(ArrayList<Integer> collection, Integer value, int index) {
        throw new AssertionFailure();
    }
    
    @Override
    protected int addAll(ArrayList<Integer> collection, Collection<Integer> elements) {
        collection.pushAll(elements);
        return elements.size();
    }
    
    @Override
    protected boolean remove(ArrayList<Integer> collection, Integer element) {
        throw new AssertionFailure();
    }
    
    @Override
    protected int removeAll(ArrayList<Integer> collection, Collection<Integer> elements) {
        throw new AssertionFailure();
    }
    
    @Override
    protected void clear(ArrayList<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected ArrayList<Integer> create() {
        return new ArrayList<Integer>();
    }
    
    @Override
    protected Integer set(ArrayList<Integer> collection, Integer value, int index) {
        // TODO exchange index and value
        return collection.set(index, value);
    }
}
