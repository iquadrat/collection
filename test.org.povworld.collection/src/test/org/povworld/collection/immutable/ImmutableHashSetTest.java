package test.org.povworld.collection.immutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableHashSet;
import org.povworld.collection.immutable.ImmutableHashSet.Builder;
import org.povworld.collection.immutable.ImmutableSet;

import test.org.povworld.collection.AbstractSetTest;

/**
 * Unit tests for {@link ImmutableHashSet}.
 */
public class ImmutableHashSetTest extends AbstractSetTest<ImmutableSet<String>> {
    
    public ImmutableHashSetTest() {
        super(ImmutableHashSet.<String>newBuilder());
    }
    
    @Test
    public void removeFromBuilder() {
        Builder<Integer> builder = ImmutableHashSet.newBuilder();
        builder.remove(74).add(143).add(11).add(143);
        builder.remove(143).remove(143);
        ImmutableSet<Integer> col = builder.build();
        assertEquals(ImmutableCollections.setOf(11), col);
    }
    
}
