package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.ObjectUtil;

public class DefaultIdentificatorTest {
    
    private interface TestMock {
        
        public boolean _equals(Object obj);
        
        public int _hashCode();
        
    }
    
    private static class TestObject {
        
        private final TestMock mock;
        
        public TestObject(TestMock mock) {
            this.mock = mock;
        }
        
        @Override
        public boolean equals(Object obj) {
            TestObject other = ObjectUtil.castOrNull(obj, TestObject.class);
            if (other == null) return false;
            return mock._equals(other.mock);
        }
        
        @Override
        public int hashCode() {
            return mock._hashCode();
        }
        
    }
    
    private final Identificator<Object> identificator = CollectionUtil.getObjectIdentificator();
    
    private final TestMock mock1 = mock(TestMock.class);
    
    private final TestMock mock2 = mock(TestMock.class);
    
    private final TestObject object1 = new TestObject(mock1);
    
    private final TestObject object2 = new TestObject(mock2);
    
    @Test
    public void equals() {
        when(mock1._equals(mock2)).thenReturn(false);
        when(mock2._equals(mock1)).thenReturn(false);
        when(mock1._equals(mock1)).thenReturn(true);
        
        assertFalse(identificator.equals(object1, object2));
        assertFalse(identificator.equals(object2, object1));
        assertTrue(identificator.equals(object1, object1));
    }
    
    @Test
    public void equalsDifferentObjs() {
        when(mock1._equals(mock2)).thenReturn(true);
        assertTrue(identificator.equals(object1, object2));
    }
    
    @Test
    public void verifyHashcode() {
        when(mock1._hashCode()).thenReturn(8819);
        assertEquals(ObjectUtil.strengthenedHashcode(8819), identificator.hashCode(object1));
    }
    
}
