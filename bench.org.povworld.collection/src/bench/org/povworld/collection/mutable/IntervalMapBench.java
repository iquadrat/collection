package bench.org.povworld.collection.mutable;

import java.util.Random;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachBoolean;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.annotations.MemoryBench;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.common.Interval;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.IntervalMap;

public class IntervalMapBench {
    
    private Random random = new Random();
    private IntervalMap<String> map;
    final int maxLength = 100;
    final int maxStart;
    
    private ArrayList<Interval> intervals;
    private ArrayList<String> values;
    private final Interval iterate;
    
    public IntervalMapBench(@DivideBy @ForEachInt({100, 1000, 10000}) int intervalCount, @ForEachBoolean({true, false}) boolean zeroStart) {
        intervals = new ArrayList<>(intervalCount);
        values = new ArrayList<>(intervalCount);
        map = create();
        maxStart = maxLength * intervalCount / 10;
        for (int i = 0; i < intervalCount; ++i) {
            int start = zeroStart ? 0 : random.nextInt(maxStart);
            int length = random.nextInt(maxLength) + 1;
            Interval interval = new Interval(start, start + length);
            intervals.add(interval);
            String value = String.valueOf(i);
            values.add(value);
            map.add(interval, value);
        }
        iterate = zeroStart ? new Interval(maxLength * 9 / 10, maxLength * 9 / 10 + 1) : new Interval(0, maxStart + maxLength);
    }
    
    @Bench
    @SuppressWarnings("unused")
    public int overlappersPoint() {
        int result = 0;
        for (int i = iterate.getStart(); i < iterate.getEnd(); i += maxLength) {
            for (String overlap: map.getOverlappers(i)) {
                result++;
            }
        }
        return result;
    }
    
    @Bench
    @SuppressWarnings("unused")
    public int overlappersInterval() {
        int result = 0;
        for (int i = iterate.getStart(); i < iterate.getEnd(); i += maxLength) {
            for (String overlap: map.getOverlappers(new Interval(i, i + 2 * maxLength))) {
                result++;
            }
        }
        return result;
    }
    
    @Bench
    public int iterate() {
        int result = 0;
        EntryIterator<Interval, String> iterator = map.entryIterator();
        while (iterator.next()) {
            iterator.getCurrentKey();
            iterator.getCurrentValue();
            result++;
        }
        return result;
    }
    
    @Bench
    public Object add() {
        IntervalMap<String> map = create();
        for (int i = 0; i < intervals.size(); ++i) {
            map.add(intervals.get(i), values.get(i));
        }
        return map;
    }
    
    @Bench
    public Object addRemove() {
        IntervalMap<String> map = create();
        for (int i = 0; i < intervals.size(); ++i) {
            map.add(intervals.get(i), values.get(i));
        }
        for (int i = 0; i < intervals.size(); ++i) {
            map.remove(intervals.get(i), values.get(i));
        }
        return map;
    }
    
    @MemoryBench
    public Object memory() {
        IntervalMap<String> map = create();
        for (int i = 0; i < intervals.size(); ++i) {
            map.add(intervals.get(i), values.get(i));
        }
        return map;
    }

    private IntervalMap<String> create() {
        return IntervalMap.create(String.class);
        //return new IntervalMap2<>();
    }
    
    public static void main(String[] args) {
        BenchmarkRunner runner = new BenchmarkRunner();
        runner.add(IntervalMapBench.class);
        runner.run(BenchmarkContext.create(new ConsoleProgressMonitor()));
    }
    
}
