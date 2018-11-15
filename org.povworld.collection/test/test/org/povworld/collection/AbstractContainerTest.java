package test.org.povworld.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Container;
import org.povworld.collection.Identificator;
import org.povworld.collection.immutable.ImmutableCollections;

public abstract class AbstractContainerTest<C extends Container<String>> extends AbstractStringCollectionTest<C> {
    
    public AbstractContainerTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    protected Identificator<? super String> getIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Test
    public void identificator() {
        assertEquals(getIdentificator(), collectionEmpty.getIdentificator());
        assertEquals(getIdentificator(), collectionSingle.getIdentificator());
        assertEquals(getIdentificator(), collectionThree.getIdentificator());
        assertEquals(getIdentificator(), collectionLarge.getIdentificator());
    }
    
    @Test
    public void contains() {
        assertFalse(collectionEmpty.contains(""));
        assertFalse(collectionSingle.contains("foo"));
        assertTrue(collectionSingle.contains(collectionSingle.getFirst()));
        assertTrue(collectionThree.contains("three"));
        assertFalse(collectionThree.contains("four"));
        assertTrue(collectionLarge.contains("42"));
        assertFalse(collectionLarge.contains("bar"));
        for (String element: collectionLarge) {
            assertTrue("element " + element + " is missing!",
                    collectionLarge.contains(element));
        }
    }
    
    @Test
    public void containsAll() {
        assertTrue(collectionThree.containsAll(ImmutableCollections.listOf()));
        assertTrue(collectionThree.containsAll(ImmutableCollections.listOf("one")));
        assertTrue(collectionThree.containsAll(ImmutableCollections.listOf("one", "three", "one")));
        assertFalse(collectionThree.containsAll(ImmutableCollections.listOf("one", "four")));
    }
    
    @Test
    public void findEqualOrNull() {
        String one = new String("one");
        String found = collectionThree.findEqualOrNull(one);
        assertEquals(one, found);
        assertNotSame(one, found);
        
        assertSame(found, collectionThree.findEqualOrNull(found));
        
        assertNull(collectionThree.findEqualOrNull("four"));
    }
    
}
