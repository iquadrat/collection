package bench.org.povworld.collection;

import org.jbenchx.annotations.Bench;

public class Interpolation {
    
    int size;
    
    int hashvalue;
    
    int[] hashValues;
    
    public Interpolation() {
        size = 54;
        hashvalue = 1873812738;
        hashValues = new int[size];
        hashValues[0] = -1367814744;
        hashValues[53] = 378941798;
    }
    
    @Bench
    public int longInterpolation() {
        return (int)(((size - 1) * ((long)hashvalue - hashValues[0])) / ((long)hashValues[size - 1] - hashValues[0]));
    }
    
    @Bench
    public int doubleInterpolation() {
        return (int)(((size - 1) * ((double)hashvalue - hashValues[0])) / ((double)hashValues[size - 1] - hashValues[0]));
    }
    
    @Bench
    public int roundedDoubleInterpolation() {
        return (int)Math.round((((size - 1) * ((double)hashvalue - hashValues[0])) / ((double)hashValues[size - 1] - hashValues[0])));
    }
    
    @Bench
    public int floatInterpolation() {
        return (int)(((size - 1) * ((float)hashvalue - hashValues[0])) / ((float)hashValues[size - 1] - hashValues[0]));
    }
    
    @Bench
    public int floatLongInterpolation() {
        return (int)(((size - 1) * ((float)((long)hashvalue - hashValues[0]))) / ((long)hashValues[size - 1] - hashValues[0]));
    }
    
    @Bench
    public int roundedFloatInterpolation() {
        return Math.round((((size - 1) * ((float)hashvalue - hashValues[0])) / ((float)hashValues[size - 1] - hashValues[0])));
    }
    
    @Bench
    public int intInterpolation() {
        return ((size - 1) * (hashvalue - hashValues[0])) / (hashValues[size - 1] - hashValues[0]);
    }
    
}
