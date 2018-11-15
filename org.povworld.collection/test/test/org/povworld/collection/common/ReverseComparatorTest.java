package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.common.ReverseComparator;

@RunWith(MockitoJUnitRunner.class)
public class ReverseComparatorTest {
    
    private final String o1 = "a";
    
    private final String o2 = "b";
    
    private final String o3 = "c";
    
    @Mock
    private Comparator<String> delegate;
    
    private ReverseComparator<String> comparator;
    
    @Before
    public void setUp() {
        comparator = new ReverseComparator<String>(delegate);
    }
    
    @Test
    public void compare() {
        when(delegate.compare("b", "a")).thenReturn(-1);
        when(delegate.compare("c", "a")).thenReturn(0);
        when(delegate.compare("d", "a")).thenReturn(1);
        
        assertEquals(-1, comparator.compare("a", "b"));
        assertEquals(0, comparator.compare("a", "c"));
        assertEquals(1, comparator.compare("a", "d"));
    }
    
    @Test
    public void isIdentifiable() {
        when(delegate.isIdentifiable(o1)).thenReturn(true);
        when(delegate.isIdentifiable(o2)).thenReturn(false);
        
        assertTrue(comparator.isIdentifiable(o1));
        assertFalse(comparator.isIdentifiable(o2));
    }
    
    @Test
    public void equalsTwoObjects() {
        when(delegate.equals(o1, o2)).thenReturn(true);
        when(delegate.equals(o1, o3)).thenReturn(false);
        
        assertTrue(comparator.equals(o1, o2));
        assertFalse(comparator.equals(o1, o3));
    }
    
    @Test
    public void hashCodeOfObject() {
        when(delegate.hashCode(o1)).thenReturn(22);
        assertEquals(22, comparator.hashCode(o1));
    }
    
    @Test
    public void hashCodeImpl() {
        assertEquals(comparator.hashCode(), comparator.hashCode());
        Mockito.validateMockitoUsage();
    }
    
    @Test
    public void equalsImpl() {
        assertFalse(comparator.equals(null));
        assertFalse(comparator.equals(CollectionUtil.getDefaultComparator(String.class)));
        assertTrue(comparator.equals(comparator));
        assertTrue(comparator.equals(new ReverseComparator<>(delegate)));
    }
    
}
