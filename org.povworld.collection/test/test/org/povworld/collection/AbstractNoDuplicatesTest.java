package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.Container;
import org.povworld.collection.Identificator;
import org.povworld.collection.persistent.PersistentHashSet;
import org.povworld.collection.persistent.PersistentSet;

public abstract class AbstractNoDuplicatesTest<C extends Container<String>> extends AbstractContainerTest<C> {
    
    public AbstractNoDuplicatesTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return false;
    }
    
    @Test
    public void noDuplicatesAllowed() {
        builder.reset()
                .add("0")
                .add("1")
                .add("0")
                .add("1")
                .add("2");
        
        C set = builder.build();
        assertEquals(3, set.size());
        assertTrue(set.contains("0"));
        assertTrue(set.contains("1"));
        assertTrue(set.contains("2"));
    }
    
    @Test
    public void equalsMustConsiderIndentificator() {
        // TODO identificator could be a mock
        PersistentSet<String> empty = PersistentHashSet.<String>empty(new Identificator<String>() {
            
            @Override
            public boolean isIdentifiable(Object object) {
                return (object instanceof String);
            }
            
            @Override
            public boolean equals(String object1, String object2) {
                return object1.length() == object2.length();
            }
            
            @Override
            public int hashCode(String object) {
                return object.length();
            }
            
        });
        assertFalse(collectionEmpty.equals(empty));
        
        PersistentSet<String> three = empty.withAll(collectionThree);
        assertFalse(collectionThree.equals(three));
    }
    
}
