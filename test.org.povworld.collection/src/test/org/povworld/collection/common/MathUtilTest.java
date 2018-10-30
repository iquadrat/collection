package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.common.MathUtil;

public class MathUtilTest {
    
    @Test
    public void nextPowerOfTwo() {
        assertEquals(1, MathUtil.nextPowerOfTwo(-13));
        assertEquals(1, MathUtil.nextPowerOfTwo(-1));
        assertEquals(1, MathUtil.nextPowerOfTwo(0));
        assertEquals(1, MathUtil.nextPowerOfTwo(1));
        assertEquals(2, MathUtil.nextPowerOfTwo(2));
        assertEquals(4, MathUtil.nextPowerOfTwo(3));
        assertEquals(4, MathUtil.nextPowerOfTwo(4));
        assertEquals(16, MathUtil.nextPowerOfTwo(15));
        assertEquals(16, MathUtil.nextPowerOfTwo(16));
        assertEquals(2048, MathUtil.nextPowerOfTwo(1314));
    }
    
}
