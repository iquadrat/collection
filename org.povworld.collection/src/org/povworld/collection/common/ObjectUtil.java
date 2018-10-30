package org.povworld.collection.common;

import javax.annotation.CheckForNull;

public final class ObjectUtil {
    
    private ObjectUtil() {}
    
    /**
     * Checks two object references for equality. Two references are equal if
     * both point to {@code null} or if they refer to equal objects.
     *
     * @return true if {@code obj1==obj2} or
     *         {@code (obj1!=null) && ob1.equals(obj2)}
     */
    public static boolean objectEquals(@CheckForNull Object obj1, @CheckForNull Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }
    
    public static int hashCode(@CheckForNull Object object) {
        if (object == null) {
            return 0;
        }
        return object.hashCode();
    }
    
    /**
     * Verifies that the given element is not {@code null}.
     *
     * @param <T>
     *            the object type
     * @param element
     *            the element to test
     * @return the element if not {@code null} otherwise an assertion is thrown
     */
    public static <T> T checkNotNull(@CheckForNull T element) {
        if (element == null) {
            throw new NullPointerException();
        }
        return element;
    }
    
    @CheckForNull
    public static <T> T castOrNull(@CheckForNull Object object, Class<T> clazz) {
        if (!clazz.isInstance(object)) {
            return null;
        }
        return clazz.cast(object);
    }
    
    /**
     * Shuffles the bits in the given value to entangle them with each other.
     * This distributes the entropy of individual bits better in case of hash
     * functions that tend to only vary certain bits. Using the resulting value
     * instead of the plain hash code can reduce the collisions in hash tables
     * that use only a subset of the hash bits to determine the position in the
     * table.
     *
     * @return a hash value based on the given input
     */
    public static int strengthenedHashcode(int hashcode) {
        // TODO verify that this is a permutation
        int value = hashcode;
        value += ~(value << 9);
        value ^= (value >>> 14);
        value += (value << 4);
        value ^= (value >>> 10);
        return value;
    }
    
}
