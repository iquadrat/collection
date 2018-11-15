package test.org.povworld.collection.adapt;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.adapt.SetAdapter;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;

import test.org.povworld.collection.AbstractSetTest;

public class SetAdapterTest extends AbstractSetTest<SetAdapter<String>> {
    
    private static class Builder<E> extends AbstractCollectionBuilder<E, SetAdapter<E>> {
        
        private java.util.Set<E> set = new java.util.HashSet<E>();
        
        @Override
        protected void _add(E element) {
            set.add(element);
        }
        
        @Override
        protected void _reset() {
            set = new java.util.HashSet<E>();
        }
        
        @Override
        protected SetAdapter<E> _createCollection() {
            return new SetAdapter<E>(set);
        }
        
    }
    
    public SetAdapterTest() {
        super(new Builder<String>());
    }
    
    @Override
    protected Iterator<String> modifyingIterator(SetAdapter<String> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void add() {
        SetAdapter<String> set = collectionEmpty;
        set.add("foo");
        set.add("bar");
        assertEquals(ImmutableCollections.setOf("foo", "bar"), set);
    }
    
    @Test
    public void remove() {
        collectionThree.remove(three);
        assertEquals(ImmutableCollections.setOf(one, two), collectionThree);
        
        collectionThree.remove(one);
        assertEquals(ImmutableCollections.setOf(two), collectionThree);
    }
    
    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void findEqualOrNull() {
        collectionSingle.findEqualOrNull("foo");
    }
    
}
