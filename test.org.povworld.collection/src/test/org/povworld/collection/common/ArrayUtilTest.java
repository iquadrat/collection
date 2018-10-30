package test.org.povworld.collection.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.povworld.collection.common.ArrayUtil;

public class ArrayUtilTest {
    
    @Test
    public void arrayOf() {
        String[] empty = ArrayUtil.arrayOf();
        assertEquals(0, empty.length);
        assertEquals(String.class, empty.getClass().getComponentType());
        
        Integer[] one = ArrayUtil.arrayOf(1);
        assertEquals(1, one.length);
        assertEquals(Integer.valueOf(1), one[0]);
        assertEquals(Integer.class, one.getClass().getComponentType());
        
        Object[] two = ArrayUtil.<Object>arrayOf("2", 2);
        assertEquals(2, two.length);
        assertEquals(String.valueOf(2), two[0]);
        assertEquals(Integer.valueOf(2), two[1]);
        assertEquals(Object.class, two.getClass().getComponentType());
    }
    
    @Test
    public void emptyObjectArrayCasted() {
        Object[] empty = ArrayUtil.unsafeCastedEmptyArray();
        assertEquals(0, empty.length);
        assertEquals(Object.class, empty.getClass().getComponentType());
    }
    
    @Test(expected = ClassCastException.class)
    public void emptyObjectArrayCasted_throwsOnAssignmentToNonObjectArray() {
        @SuppressWarnings("unused")
        String[] empty = ArrayUtil.unsafeCastedEmptyArray();
    }
    
    @Test
    public void newObjectArrayCasted() {
        assertArrayEquals(new Object[0], ArrayUtil.unsafeCastedNewArray(0));
        assertArrayEquals(new Object[1], ArrayUtil.unsafeCastedNewArray(1));
        assertArrayEquals(new Object[14], ArrayUtil.unsafeCastedNewArray(14));
    }
    
    @Test(expected = ClassCastException.class)
    public void newObjectArrayCasted_throwsOnAssignmentToNonObjectArray() {
        @SuppressWarnings("unused")
        String[] empty = ArrayUtil.unsafeCastedNewArray(13);
    }
    
    @Test
    public void newArrayWithType() {
        assertArrayEquals(new Integer[0], ArrayUtil.newArray(Integer.class, 0));
        assertArrayEquals(new String[1], ArrayUtil.newArray(String.class, 1));
        assertArrayEquals(new Object[14], ArrayUtil.newArray(Object.class, 14));
    }
    
    @Test(expected = ClassCastException.class)
    public void unsafeCast_throwsOnAssignmentToNonObjectArray() {
        Object[] objects = new Object[10];
        @SuppressWarnings("unused")
        String[] strings = ArrayUtil.unsafeCast(objects);
    }
    
    @Test
    public void prependArrayElement() {
        String[] array = new String[0];
        array = ArrayUtil.prependArrayElement(array, "one");
        assertArrayEquals(new String[] {"one"}, array);
        array = ArrayUtil.prependArrayElement(array, "two");
        assertArrayEquals(new String[] {"two", "one"}, array);
        array = ArrayUtil.prependArrayElement(array, "three");
        assertArrayEquals(new String[] {"three", "two", "one"}, array);
    }
    
    @Test
    public void prependArrayElement_primitives() {
        assertTrue(Arrays.equals(new boolean[] {true, false, true}, ArrayUtil.prependArrayElement(new boolean[] {false, true}, true)));
        assertTrue(Arrays.equals(new byte[] {4, 2, 3}, ArrayUtil.prependArrayElement(new byte[] {2, 3}, (byte)4)));
        assertTrue(Arrays.equals(new char[] {'c', 'a', 'b'}, ArrayUtil.prependArrayElement(new char[] {'a', 'b'}, 'c')));
        assertTrue(Arrays.equals(new int[] {4, 2, 3}, ArrayUtil.prependArrayElement(new int[] {2, 3}, 4)));
        assertTrue(Arrays.equals(new long[] {4, 2, 3}, ArrayUtil.prependArrayElement(new long[] {2, 3}, 4)));
    }
    
    @Test
    public void appendArrayElement() {
        String[] array = new String[0];
        array = ArrayUtil.appendArrayElement(array, "one");
        assertArrayEquals(new String[] {"one"}, array);
        array = ArrayUtil.appendArrayElement(array, "two");
        assertArrayEquals(new String[] {"one", "two"}, array);
        array = ArrayUtil.appendArrayElement(array, "three");
        assertArrayEquals(new String[] {"one", "two", "three"}, array);
    }
    
    @Test
    public void appendArrayElement_primitives() {
        assertTrue(Arrays.equals(new boolean[] {false, true, true}, ArrayUtil.appendArrayElement(new boolean[] {false, true}, true)));
        assertTrue(Arrays.equals(new byte[] {2, 3, 4}, ArrayUtil.appendArrayElement(new byte[] {2, 3}, (byte)4)));
        assertTrue(Arrays.equals(new char[] {'a', 'b', 'c'}, ArrayUtil.appendArrayElement(new char[] {'a', 'b'}, 'c')));
        assertTrue(Arrays.equals(new int[] {2, 3, 4}, ArrayUtil.appendArrayElement(new int[] {2, 3}, 4)));
        assertTrue(Arrays.equals(new long[] {2, 3, 4}, ArrayUtil.appendArrayElement(new long[] {2, 3}, 4)));
    }
    
