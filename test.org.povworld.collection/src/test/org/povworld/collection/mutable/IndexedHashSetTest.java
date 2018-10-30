package test.org.povworld.collection.mutable;

import org.povworld.collection.mutable.IndexedHashSet;

import test.org.povworld.collection.AbstractOrderedSetTest;

/**
 * Unit tests for {@link IndexedHashSet}.
 */
public class IndexedHashSetTest extends AbstractOrderedSetTest<IndexedHashSet<String>> {
    
    public IndexedHashSetTest() {
        super(IndexedHashSet.<String>newBuilder());
    }
}
