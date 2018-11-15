package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.common.SingletonIterator;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;

import test.org.povworld.collection.TestUtil;

public class SingletonIteratorTest {
    
    protected static final String THE_ELEMENT = "foo";
    
    private Iterable<String> iterable;
    
    public SingletonIteratorTest() {
        iterable = new Iterable<String>() {
            
            @Override
            public Iterator<String> iterator() {
                return new SingletonIterator<String>(THE_ELEMENT);
            }
            
        };
    }
    
    @Test
    public void iterate() {
        ArrayList<String> actual = TestUtil.verifyIterable(iterable, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf(THE_ELEMENT), actual);
    }
    
    @Test
    public void iterateRemoveNotSupported() {
        TestUtil.verifyIteratableRemoveUnsupported(iterable);
    }
    
}
