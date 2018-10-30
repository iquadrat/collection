package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.povworld.collection.common.ObjectUtil;

public class ObjectUtilTest {
    
    @Test
    public void objectEquals() {
        Object object1 = new Object();
        Object object2 = new Object();
        assertTrue(ObjectUtil.objectEquals(object1, object1));
        assertFalse(ObjectUtil.objectEquals(object1, object2));
        assertFalse(ObjectUtil.objectEquals(object1, null));
        assertFalse(ObjectUtil.objectEquals(null, object2));
        assertTrue(ObjectUtil.objectEquals(null, null));
        assertTrue(ObjectUtil.objectEquals(new String("foo"), new String("foo")));
    }
    
    @Test
    public void hashcode() {
        assertEquals(0, ObjectUtil.hashCode(null));
        assertEquals("foo".hashCode(), ObjectUtil.hashCode("foo"));
    }
    
    @Test
    public void castOrNull() {
        assertEquals(null, ObjectUtil.castOrNull(null, String.class));
        assertEquals(null, ObjectUtil.castOrNull(new Object(), String.class));
        
        String s = new String("foo");
        assertSame(s, ObjectUtil.castOrNull(s, String.class));
        assertSame(s, ObjectUtil.castOrNull(s, Object.class));
    }
    
}
