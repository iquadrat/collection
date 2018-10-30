package org.povworld.collection.common;

/**
 * An instance of this class is thrown if an assertion failed.
 */
public class AssertionFailure extends Error {
    
    private static final long serialVersionUID = 1L;
    
    public AssertionFailure() {}
    
    public AssertionFailure(Throwable cause) {
        super(cause);
    }
    
    public AssertionFailure(String message) {
        super(message);
    }
    
}
