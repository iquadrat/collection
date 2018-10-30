package test.org.povworld.collection.persistent;

import org.povworld.collection.persistent.PersistentIndexedSet;
import org.povworld.collection.persistent.PersistentIndexedSetImpl;

import test.org.povworld.collection.AbstractOrderedSetTest;

public class PersistentIndexedSetTest extends AbstractOrderedSetTest<PersistentIndexedSet<String>> {
    
    public PersistentIndexedSetTest() {
        super(PersistentIndexedSetImpl.<String>newBuilder());
    }
    
}
