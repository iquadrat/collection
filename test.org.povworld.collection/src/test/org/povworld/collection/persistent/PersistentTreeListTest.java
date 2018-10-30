package test.org.povworld.collection.persistent;

import org.junit.Test;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.persistent.PersistentList;
import org.povworld.collection.persistent.PersistentTreeList;

/**
 * Unit tests for {@link PersistentTreeList}.
 */
public class PersistentTreeListTest extends AbstractPersistentListTest<PersistentList<String>> {
    
    public PersistentTreeListTest() {
        super(PersistentTreeList.<String>newBuilder());
    }
    
    @Test
    public void checkInvariants() {
        checkInvariants(collectionEmpty);
        checkInvariants(collectionSingle);
        checkInvariants(collectionThree);
        checkInvariants(collectionLarge);
    }
    
    private void checkInvariants(PersistentList<?> list) {
        PersistentTreeList<?> treeList = ObjectUtil.castOrNull(list, PersistentTreeList.class);
        if (treeList != null) {
            treeList.checkInvariants();
        }
    }
    
}
