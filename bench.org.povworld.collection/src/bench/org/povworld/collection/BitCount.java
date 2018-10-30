package bench.org.povworld.collection;

import org.jbenchx.annotations.Bench;

public class BitCount {
    
    private final int x;
    
    private final long y;
    
    public BitCount() {
        x = 1234567;
        y = 12345678901234L;
    }
    
    @Bench
    public int bitCountInt() {
        int a = 0;
        for (int i = 0; i < 1000; ++i) {
            a += Integer.bitCount(x + i);
        }
        return a;
    }
    
    @Bench
    public int bitCountLong() {
        int a = 0;
        for (long i = 0; i < 1000; ++i) {
            a += Long.bitCount(y + i);
        }
        return a;
    }
    
    @Bench
    public int bitCountLongEmulated() {
        int a = 0;
        for (int i = 0; i < 1000; ++i) {
            long y0 = y + i;
            int y1 = (int)(y0 & -1);
            int y2 = (int)(y0 >>> 32);
            a += Integer.bitCount(y1) + Integer.bitCount(y2);
        }
        return a;
    }
    
}
