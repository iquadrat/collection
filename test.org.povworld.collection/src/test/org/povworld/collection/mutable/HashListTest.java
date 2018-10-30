package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.povworld.collection.CollectionUtil.wrap;

import org.junit.Test;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.HashList;

import test.org.povworld.collection.AbstractListTest;

/**
 * Unit tests for {@link HashList}.
 * 
 * @see HashListMutationTest
 * @see HashListTest 
 */
public class HashListTest extends AbstractListTest<HashList<String>> {
    
    public HashListTest() {
        super(HashList.<String>newBuilder());
    }
    
    @Test
    public void createSmall() {
        Integer[] array = new Integer[] {1, 2, 3, 4, 5};
        List<Integer> elements = ImmutableCollections.listOf(array);
        HashList<Integer> list1 = HashList.create(elements);
        HashList<Integer> list2 = HashList.<Integer>newBuilder(5).addAll(elements).build();
        HashList<Integer> list3 = HashList.create(wrap(array));
        HashList<Integer> list4 = HashList.create(elements);
        
        assertEquals(5, list1.size());
        assertEquals(elements, list1);
        assertEquals(elements, list2);
        assertEquals(elements, list3);
        assertEquals(elements, list4);
    }
    
    @Test
    public void createLarge() {
        Integer[] array = new Integer[55];
        for (int i = 0; i < 55; ++i) {
            array[i] = Integer.valueOf(i);
        }
        List<Integer> elements = ImmutableCollections.listOf(array);
        HashList<Integer> list1 = HashList.create(elements);
        HashList<Integer> list2 = HashList.<Integer>newBuilder(55).addAll(elements).build();
        HashList<Integer> list3 = HashList.<Integer>newBuilder(5).addAll(elements).build();
        HashList<Integer> list4 = HashList.create(wrap(array));
        HashList<Integer> list5 = HashList.create(elements);
        
        assertEquals(55, list1.size());
        assertEquals(elements, list1);
        assertEquals(elements, list2);
        assertEquals(elements, list3);
        assertEquals(elements, list4);
        assertEquals(elements, list5);
    }
}
