package org.povworld.collection.common;

import javax.annotation.CheckForNull;

import org.povworld.collection.Collection;

public class PreConditions {
    
    private PreConditions() {}
    
    private static final boolean DYNAMIC_NP_CHECKS = true;
    
    public static boolean dynamicNullPointerChecksEnabled() {
        return DYNAMIC_NP_CHECKS;
    }
    
    /**
     * Used to check parameters given to the library. These checks are
     * superfluous if the client uses static null checking as all places where
     * null is allowed, the {@code @CheckForNull} annotation is used.
     *
     * @throws NullPointerException
     *             if dynamic null pointer checking is enabled and the given
     *             object is indeed null
     */
    public static <T> T paramNotNull(@CheckForNull T object) {
        if (DYNAMIC_NP_CHECKS && (object == null)) {
            throw new NullPointerException();
        }
        return object;
    }
    
    /**
     * @throws IllegalArgumentException
     *             if the given condition does not hold
     */
    public static void paramCheck(Object argument, String message, boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("Argument " + argument + " failed precondition: " + message);
        }
    }
    
    public static void paramNotEmpty(Collection<?> argument) {
        paramCheck(argument, "Should not be emtpy!", !argument.isEmpty());
    }
    
    /**
     * @throws IllegalStateException
     *             if the given condition does not hold
     */
    public static void conditionCheck(String message, boolean condition) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
    
    public static byte paramPositive(byte value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected positive value but got " + value);
        }
        return value;
    }
    
    public static short paramPositive(short value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected positive value but got " + value);
        }
        return value;
    }
    
    public static int paramPositive(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected positive value but got " + value);
        }
        return value;
    }
    
    public static long paramPositive(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected positive value but got " + value);
        }
        return value;
    }
    
}
