package test.org.povworld.collection.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Comparator;

import org.junit.Test;
import org.mockito.Mockito;
import org.povworld.collection.CollectionUtil;

public class DefaultComparatorTest {
    
    private interface MyThing extends Comparable<MyThing> {
    }
    
    private final Comparator<MyThing> comparator = CollectionUtil.getDefaultComparator(MyThing.class);
    
    private final MyThing thing1 = Mockito.mock(MyThing.class);
    
    private final MyThing thing2 = Mockito.mock(MyThing.class);
    
    @Test
    public void compare() {
        when(thing1.compareTo(thing2)).thenReturn(51);
        when(thing2.compareTo(thing1)).thenReturn(-11);
        when(thing1.compareTo(thing1)).thenReturn(0);
        when(thing2.compareTo(thing2)).thenReturn(0);
        
        assertTrue(comparator.compare(thing1, thing1) == 0);
        assertTrue(comparator.compare(thing1, thing2) > 0);
        assertTrue(comparator.compare(thing2, thing1) < 0);
        assertTrue(comparator.compare(thing2, thing2) == 0);
    }
    
}
