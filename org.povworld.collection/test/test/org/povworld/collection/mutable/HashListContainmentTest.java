package test.org.povworld.collection.mutable;

import org.povworld.collection.mutable.HashList;

import test.org.povworld.collection.AbstractContainerTest;

/**
 * Unit tests for {@link HashList}.
 *
 * @see HashListMutationTest
 * @see HashListTest
 */
public class HashListContainmentTest extends AbstractContainerTest<HashList<String>> {
    
    public HashListContainmentTest() {
        super(HashList.<String>newBuilder());
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
}
