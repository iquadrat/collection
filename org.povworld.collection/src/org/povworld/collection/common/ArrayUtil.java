package org.povworld.collection.common;

import java.lang.reflect.Array;
import java.util.Arrays;

import javax.annotation.CheckForNull;

public class ArrayUtil {
    
    private static final Object[] EMPTY_ARRAY = new Object[0];
    
    /**
     * Returns an array containing the given {@code elements}.
     */
    @SafeVarargs
    public static <E> E[] arrayOf(E... elements) {
        return elements;
    }
    
    /**
     * Creates a new array of element type <E> and given {@code length}.
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] newArray(Class<E> elementType, int length) {
        return (E[])Array.newInstance(elementType, length);
    }
    
    /**
     * Creates a new array with the same type as given array and of given {@code length}.
     */
    public static <E> E[] newArray(E[] arrayWithType, int length) {
        @SuppressWarnings("unchecked")
        Class<E> elementType = (Class<E>)arrayWithType.getClass().getComponentType();
        return newArray(elementType, length);
    }
    
    public static <E> E[] appendArrayElement(E[] array, @CheckForNull E element) {
        E[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> boolean[] appendArrayElement(boolean[] array, boolean element) {
        boolean[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> byte[] appendArrayElement(byte[] array, byte element) {
        byte[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> char[] appendArrayElement(char[] array, char element) {
        char[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> int[] appendArrayElement(int[] array, int element) {
        int[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> long[] appendArrayElement(long[] array, long element) {
        long[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> float[] appendArrayElement(float[] array, float element) {
        float[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> double[] appendArrayElement(double[] array, double element) {
        double[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = element;
        return result;
    }
    
    public static <E> E[] prependArrayElement(E[] array, @CheckForNull E element) {
        E[] result = newArray(array, array.length + 1);
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static boolean[] prependArrayElement(boolean[] array, boolean element) {
        boolean[] result = new boolean[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static byte[] prependArrayElement(byte[] array, byte element) {
        byte[] result = new byte[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static char[] prependArrayElement(char[] array, char element) {
        char[] result = new char[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static int[] prependArrayElement(int[] array, int element) {
        int[] result = new int[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static long[] prependArrayElement(long[] array, long element) {
        long[] result = new long[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static float[] prependArrayElement(float[] array, float element) {
        float[] result = new float[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static double[] prependArrayElement(double[] array, double element) {
        double[] result = new double[array.length + 1];
        result[0] = element;
        System.arraycopy(array, 0, result, 1, array.length);
        return result;
    }
    
    public static <E> E[] insertArrayElement(E[] array, int index, @CheckForNull E element) {
        E[] result = newArray(array, array.length + 1);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static boolean[] insertArrayElement(boolean[] array, int index, boolean element) {
        boolean[] result = new boolean[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static byte[] insertArrayElement(byte[] array, int index, byte element) {
        byte[] result = new byte[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static char[] insertArrayElement(char[] array, int index, char element) {
        char[] result = new char[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static int[] insertArrayElement(int[] array, int index, int element) {
        int[] result = new int[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static long[] insertArrayElement(long[] array, int index, long element) {
        long[] result = new long[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static float[] insertArrayElement(float[] array, int index, float element) {
        float[] result = new float[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static double[] insertArrayElement(double[] array, int index, double element) {
        double[] result = new double[array.length + 1];
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    public static <E> E[] removeArrayElement(E[] array, int index) {
        E[] result = newArray(array, array.length - 1);
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static boolean[] removeArrayElement(boolean[] array, int index) {
        boolean[] result = new boolean[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static byte[] removeArrayElement(byte[] array, int index) {
        byte[] result = new byte[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static char[] removeArrayElement(char[] array, int index) {
        char[] result = new char[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static int[] removeArrayElement(int[] array, int index) {
        int[] result = new int[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static long[] removeArrayElement(long[] array, int index) {
        long[] result = new long[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static float[] removeArrayElement(float[] array, int index) {
        float[] result = new float[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static double[] removeArrayElement(double[] array, int index) {
        double[] result = new double[array.length - 1];
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(array, index + 1, result, index, array.length - index - 1);
        return result;
    }
    
    public static <E> E[] replaceArrayElement(E[] array, int index, @CheckForNull E element) {
        E[] result = array.clone();
        result[index] = element;
        return result;
    }
    
    public static int[] replaceArrayElement(int[] array, int index, int element) {
        int[] result = array.clone();
        result[index] = element;
        return result;
    }
    
    public static void reverse(byte[] array) {
        int j = array.length - 1;
        int i = 0;
        while (i < j) {
            swap(array, i, j);
            i++;
            j--;
        }
    }
    
    public static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
    
    public static <E> void reverse(E[] array) {
        int j = array.length - 1;
        int i = 0;
        while (i < j) {
            swap(array, i, j);
            i++;
            j--;
        }
    }
    
    public static <E> void swap(E[] array, int i, int j) {
        E tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
    
    /**
     * Returns an empty object array casted to an array of the generic type <E>.
     * You can only use this when your code works with a fully generic type and 
     * therefore you are actually working with an Object array, e.g.:
     * <pre><code> 
     * class Foo<E> {
     *   private E[] elements = ArrayUtil.unsafeCastedEmptyArray();
     *   void add(E element) {
     *     elements = ArrayUtil.appendArrayElement(elements, element);
     *   }
     *   // ...
     * }
     * </code></pre>
     * 
     * <p>Assigning to a non-object array type fails:
     * <pre><code>
     * String[] empty = ArrayUtil.unsafeCastedEmptyArray() // throws ClassCastException
     * </code></pre>
     */
    public static <E> E[] unsafeCastedEmptyArray() {
        return unsafeCast(EMPTY_ARRAY);
    }
    
    /**
     * Returns a new object array of {@code length} casted to an array of the generic type <E>.
     * You can only use this when your code works with a fully generic type and therefore you 
     * are actually working with an Object array, e.g.:
     * <pre><code> 
     * class Foo<E> {
     *   private E[] elements = ArrayUtil.unsafeCastedNewArray(10);
     *   void store(E e, int i) {
     *     elements[i] = e; 
     *   }
     *   E receive(int i) {
     *     return elements[i];
     *   }
     *   // ...
     * }
     * </code></pre>
     * 
     * <p>Assigning to a non-object array type fails:
     * <pre><code>
     * String[] empty = ArrayUtil.unsafeCastedNewArray(3); // throws ClassCastException
     * </code></pre>
     */
    public static <E> E[] unsafeCastedNewArray(int length) {
        return unsafeCast(new Object[length]);
    }
    
    /**
     * Casts an object array to an array of type <E>. This is an unsafe operation, the
     * type of the array is not changed.
     * <p>
     * Assignment to a type array will generally fail:
     * <pre><code>
     * Object[] objects = new Object[10];
     * String[] strings = ArrayUtil.unsafeCast(objects); // throws ClassCastException
     * </code></pre>
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] unsafeCast(Object[] object) {
        return (E[])object;
    }
    
}
