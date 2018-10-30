package test.org.povworld.collection.adapt;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.adapt.ListAdapter;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;

import test.org.povworld.collection.AbstractListTest;

/**
 * Unit tests for {@link ListAdapter}.
 */
public class ListAdapterTest extends AbstractListTest<ListAdapter<String>> {
    
    private static class Builder<E> extends AbstractCollectionBuilder<E, ListAdapter<E>> {
        
        private java.util.List<E> list = new java.util.ArrayList<E>();
        
        @Override
        protected void _add(E element) {
            list.add(element);
        }
        
        @Override
        protected void _reset() {
            list = new java.util.ArrayList<E>();
        }
        
        @Override
        protected ListAdapter<E> _createCollection() {
            return new ListAdapter<E>(list);
        }
        
    }
    
    public ListAdapterTest() {
        super(new Builder<String>());
    }
    
    @Override
    protected Iterator<String> modifyingIterator(ListAdapter<String> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void add() {
        ListAdapter<String> list = collectionEmpty;
        list.add("foo");
        list.add("bar");
        assertEquals(ImmutableCollections.listOf("foo", "bar"), list);
        
        list.add(0, "moo");
        list.add(3, "zoo");
        assertEquals(ImmutableCollections.listOf("moo", "foo", "bar", "zoo"), list);
    }
    
    @Test
    public void remove() {
        collectionThree.remove(2);
        assertEquals(ImmutableCollections.listOf(one, two), collectionThree);
        
        collectionThree.remove(0);
        assertEquals(ImmutableCollections.listOf(two), collectionThree);
    }
    
}
