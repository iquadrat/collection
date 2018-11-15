package test.org.povworld.collection.mutable;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeSet;

import test.org.povworld.collection.AbstractOrderedSetTest;

/**
 * Unit tests for {@link TreeSet}.
 * 
 * @see TreeSetMutationTest
 */
public class TreeSetTest extends AbstractOrderedSetTest<TreeSet<String>> {
    
    public TreeSetTest() {
        super(TreeSet.newBuilder(String.class));
    }
    
    @Override
    protected Identificator<? super String> getIdentificator() {
        return CollectionUtil.getDefaultComparator(String.class);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return CollectionUtil.sort(ImmutableCollections.asList(elements));
    }
    
}
