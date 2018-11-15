package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableSet;
import org.povworld.collection.immutable.PerfectHashSet;
import org.povworld.collection.immutable.PerfectHashSet.NoPerfectHashSetFoundException;

import test.org.povworld.collection.AbstractSetTest;
import test.org.povworld.collection.ChosenHash;

public class PerfectHashSetTest extends AbstractSetTest<ImmutableSet<String>> {
    
    private static class TestSetBuilder extends AbstractCollectionBuilder<String, ImmutableSet<String>> {
        
        private final CollectionBuilder<String, ? extends ImmutableSet<String>> builder = PerfectHashSet.newBuilder();
        
        @Override
        protected void _add(String element) {
            builder.add(element);
        }
        
        @Override
        protected ImmutableSet<String> _createCollection() {
            return builder.build();
        }
        
        @Override
        protected void _reset() {
            builder.reset();
        }
        
    }
    
    public PerfectHashSetTest() {
        super(new TestSetBuilder());
    }
    
    @Test(expected = NoPerfectHashSetFoundException.class)
    public void testCannotBuildIfHashIsIdentical() {
        int hash = 918239182;
        PerfectHashSet.of(new ChosenHash("foo", hash), new ChosenHash("bar", hash));
    }
    
    @Test
    public void testLargeSetWithChosenHash() {
        ChosenHash[] array = new ChosenHash[4096];
        for (int i = 0; i < array.length; ++i) {
            array[i] = new ChosenHash(String.valueOf(i), 4 * i + 1234244351);
        }
        PerfectHashSet<ChosenHash> set1 = PerfectHashSet.of(array);
        assertEquals(4096, set1.size());
        assertEquals(ImmutableCollections.setOf(array), set1);
        
        Collections.shuffle(Arrays.asList(array));
        PerfectHashSet<ChosenHash> set2 = PerfectHashSet.of(array);
        assertEquals(set1, set2);
    }
}
