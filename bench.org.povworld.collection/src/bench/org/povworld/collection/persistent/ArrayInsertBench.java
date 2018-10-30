package bench.org.povworld.collection.persistent;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.common.ArrayUtil;

public class ArrayInsertBench {
    
    private final Object[] array;
    
    private final Object element;
    
    public ArrayInsertBench(@ForEachInt({1, 8, 16, 32, 64}) int size) {
        array = new Object[size];
        for (int i = 0; i < size; ++i) {
            array[i] = Integer.valueOf(1932 + i * 123213);
        }
        element = Integer.valueOf(42);
    }
    
    @Bench
    public Object insertBegin() {
        return ArrayUtil.insertArrayElement(array, 0, element);
    }
    
    @Bench
    public Object insertMiddle() {
        return ArrayUtil.insertArrayElement(array, array.length / 2, element);
    }
    
    @Bench
    public Object insertEnd() {
        return ArrayUtil.insertArrayElement(array, array.length - 1, element);
    }
    
}
//Initializing Benchmarking Framework...
//Running on Linux 3.11.4-pf-i7
//Max heap = 7484735488 System Benchmark = 0.88ns
//Performing 15 benchmarking tasks..
//[0] ArrayInsertBench(1).insertBegin********** 22.3ns
//[1] ArrayInsertBench(8).insertBegin********** 28.2ns
//[2] ArrayInsertBench(16).insertBegin**********  34.6ns
//[3] ArrayInsertBench(32).insertBegin**********  46.3ns
//[4] ArrayInsertBench(64).insertBegin**********  72.4ns
//[5] ArrayInsertBench(1).insertEnd********** 23.0ns
//[6] ArrayInsertBench(8).insertEnd*************  23.9ns
//[7] ArrayInsertBench(16).insertEnd**********  24.1ns
//[8] ArrayInsertBench(32).insertEnd**********  24.4ns
//[9] ArrayInsertBench(64).insertEnd**********  27.0ns
//[10]  ArrayInsertBench(1).insertMiddle.*********  22.9ns
//[11]  ArrayInsertBench(8).insertMiddle**********  25.5ns
//[12]  ArrayInsertBench(16).insertMiddle********** 29.0ns
//[13]  ArrayInsertBench(32).insertMiddle********** 35.6ns
//[14]  ArrayInsertBench(64).insertMiddle********** 49.4ns
//Success.

//Initializing Benchmarking Framework...
//Running on Linux 3.11.4-pf-i7
//Max heap = 7484735488 System Benchmark = 0.89ns
//Performing 15 benchmarking tasks..
//[0] ArrayInsertBench(1).insertBegin********** 19.6ns
//[1] ArrayInsertBench(8).insertBegin********** 21.3ns
//[2] ArrayInsertBench(16).insertBegin**********  21.6ns
//[3] ArrayInsertBench(32).insertBegin**********  23.6ns
//[4] ArrayInsertBench(64).insertBegin**********  29.1ns
//[5] ArrayInsertBench(1).insertEnd.********* 21.7ns
//[6] ArrayInsertBench(8).insertEnd********** 26.3ns
//[7] ArrayInsertBench(16).insertEnd**********  27.3ns
//[8] ArrayInsertBench(32).insertEnd**********  30.2ns
//[9] ArrayInsertBench(64).insertEnd**********  32.6ns
//[10]  ArrayInsertBench(1).insertMiddle*********** 21.8ns
//[11]  ArrayInsertBench(8).insertMiddle**********  26.8ns
//[12]  ArrayInsertBench(16).insertMiddle********** 27.6ns
//[13]  ArrayInsertBench(32).insertMiddle********** 30.1ns
//[14]  ArrayInsertBench(64).insertMiddle********** 40.0ns
//Success.
