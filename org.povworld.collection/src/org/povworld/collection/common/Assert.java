package org.povworld.collection.common;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

/**
 * Static class to assert conditions and invariants.
 * 
 * @see PreConditions
 */
public final class Assert {
    
    private Assert() {
        // utility class
    }
    
    private static final Logger logger = Logger.getLogger(Assert.class.getName());
    
    public static Logger getLogger() {
        return logger;
    }
    
    /**
     * Notifies that an assertion has failed.
     * 
     * <p>The return type is set to {@link RuntimeException} to allow writing {@code throw Assert.fail(message)}.
     * However, as the method itself always throws it will never return anything anyways.
     * 
     * @throws AssertionFailure is always thrown
     */
    public static RuntimeException fail(String message) {
        getLogger().log(Level.SEVERE, message);
        throw new AssertionFailure(message);
    }
    
    /**
     * Notifies that an assertion has failed. The message gets composed by the inserting the given arguments into the formatter string.
     * 
     * @param messageFormat Message formats
     * @param messageArguments format arguments
     * @see String#format(String, Object...)
     */
    public static RuntimeException fail(String messageFormat, Object... messageArguments) {
        String formattedMessage;
        try {
            formattedMessage = String.format(messageFormat, messageArguments);
        } catch (IllegalFormatException e) {
            // fall-back for incorrect formatting
            formattedMessage = messageFormat + Arrays.toString(messageArguments);
        }
        return fail(formattedMessage);
    }
    
    /**
     * Notifies that an assertion has failed: The given {@code throwable} occurred at an unexpected place.
     * 
     * @throws AssertionFailure is always thrown
     * @return The return type is set to {@link RuntimeException} to allow writing {@code throw Assert.fail(message)}.
     *         However, as the method itself always throws it will never return anything anyways.
     */
    public static RuntimeException fail(Throwable throwable) {
        getLogger().log(Level.SEVERE, "An unexpected exception occurred!", throwable);
        throw new AssertionFailure(throwable);
    }
    
    /**
     * Asserts that the given two objects are equal to each other.
     */
    public static void assertEquals(@CheckForNull Object object1, @CheckForNull Object object2) {
        if (!ObjectUtil.objectEquals(object1, object2)) {
            fail(object1 + " not equals to " + object2);
        }
    }
    
    /**
     * Asserts that the given two objects are equal to each other. If not, logs an assertion failure
     * with the given {@code message}.
     */
    public static void assertEquals(@CheckForNull Object object1, @CheckForNull Object object2, String message) {
        if (!ObjectUtil.objectEquals(object1, object2)) {
            fail(message);
        }
    }
    
    /**
     * Asserts that the given {@code object} is an instance of given {@code class_}.
     */
    public static void assertInstanceOf(@CheckForNull Object object, Class<?> class_) {
        if (class_.isInstance(object)) return;
        fail("%s is not instance of %s", (object == null) ? "null" : object + "(" + object.getClass().getName() + ")", class_.getName());
    }
    
    /**
     * Asserts that a condition does not hold. Otherwise, logs an assertion failure
     * with the given {@code message}.
     */
    public static void assertFalse(boolean condition, String message) {
        if (condition) fail(message);
    }
    
    public static void assertFalse(boolean condition, String messageFormat, Object... messageArguments) {
        if (condition) fail(messageFormat, messageArguments);
    }
    
    /**
     * Asserts that the {@code condition} holds. Otherwise, logs an assertion failure
     * with the given {@code message}.
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) fail(message);
    }
    
    public static void assertTrue(boolean condition, String messageFormat, Object... messageArguments) {
        if (!condition) fail(messageFormat, messageArguments);
    }
    
    /**
     * Asserts that the given object is the {@code null} reference.
     */
    public static void assertNull(@CheckForNull Object obj, String message) {
        if (obj != null) fail(message);
    }
    
}
