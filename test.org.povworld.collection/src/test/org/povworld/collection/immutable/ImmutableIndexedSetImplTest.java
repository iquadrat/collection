package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableIndexedSet;
import org.povworld.collection.immutable.ImmutableIndexedSetImpl;
import org.povworld.collection.immutable.ImmutableIndexedSetImpl.Builder;

import test.org.povworld.collection.AbstractOrderedSetTest;

/**
 * Unit tests for {@link ImmutableIndexedSetImpl}.
 */
public class ImmutableIndexedSetImplTest extends AbstractOrderedSetTest<ImmutableIndexedSet<String>> {
    
    public ImmutableIndexedSetImplTest() {
        super(ImmutableIndexedSetImpl.<String>newBuilder());
    }

    @Test
    public void get() {
        assertEquals("foobar", collectionSingle.get(0));
        assertEquals("one", collectionThree.get(0));
        assertEquals("two", collectionThree.get(1));
        assertEquals("three", collectionThree.get(2));
        assertEquals("442", collectionLarge.get(442));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void getNegativeIndex() {
        collectionThree.get(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void getTooLargeIndex() {
        collectionThree.get(3);
    }
    
    @Test
    public void removeFromBuilder() {
        Builder<Integer> builder = ImmutableIndexedSetImpl
                .newBuilder();
        
        builder
                .remove(74)
                .add(143)
                .add(11)
                .add(143)
                .remove(143)
                .remove(143);
        
        ImmutableIndexedSet<Integer> col = builder.build();
        assertEquals(ImmutableCollections.orderedSetOf(11), col);
    }
    
}