    @Test
    public void insertArrayElement() {
        String[] array = new String[] {"x", "y"};
        array = ArrayUtil.insertArrayElement(array, 1, "one");
        assertArrayEquals(new String[] {"x", "one", "y"}, array);
        array = ArrayUtil.insertArrayElement(array, 0, "two");
        assertArrayEquals(new String[] {"two", "x", "one", "y"}, array);
        array = ArrayUtil.insertArrayElement(array, 4, "three");
        assertArrayEquals(new String[] {"two", "x", "one", "y", "three"}, array);
    }
    
    @Test
    public void insertArrayElement_primitives() {
        assertTrue(Arrays.equals(new boolean[] {false, true, true}, ArrayUtil.insertArrayElement(new boolean[] {false, true}, 1, true)));
        assertTrue(Arrays.equals(new byte[] {2, 4, 3}, ArrayUtil.insertArrayElement(new byte[] {2, 3}, 1, (byte)4)));
        assertTrue(Arrays.equals(new char[] {'a', 'c', 'b'}, ArrayUtil.insertArrayElement(new char[] {'a', 'b'}, 1, 'c')));
        assertTrue(Arrays.equals(new int[] {2, 4, 3}, ArrayUtil.insertArrayElement(new int[] {2, 3}, 1, 4)));
        assertTrue(Arrays.equals(new long[] {2, 4, 3}, ArrayUtil.insertArrayElement(new long[] {2, 3}, 1, 4)));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void insertArrayElementOutOfBounds() {
        String[] array = new String[] {"x", "y"};
        ArrayUtil.insertArrayElement(array, 3, "one");
    }
    
    @Test
    public void removeArrayElement() {
        Integer[] array = new Integer[] {1, 2, 3, 4, 5};
        array = ArrayUtil.removeArrayElement(array, 2);
        assertArrayEquals(new Integer[] {1, 2, 4, 5}, array);
        array = ArrayUtil.removeArrayElement(array, 0);
        assertArrayEquals(new Integer[] {2, 4, 5}, array);
        array = ArrayUtil.removeArrayElement(array, 2);
        assertArrayEquals(new Integer[] {2, 4}, array);
    }
    
    @Test
    public void removeArrayElement_primitives() {
        assertTrue(Arrays.equals(new boolean[] {true}, ArrayUtil.removeArrayElement(new boolean[] {false, true}, 0)));
        assertTrue(Arrays.equals(new byte[] {3}, ArrayUtil.removeArrayElement(new byte[] {2, 3}, 0)));
        assertTrue(Arrays.equals(new char[] {'b'}, ArrayUtil.removeArrayElement(new char[] {'a', 'b'}, 0)));
        assertTrue(Arrays.equals(new int[] {3}, ArrayUtil.removeArrayElement(new int[] {2, 3}, 0)));
        assertTrue(Arrays.equals(new long[] {3}, ArrayUtil.removeArrayElement(new long[] {2, 3}, 0)));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void removeArrayElementOutOfBounds() {
        String[] array = new String[] {"x", "y"};
        ArrayUtil.insertArrayElement(array, 3, "one");
    }
    
    @Test
    public void replaceArrayElement() {
        Integer[] array = new Integer[] {1, 2, 3, 4, 5};
        array = ArrayUtil.replaceArrayElement(array, 2, -3);
        assertArrayEquals(new Integer[] {1, 2, -3, 4, 5}, array);
        array = ArrayUtil.replaceArrayElement(array, 0, 11);
        assertArrayEquals(new Integer[] {11, 2, -3, 4, 5}, array);
    }
    
    @Test
    public void reverseArrayElement() {
        Integer[] array = new Integer[0];
        ArrayUtil.reverse(array);
        assertArrayEquals(new Integer[] {}, array);
        
        array = new Integer[] {11};
        ArrayUtil.reverse(array);
        assertArrayEquals(new Integer[] {11}, array);
        
        array = new Integer[] {4, 3, 2, 1};
        ArrayUtil.reverse(array);
        assertArrayEquals(new Integer[] {1, 2, 3, 4}, array);
    }
    
    @Test
    public void swap() {
        Integer[] array = new Integer[] {1, 2, 3, 4, 5};
        ArrayUtil.swap(array, 2, 4);
        assertArrayEquals(new Integer[] {1, 2, 5, 4, 3}, array);
    }
    
    public static void assertArrayEquals(Object[] expected, Object[] actual) {
        // Also test the type, not only the elements.
        assertEquals(expected.getClass(), actual.getClass());
        Assert.assertArrayEquals(expected, actual);
    }
}
