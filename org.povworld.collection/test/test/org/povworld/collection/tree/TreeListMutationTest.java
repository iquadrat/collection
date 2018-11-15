package test.org.povworld.collection.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.mutable.TreeList;

import test.org.povworld.collection.mutable.AbstractMutableIndexedCollectionTest;

public class TreeListMutationTest extends AbstractMutableIndexedCollectionTest<TreeList<Integer>> {
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Override
    protected boolean supportsRemove() {
        return true;
    }
    
    @Override
    protected boolean supportsAddByIndex() {
        return true;
    }
    
    @Override
    protected boolean add(TreeList<Integer> collection, Integer element) {
        collection.add(element);
        return true;
    }
    
    @Override
    protected boolean supportsSet() {
        return true;
    }
    
    @Override
    protected void add(TreeList<Integer> collection, Integer value, int index) {
        collection.add(value, index);
    }
    
    @Override
    protected int addAll(TreeList<Integer> collection, Collection<Integer> elements) {
        collection.addAll(elements);
        return elements.size();
    }
    
    @Override
    protected boolean remove(TreeList<Integer> collection, Integer element) {
        return collection.remove(element);
    }
    
    @Override
    protected int removeAll(TreeList<Integer> collection, Collection<Integer> elements) {
        return collection.removeAll(elements);
    }
    
    @Override
    protected void clear(TreeList<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected TreeList<Integer> create() {
        return new TreeList<Integer>();
    }
    
    @Override
    protected Integer set(TreeList<Integer> collection, Integer value, int index) {
        return collection.set(index, value);
    }
    
    @Test
    public void setAndContains() {
        collection = create(1, 2, 3, 4);
        collection.set(2, 42);
        assertTrue(collection.contains(42));
        assertFalse(collection.contains(3));
        
        collection.set(1, 1);
        assertTrue(collection.contains(1));
        assertFalse(collection.contains(2));
        assertEquals(0, collection.indexOf(1));
        assertEquals(1, collection.lastIndexOf(1));
    }
}
