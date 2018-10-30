package bench.org.povworld.collection.persistent;

import java.util.Arrays;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.ForEachInt;

public class ArrayCopyBench {
    
    private final int[] array;
    
    public ArrayCopyBench(@ForEachInt({1, 8, 16, 32, 64}) int size) {
        array = new int[size];
        for (int i = 0; i < size; ++i) {
            array[i] = 1932 + i * 123213;
        }
    }
    
    @Override
    @Bench
    public Object clone() {
        return array.clone();
    }
    
    @Bench
    public Object copyArrays() {
        return Arrays.copyOf(array, array.length);
    }
    
    @Bench
    public Object copySystem() {
        int[] dst = new int[array.length];
        System.arraycopy(array, 0, dst, 0, array.length);
        return dst;
    }
    
    @Bench
    public Object copyLoop() {
        int[] dst = new int[array.length];
        for (int i = 0; i < array.length; ++i) {
            dst[i] = array[i];
        }
        return dst;
    }
    
}
