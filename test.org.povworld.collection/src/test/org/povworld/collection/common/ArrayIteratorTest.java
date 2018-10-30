package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.List;
import org.povworld.collection.common.ArrayIterator;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;

import test.org.povworld.collection.TestUtil;

public class ArrayIteratorTest {
    
    @Test
    public void iterateEmptyArray() {
        test();
    }
    
    @Test
    public void iterateOneElementArray() {
        test("foo");
    }
    
    @Test
    public void iterateLargeArray() {
        String[] array = new String[100];
        for (int i = 0; i < 100; ++i) {
            array[i] = String.valueOf(i);
        }
        test(array);
    }
    
    private void test(String... array) {
        List<String> elements = TestUtil.verifyIteratorMultipleHasNext(new ArrayIterator<String>(array), array.length,
                ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf(array), elements);
        
        elements = TestUtil.verifyIteratorNoHasNext(new ArrayIterator<String>(array), array.length, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf(array), elements);
        
        elements = TestUtil.verifyIteratorStandardPattern(new ArrayIterator<String>(array), array.length, ArrayList.<String>newBuilder());
        assertEquals(ImmutableCollections.listOf(array), elements);
    }
    
}
