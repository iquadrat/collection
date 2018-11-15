package test.org.povworld.collection.common;

import org.junit.Test;
import org.povworld.collection.common.PreConditions;

public class PreConditionsTest {
    
    @Test
    public void paramNotNull() {
        PreConditions.paramNotNull(new Object());
        PreConditions.paramNotNull("");
    }
    
    @Test(expected = NullPointerException.class)
    public void paramNotNullThrowsDynamicEnabled() throws Exception {
        if (!PreConditions.dynamicNullPointerChecksEnabled()) {
            throw new NullPointerException();
        }
        PreConditions.paramNotNull(null);
    }
    
    @Test
    public void paramNotNullDynamicDisabled() throws Exception {
        if (PreConditions.dynamicNullPointerChecksEnabled()) {
            return;
        }
        PreConditions.paramNotNull(null);
    }
    
    @Test
    public void paramCheck() {
        PreConditions.paramCheck("foobar", "message", true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void paramCheckFails() {
        PreConditions.paramCheck("foobar", "message", false);
    }
    
    @Test
    public void conditionCheck() {
        PreConditions.conditionCheck("message", true);
    }
    
    @Test(expected = IllegalStateException.class)
    public void conditionCheckFails() {
        PreConditions.conditionCheck("message", false);
    }
    
}
