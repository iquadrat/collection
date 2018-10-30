package bench.org.povworld.collection;

import java.util.Arrays;

import javax.annotation.CheckForNull;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.common.ArrayUtil;

public class ArrayInsert {
    
    private final String[] base;
    
    private final Object element;
    
    private final int arraySize;
    
    public ArrayInsert(
            @DivideBy @ForEachInt({0, 1, 3, 5, 10, 20, 30, 40}) int arraySize
//      @DivideBy @ForEachInt({100,1000,10000}) int arraySize
    ) {
        this.arraySize = arraySize;
        base = new String[arraySize];
        ElementProducer<String> sp = StringProducer.createDefaultElementProducer();
        for (int i = 0; i < arraySize; ++i) {
            base[i] = sp.produce();
        }
        element = sp.produce();
    }
    
    @Bench
    public Object insertArrayElement() {
        Object result = null;
        for (int i = 0; i < arraySize; ++i) {
            result = insertArrayElement2b(base, i, "foobar");
        }
        return result;
    }
    
    private static <E> E[] insertArrayElement1(E[] array, int index, @CheckForNull E element) {
        // TODO create empty instead and do two copies?
        E[] result = Arrays.copyOf(array, array.length + 1);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    private static <E> E[] insertArrayElement2(E[] array, int index, @CheckForNull E element) {
        E[] result = ArrayUtil.unsafeCastedNewArray(array.length + 1);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    private static <E> E[] insertArrayElement2b(E[] array, int index, @CheckForNull E element) {
        //E[] result = (E[])Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        E[] result = ArrayUtil.newArray(array, array.length + 1);
        System.arraycopy(array, 0, result, 0, index);
        result[index] = element;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    private static <E> E[] insertArrayElement3(E[] array, int index, @CheckForNull E element) {
        E[] result = ArrayUtil.unsafeCastedNewArray(array.length + 1);
        arraycopy(array, 0, result, 0, index);
        result[index] = element;
        arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }
    
    private static <E> void arraycopy(E[] src, int srcPos, E[] dst, int dstPos, int length) {
        for (int i = 0; i < length; ++i) {
            dst[dstPos + i] = src[srcPos + i];
        }
    }
    
}
