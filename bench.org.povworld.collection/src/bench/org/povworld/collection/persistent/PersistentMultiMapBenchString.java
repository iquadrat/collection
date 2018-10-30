package bench.org.povworld.collection.persistent;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.SkipBenchmarkException;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachBoolean;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.monitor.ConsoleProgressMonitor;

import bench.org.povworld.collection.StringProducer;

public class PersistentMultiMapBenchString extends PersistentMultiMapBench<String> {
    
    private static final int MAX_ELEMENTS = 10000000;
    
    public PersistentMultiMapBenchString(
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000, 100000}) int keyCount,
            //@DivideBy @ForEachInt({100000}) int keyCount,
            //@DivideBy @ForEachInt({1, 2, 5, 10, 1000}) int valueCount,
            @ForEachInt({2}) int valueCount,
            @ForEachBoolean({/*true, */false}) boolean variableValueCount) {
        super(checkArguments(keyCount, valueCount, variableValueCount), valueCount, variableValueCount, StringProducer.createDefaultKeyProducer(),
                StringProducer.createDefaultValueProducer());
    }
    
    private static int checkArguments(int keyCount, int valueCount, boolean variableValueCount) {
//    if (keyCount * valueCount < 32) {
//      skip();
//    }
        
        if (keyCount * valueCount > MAX_ELEMENTS) {
            skip();
        }
        if (keyCount == 0 && valueCount != 1) {
            skip();
        }
        if (valueCount <= 1 && variableValueCount) {
            skip();
        }
        return keyCount;
    }
    
    private static void skip() {
        throw new SkipBenchmarkException();
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(PersistentMultiMapBenchString.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}