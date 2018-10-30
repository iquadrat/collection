package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.immutable.ImmutableArrayList;
import org.povworld.collection.immutable.ImmutableList;

import test.org.povworld.collection.AbstractListTest;

/**
 * Unit tests for {@link ImmutableArrayList}.
 */
public class ImmutableArrayListTest extends AbstractListTest<ImmutableList<String>> {
    
    public ImmutableArrayListTest() {
        super(ImmutableArrayList.<String>newBuilder());
    }
    
    @Test
    public void testBuildWithExpectedSize() {
        CollectionBuilder<String, ImmutableList<String>> builder = ImmutableArrayList.<String>newBuilder(manyElements.length);
        builder.addAll(collectionLarge);
        ImmutableList<String> collection = builder.build();
        assertEquals(collectionLarge, collection);
    }
    
    @Test
    public void testBuildWithExpectedSizeZero() {
        CollectionBuilder<String, ImmutableList<String>> builder = ImmutableArrayList.<String>newBuilder(0);
        ImmutableList<String> collection = builder.build();
        assertTrue(collection.isEmpty());
        
        builder = ImmutableArrayList.<String>newBuilder(0);
        builder.add("Hello");
        assertEquals(1, builder.build().size());
    }
    
}
