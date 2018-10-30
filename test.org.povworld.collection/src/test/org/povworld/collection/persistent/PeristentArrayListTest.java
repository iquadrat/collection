package test.org.povworld.collection.persistent;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.persistent.PersistentArrayList;
import org.povworld.collection.persistent.PersistentList;

/**
 * Unit tests for {@link PersistentArrayList}.
 */
public class PeristentArrayListTest extends AbstractPersistentListTest<PersistentList<String>> {
    
    public PeristentArrayListTest() {
        super(PersistentArrayList.<String>newBuilder());
    }
    
    @Test
    public void createFromNonFinalArray() {
        String[] elements = new String[] {"a"};
        PersistentArrayList<String> list = new PersistentArrayList<>(elements);
        assertEquals("a", list.get(0));
        elements[0] = "b";
        assertEquals("a", list.get(0));
    }
    
}
