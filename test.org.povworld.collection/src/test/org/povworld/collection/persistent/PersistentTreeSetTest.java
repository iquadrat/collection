package test.org.povworld.collection.persistent;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.persistent.PersistentOrderedSet;
import org.povworld.collection.persistent.PersistentTreeSet;
import org.povworld.collection.persistent.PersistentTreeSet.BalancerType;

import test.org.povworld.collection.AbstractOrderedSetTest;

@RunWith(Parameterized.class)
public class PersistentTreeSetTest extends AbstractOrderedSetTest<PersistentOrderedSet<String>> {
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{BalancerType.AVL}, {BalancerType.NON_BALANCED}});
    }
    
    public PersistentTreeSetTest(BalancerType balancer) {
        super(PersistentTreeSet.newBuilder(CollectionUtil.getDefaultComparator(String.class), balancer));
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return CollectionUtil.sort(ImmutableCollections.asList(elements));
    }
    
    @Override
    protected Identificator<? super String> getIdentificator() {
        return CollectionUtil.getDefaultComparator(String.class);
    }
    
}
