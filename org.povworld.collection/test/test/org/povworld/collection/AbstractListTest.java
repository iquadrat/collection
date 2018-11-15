package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.List;

public abstract class AbstractListTest<C extends List<String>> extends AbstractOrderedCollectionTest<C> {
    
    public AbstractListTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Test
    public void get() {
        assertEquals("foobar", collectionSingle.get(0));
        
        assertEquals("one", collectionThree.get(0));
        assertEquals("two", collectionThree.get(1));
        assertEquals("three", collectionThree.get(2));
        
        assertEquals("42", collectionLarge.get(42));
    }
    
    @Test
    public void getOutOfBoundThrows() {
        verifyGetThrows(collectionEmpty, -1);
        verifyGetThrows(collectionEmpty, 0);
        verifyGetThrows(collectionEmpty, 1);
        
        verifyGetThrows(collectionEmpty, 1);
        verifyGetThrows(collectionEmpty, 2);
        
        verifyGetThrows(collectionThree, 3);
        verifyGetThrows(collectionThree, -123);
        verifyGetThrows(collectionThree, 83123);
        
        verifyGetThrows(collectionLarge, -1);
        verifyGetThrows(collectionThree, collectionLarge.size());
    }
    
    private void verifyGetThrows(C collection, int i) {
        try {
            collection.get(i);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
    }
    
}
