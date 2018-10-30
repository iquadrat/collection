package org.povworld.collection.common;

import java.util.Random;

public class MathUtil {
    
    private MathUtil() {}
    
    /**
     * Returns 2^k for the smallest positive integer k such that 2^k >= n. If n is negative,
     * then the result is always 1.
     */
    public static int nextPowerOfTwo(int n) {
        int k = 1;
        while (k < n) {
            k *= 2;
        }
        return k;
    }
    
    public static int log2(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        return 31 - Integer.numberOfLeadingZeros(n);
    }
    
    public static int[] identityPermutation(int size) {
        int[] permutation = new int[size];
        for (int i = 0; i < size; ++i) {
            permutation[i] = i;
        }
        return permutation;
    }
    
    public static int[] randomPermutation(int size, Random random) {
        int[] permutation = new int[size];
        for (int i = 0; i < size; i++) {
            int j = random.nextInt(i + 1);
            permutation[i] = permutation[j];
            permutation[j] = i;
        }
        return permutation;
    }
    
}
