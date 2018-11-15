package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;
import org.povworld.collection.Set;
import org.povworld.collection.common.ShuffledIterable;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableList;
import org.povworld.collection.mutable.HashSet;

import test.org.povworld.collection.TestUtil;

public class ShuffledIterableTest {
    
    private final ImmutableList<String> list;
    
    private final ShuffledIterable<String> iterable;
    
    public ShuffledIterableTest() {
        list = ImmutableCollections.listOf("one", "two", "three");
        iterable = new ShuffledIterable<>(list, new Random(1));
    }
    
    @Test
    public void iterate() {
        Set<String> actual = TestUtil.verifyIterable(iterable, HashSet.<String>newBuilder());
        assertEquals(ImmutableCollections.asSet(list), actual);
    }
    
    @Test
    public void removeNotSupported() {
        TestUtil.verifyIteratableRemoveUnsupported(iterable);
    }
    
}
