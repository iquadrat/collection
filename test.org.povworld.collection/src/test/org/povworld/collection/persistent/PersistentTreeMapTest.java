package test.org.povworld.collection.persistent;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.persistent.PersistentMap;
import org.povworld.collection.persistent.PersistentTreeMap;
import org.povworld.collection.persistent.PersistentTreeMap.BalancerType;

/**
 * Unit tests for {@link PersistentTreeMap}.
 */
@RunWith(Parameterized.class)
public class PersistentTreeMapTest extends AbstractPersistentMapTest {
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{BalancerType.AVL}, {BalancerType.NON_BALANCED}});
    }
    
    private final BalancerType balancer;
    
    public PersistentTreeMapTest(BalancerType balancer) {
        this.balancer = balancer;
    }
    
    @Override
    protected PersistentMap<String, Integer> empty() {
        return PersistentTreeMap.<String, Integer>empty(String.class, balancer);
    }
    
    @Test
    public void invariants() {
        checkInvariants(mapEmpty);
        checkInvariants(mapSingleton);
        checkInvariants(mapThree);
        checkInvariants(mapLarge);
    }
    
    private void checkInvariants(PersistentMap<String, Integer> map) {
        PersistentTreeMap<?, ?, ?> treeMap = ObjectUtil.castOrNull(map, PersistentTreeMap.class);
        if (treeMap != null) {
            treeMap.checkInvariants();
        }
    }
    
}
