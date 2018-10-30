package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.AssertionFailure;

public class AssertTest {
    
    private static final String SOME_FAILURE_MESSAGE = "some failure message";
    
    private Level level;
    
    @Before
    public void setUp() {
        // turn logging of for this test
        level = Assert.getLogger().getLevel();
        Assert.getLogger().setLevel(Level.OFF);
    }
    
    @After
    public void tearDown() {
        // restore original logging level
        Assert.getLogger().setLevel(level);
    }
    
    @Test
    public void failWithString() {
        try {
            Assert.fail(SOME_FAILURE_MESSAGE);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(SOME_FAILURE_MESSAGE, e.getMessage());
        }
    }
    
    @Test
    public void failWithMessage() {
        try {
            Assert.fail("test %d %s message", 15, "foo");
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("test 15 foo message", e.getMessage());
        }
    }
    
    @Test
    public void failWithIllegalFormat() {
        try {
            Assert.fail("test %d %s message", 15);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("test %d %s message[15]", e.getMessage());
        }
    }
    
    @Test
    public void failWithThrowable() {
        Throwable throwable = new Throwable(SOME_FAILURE_MESSAGE);
        try {
            Assert.fail(throwable);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(throwable, e.getCause());
        }
    }
    
    @Test
    public void equals() {
        Assert.assertEquals("foo", "foo");
        try {
            Assert.assertEquals("foo", "bar");
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            // pass
        }
    }
    
    @Test
    public void equalsWithMessage() {
        Assert.assertEquals("foo", "foo", SOME_FAILURE_MESSAGE);
        try {
            Assert.assertEquals("foo", "bar", SOME_FAILURE_MESSAGE);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(SOME_FAILURE_MESSAGE, e.getMessage());
        }
    }
    
    @Test
    public void instanceOf() {
        Assert.assertInstanceOf("foo", String.class);
        try {
            Assert.assertInstanceOf("foo", Integer.class);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("foo(java.lang.String) is not instance of java.lang.Integer", e.getMessage());
        }
        try {
            Assert.assertInstanceOf(null, Integer.class);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("null is not instance of java.lang.Integer", e.getMessage());
        }
    }
    
    @Test
    public void falseWithString() {
        Assert.assertFalse(1 == 2, SOME_FAILURE_MESSAGE);
        try {
            Assert.assertFalse(1 != 2, SOME_FAILURE_MESSAGE);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(SOME_FAILURE_MESSAGE, e.getMessage());
        }
    }
    
    @Test
    public void falseWithMessage() {
        Assert.assertFalse(1 == 2, "foo %d bar", 1);
        try {
            Assert.assertFalse(1 != 2, "foo %d bar", 2);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("foo 2 bar", e.getMessage());
        }
    }
    
    @Test
    public void trueWithString() {
        Assert.assertTrue(1 != 2, SOME_FAILURE_MESSAGE);
        try {
            Assert.assertTrue(1 == 2, SOME_FAILURE_MESSAGE);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(SOME_FAILURE_MESSAGE, e.getMessage());
        }
    }
    
    @Test
    public void trueWithMessage() {
        Assert.assertTrue(1 != 2, "foo %d bar", 1);
        try {
            Assert.assertTrue(1 == 2, "foo %d bar", 2);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals("foo 2 bar", e.getMessage());
        }
    }
    
    @Test
    public void nullWithString() {
        Assert.assertNull(null, SOME_FAILURE_MESSAGE);
        try {
            Assert.assertNull(new Object(), SOME_FAILURE_MESSAGE);
            failExpectedAssertionFailure();
        } catch (AssertionFailure e) {
            assertEquals(SOME_FAILURE_MESSAGE, e.getMessage());
        }
    }
    
    private void failExpectedAssertionFailure() {
        fail("expected " + AssertionFailure.class.getName());
    }
    
}
