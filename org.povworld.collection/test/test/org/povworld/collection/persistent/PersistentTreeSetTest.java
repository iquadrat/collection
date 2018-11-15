package test.org.povworld.collection.persistent;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.TreeSet;
import org.povworld.collection.persistent.PersistentOrderedSet;
import org.povworld.collection.persistent.PersistentTreeSet;
import org.povworld.collection.persistent.PersistentTreeSet.BalancerType;

/**
 * Unit tests for {@link PersistentTreeSet}. 
 */
@RunWith(Parameterized.class)
public class PersistentTreeSetTest extends AbstractPersistentOrderedSetTest<PersistentOrderedSet<String>> {
    
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
    
    @Override
    protected OrderedSet<String> setOf(String... elements) {
        return TreeSet.newBuilder(String.class).addAll(CollectionUtil.wrap(elements)).build();
    }

    @Override
    protected void checkInvariants(PersistentOrderedSet<?> set) {
        PersistentTreeSet<?, ?> treeSet = ObjectUtil.castOrNull(set, PersistentTreeSet.class);
        // Cast does not succeed if it is the special object for 'empty'.
        if (treeSet != null) {
            treeSet.checkInvariants();
        }
    }
    
}
