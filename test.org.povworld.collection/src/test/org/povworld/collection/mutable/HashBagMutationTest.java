package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.Collection;
import org.povworld.collection.mutable.HashBag;

/**
 * Mutation tests for {@link HashBag}.
 *
 * @see HashBagTest
 */
public class HashBagMutationTest extends AbstractMutableCollectionTest<HashBag<Integer>> {
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Override
    protected boolean supportsRemove() {
        return true;
    }
    
    @Override
    protected boolean add(HashBag<Integer> collection, Integer element) {
        collection.add(element);
        return true;
    }
    
    @Override
    protected int addAll(HashBag<Integer> collection, Collection<Integer> elements) {
        collection.addAll(elements);
        return elements.size();
    }
    
    @Override
    protected boolean remove(HashBag<Integer> collection, Integer element) {
        return collection.remove(element) != -1;
    }
    
    @Override
    protected int removeAll(HashBag<Integer> collection, Collection<Integer> elements) {
        return collection.removeAll(elements);
    }
    
    @Override
    protected void clear(HashBag<Integer> collection) {
        collection.clear();
    }
    
    @Override
    protected boolean contains(HashBag<Integer> collection, Integer element) {
        return collection.contains(element);
    }
    
//  
//  @Test
//  @Override
//  public void remove() {
//    HashBag<String> bag = new HashBag<String>();
//    
//    assertEquals(-1, bag.remove("foo"));
//    
//    bag.addAll(ImmutableCollections.asList("foo", "bar", "foo"));
//    assertEquals(2, bag.getCount("foo"));
//    assertEquals(1, bag.getCount("bar"));
//    assertEquals(2, bag.getNumberOfDifferentElements());
//    
//    assertEquals(1, bag.remove("foo"));
//    assertEquals(1, bag.getCount("foo"));
//    assertEquals(1, bag.getCount("bar"));
//    assertEquals(2, bag.getNumberOfDifferentElements());
//    
//    assertEquals(0, bag.remove("bar"));
//    assertEquals(1, bag.getCount("foo"));
//    assertEquals(0, bag.getCount("bar"));
//    assertEquals(1, bag.getNumberOfDifferentElements());
//    
//    assertEquals(0, bag.remove("foo"));
//    assertEquals(0, bag.getCount("foo"));
//    assertEquals(0, bag.getCount("bar"));
//    assertEquals(0, bag.getNumberOfDifferentElements());
//    
//    assertTrue(bag.isEmpty());
//    assertEquals(0, bag.getNumberOfDifferentElements());
//  }
//  
//  @Override
//  @Test
//  public void removeAll() {
//    HashBag<String> bag = new HashBag<String>("1", "2", "3", "2", "1");
//    assertEquals(0, bag.removeAll("0"));
//    assertEquals(new HashBag<String>("1", "1", "2", "2", "3"), bag);
//    
//    assertEquals(2, bag.removeAll("1"));
//    assertEquals(new HashBag<String>("2", "2", "3"), bag);
//    assertEquals(0, bag.getCount("1"));
//    assertEquals(2, bag.getCount("2"));
//    assertEquals(3, bag.size());
//    assertEquals(2, bag.getNumberOfDifferentElements());
//    
//    assertEquals(1, bag.removeAll("3"));
//    assertEquals(new HashBag<String>("2", "2"), bag);
//    assertEquals(0, bag.getCount("3"));
//    assertEquals(2, bag.getCount("2"));
//    assertEquals(2, bag.size());
//    assertEquals(1, bag.getNumberOfDifferentElements());
//  }
    
    @Override
    protected HashBag<Integer> create() {
        return new HashBag<Integer>();
    }
    
    @Test
    public void removeAllOccurrences() {
        HashBag<Integer> bag = create(1, 4, 1, 2, 3, 5, 1, 4, 4, 1, 2);
        assertEquals(2, bag.removeAllOccurrences(2));
        assertEquals(create(1, 4, 1, 3, 5, 1, 4, 4, 1), bag);
        
        assertEquals(3, bag.removeAllOccurrences(4));
        assertEquals(create(1, 1, 3, 5, 1, 1), bag);
        
        assertEquals(4, bag.removeAllOccurrences(1));
        assertEquals(create(3, 5), bag);
        
        assertEquals(0, bag.removeAllOccurrences(7));
        assertEquals(create(3, 5), bag);
    }
    
}
