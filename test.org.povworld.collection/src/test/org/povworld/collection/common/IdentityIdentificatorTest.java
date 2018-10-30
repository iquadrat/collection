package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.IdentityIdentificator;

public class IdentityIdentificatorTest {
    
    private static final Identificator<Object> INSTANCE = IdentityIdentificator.getInstance();
    
    private final Object o1 = new Object();
    
    private final Object o2 = new Object();
    
    @Test
    public void isIdentifiable() {
        assertTrue(INSTANCE.isIdentifiable("fii"));
        assertTrue(INSTANCE.isIdentifiable(new Object()));
    }
    
    @Test
    public void equalsTwoObjects() {
        assertTrue(INSTANCE.equals(o1, o1));
        assertFalse(INSTANCE.equals(o1, o2));
        assertFalse(INSTANCE.equals("foo", new String("foo")));
    }
    
    @Test
    public void hashCodeObject() {
        assertEquals(o1.hashCode(), INSTANCE.hashCode(o1));
        assertEquals(System.identityHashCode("foo"), INSTANCE.hashCode("foo"));
    }
    
}
