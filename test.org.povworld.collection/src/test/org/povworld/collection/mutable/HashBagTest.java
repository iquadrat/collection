package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.mutable.HashBag;
import org.povworld.collection.mutable.MutableCollections;

import test.org.povworld.collection.AbstractContainerTest;
import test.org.povworld.collection.TestUtil;

/**
 * Read tests for {@link HashBag}
 *
 * @see HashBagMutationTest
 */
public class HashBagTest extends AbstractContainerTest<HashBag<String>> {
    
    public HashBagTest() {
        super(HashBag.<String>newBuilder());
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return HashBag.<String>newBuilder().addAll(elements).build();
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Test
    public void create() {
        HashBag<String> bag0 = new HashBag<String>("foo", "bar", "foo");
        HashBag<String> bag1 = new HashBag<String>(bag0);
        assertEquals(bag0, bag1);
    }
    
    @Override
    @SuppressWarnings("unlikely-arg-type")
    protected void verifyEqualsAndHashcodeForUnOrdered() {
        super.verifyEqualsAndHashcodeForUnOrdered();
        
        HashBag<String> bag0 = new HashBag<String>("1", "2", "3", "2", "1");
        HashBag<String> bag1 = new HashBag<String>("1", "1", "2", "2", "3");
        HashBag<String> bag2 = new HashBag<String>("1", "1", "2", "3", "3");
        
        verifyEqualsAndHashCode(new TestUtil.UnOrderedHasher(), bag0, bag1, bag2);
        
        assertTrue(build("1", "1", "2").equals(build("2", "1", "1")));
        assertFalse(build("1", "1", "2").equals(build("2", "2", "1")));
        assertFalse(MutableCollections.orderedSetOf("1", "2", "3").equals(build("1", "1", "2")));
    }
    
}
