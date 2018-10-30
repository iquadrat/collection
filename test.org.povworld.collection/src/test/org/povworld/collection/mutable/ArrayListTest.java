package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.povworld.collection.mutable.ArrayList;

import test.org.povworld.collection.AbstractListTest;

/**
 * Unit tests for {@link ArrayList}.
 * 
 * @see ArrayListMutationTest
 */
public class ArrayListTest extends AbstractListTest<ArrayList<String>> {
    
    public ArrayListTest() {
        super(ArrayList.<String>newBuilder());
    }
    
    @Test
    public void pushPeekPop() {
        ArrayList<String> stack = new ArrayList<String>(2);
        
        assertNull(stack.pop());
        assertNull(stack.peek());
        
        stack.push("one");
        assertEquals("one", stack.peek());
        
        stack.push("two");
        assertEquals("two", stack.peek());
        
        assertEquals("two", stack.pop());
        stack.push("three");
        stack.push("four");
        
        assertEquals("four", stack.pop());
        assertEquals("three", stack.pop());
        assertEquals("one", stack.pop());
    }
}
