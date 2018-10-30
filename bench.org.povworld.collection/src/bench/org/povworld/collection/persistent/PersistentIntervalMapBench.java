package bench.org.povworld.collection.persistent;

import java.util.Random;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.DivideBy;
import org.jbenchx.annotations.ForEachInt;
import org.jbenchx.annotations.MemoryBench;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.List;
import org.povworld.collection.common.Interval;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentIntervalMap;

import bench.org.povworld.collection.ElementProducer;
import bench.org.povworld.collection.StringProducer;

/**
 * Benchmarks for {@link PersistentIntervalMap}.
 */
public class PersistentIntervalMapBench {
    
    private final Random random = new Random(1);
    
    private final ElementProducer<String> stringProducer = StringProducer.createDefaultElementProducer();
    
    private final PersistentIntervalMap<String> map;
    
    private final List<Interval> intervals;
    private final List<Interval> newIntervals;
    
    private final String nonElement;
    
    public PersistentIntervalMapBench(
            @DivideBy @ForEachInt({0, 1, 10, 100, 1000, 10000}) int intervalCount) {
        final int maxStart = intervalCount;
        final int maxLength = intervalCount / 10 + 1;
        
        this.nonElement = stringProducer.produce();
        PersistentIntervalMap<String> map = PersistentIntervalMap.empty();
        ArrayList<Interval> intervals = new ArrayList<>(intervalCount);
        ArrayList<Interval> newIntervals = new ArrayList<>(intervalCount);
        for (int i = 0; i < intervalCount; ++i) {
            Interval interval = randomInterval(maxStart, maxLength);
            intervals.push(interval);
            String element = stringProducer.produce();
            map = map.with(interval, element);
            
            newIntervals.push(randomInterval(maxStart, maxLength));
        }
        this.map = map;
        this.intervals = intervals;
        this.newIntervals = newIntervals;
    }
    
    private Interval randomInterval(int maxStart, int maxLength) {
        int start = random.nextInt(maxStart);
        Interval interval = new Interval(start, start + random.nextInt(maxLength) + 1);
        return interval;
    }
    
    @MemoryBench
    // @Ignore
    public Object memory() throws InterruptedException {
        PersistentIntervalMap<String> result = PersistentIntervalMap.empty();
        EntryIterator<Interval, String> iterator = map.entryIterator();
        while (iterator.next()) {
            result = result.with(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
        return result;
    }
    
    @Bench
    // @Ignore
    public Object getOverlappers() {
        for (Interval interval: intervals) {
            map.getOverlappers(interval);
        }
        return this;
    }
    
    @Bench
    // @Ignore
    public Object iterate() {
        EntryIterator<Interval, String> it = map.entryIterator();
        int count = 0;
        while (it.next()) {
            count++;
        }
        return count;
    }
    
    @Bench
    // @Ignore
    public Object putNewInterval() {
        for (Interval interval: newIntervals) {
            map.with(interval, nonElement);
        }
        return this;
    }
    
}
