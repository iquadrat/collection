package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Random;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Map;
import org.povworld.collection.common.Interval;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashBag;
import org.povworld.collection.mutable.HashMap;
import org.povworld.collection.mutable.IntervalMap;

import com.google.common.truth.Truth;

public class IntervalMapTest {
    
    private IntervalMap<String> map = IntervalMap.create(String.class);
    
    @Test
    public void emptyMap() {
        assertEquals(0, map.size());
        assertTrue(CollectionUtil.isEmpty(map.getOverlappers(42)));
        assertTrue(CollectionUtil.isEmpty(map.getOverlappers(new Interval(-42, 42))));
        assertFalse(map.entryIterator().next());
    }
    
    private void assertNextEntryEquals(EntryIterator<Interval, String> it, Interval interval, String string) {
        assertTrue(it.next());
        assertEquals(interval, it.getCurrentKey());
        assertEquals(string, it.getCurrentValue());
    }
    
    @Test
    public void singleInterval() {
        assertTrue(map.add(new Interval(10, 20), "hello"));
        
        assertEquals(1, map.size());
        Truth.assertThat(map.getOverlappers(7)).isEmpty();
        Truth.assertThat(map.getOverlappers(27)).isEmpty();
        Truth.assertThat(map.getOverlappers(20)).isEmpty();
        Truth.assertThat(map.getOverlappers(15)).containsExactly("hello");
        Truth.assertThat(map.getOverlappers(10)).containsExactly("hello");
        
        Truth.assertThat(map.getOverlappers(new Interval(0, 10))).isEmpty();
        Truth.assertThat(map.getOverlappers(new Interval(20, 30))).isEmpty();
        Truth.assertThat(map.getOverlappers(new Interval(4, 15))).containsExactly("hello");
        Truth.assertThat(map.getOverlappers(new Interval(14, 15))).containsExactly("hello");
        Truth.assertThat(map.getOverlappers(new Interval(17, 25))).containsExactly("hello");
        
        EntryIterator<Interval, String> it = map.entryIterator();
        assertNextEntryEquals(it, new Interval(10, 20), "hello");
        assertFalse(it.next());
    }
    
    @Test
    public void overlappersOfDisjointIntervals() {
        assertTrue(map.add(new Interval(10, 20), "10_20"));
        assertTrue(map.add(new Interval(30, 40), "30_40"));
        assertTrue(map.add(new Interval(50, 60), "50_60"));
        assertTrue(map.add(new Interval(60, 70), "60_70"));
        
        assertEquals(4, map.size());
        Truth.assertThat(map.getOverlappers(22)).isEmpty();
        Truth.assertThat(map.getOverlappers(10)).containsExactly("10_20");
        Truth.assertThat(map.getOverlappers(33)).containsExactly("30_40");
        Truth.assertThat(map.getOverlappers(59)).containsExactly("50_60");
        Truth.assertThat(map.getOverlappers(66)).containsExactly("60_70");
        
        Truth.assertThat(map.getOverlappers(new Interval(5, 25))).containsExactly("10_20");
        Truth.assertThat(map.getOverlappers(new Interval(15, 55))).containsExactly("10_20", "30_40", "50_60");
        Truth.assertThat(map.getOverlappers(new Interval(50, 66))).containsExactly("50_60", "60_70");
        
        EntryIterator<Interval, String> it = map.entryIterator();
        assertNextEntryEquals(it, new Interval(10, 20), "10_20");
        assertNextEntryEquals(it, new Interval(30, 40), "30_40");
        assertNextEntryEquals(it, new Interval(50, 60), "50_60");
        assertNextEntryEquals(it, new Interval(60, 70), "60_70");
        assertFalse(it.next());
    }
    
    @Test
    public void overlappersOfContinuousIntervals() {
        assertTrue(map.add(new Interval(10, 20), "10_20"));
        assertTrue(map.add(new Interval(20, 30), "20_30"));
        assertTrue(map.add(new Interval(30, 40), "30_40"));
        assertTrue(map.add(new Interval(40, 50), "40_50"));
        
        assertEquals(4, map.size());
        Truth.assertThat(map.getOverlappers(15)).containsExactly("10_20");
        Truth.assertThat(map.getOverlappers(20)).containsExactly("20_30");
        Truth.assertThat(map.getOverlappers(22)).containsExactly("20_30");
        Truth.assertThat(map.getOverlappers(33)).containsExactly("30_40");
        Truth.assertThat(map.getOverlappers(40)).containsExactly("40_50");
        Truth.assertThat(map.getOverlappers(50)).isEmpty();
        Truth.assertThat(map.getOverlappers(51)).isEmpty();
        
        Truth.assertThat(map.getOverlappers(new Interval(5, 25))).containsExactly("10_20", "20_30");
        Truth.assertThat(map.getOverlappers(new Interval(15, 55))).containsExactly("10_20", "20_30", "30_40", "40_50");
        Truth.assertThat(map.getOverlappers(new Interval(40, 66))).containsExactly("40_50");
        
        EntryIterator<Interval, String> it = map.entryIterator();
        assertNextEntryEquals(it, new Interval(10, 20), "10_20");
        assertNextEntryEquals(it, new Interval(20, 30), "20_30");
        assertNextEntryEquals(it, new Interval(30, 40), "30_40");
        assertNextEntryEquals(it, new Interval(40, 50), "40_50");
        assertFalse(it.next());
    }
    
    @Test
    public void overlappersOfNestedIntervals() {
        assertTrue(map.add(new Interval(10, 60), "10_60"));
        assertTrue(map.add(new Interval(20, 50), "20_50"));
        assertTrue(map.add(new Interval(30, 40), "30_40"));
        
        assertEquals(3, map.size());
        Truth.assertThat(map.getOverlappers(5)).isEmpty();
        Truth.assertThat(map.getOverlappers(10)).containsExactly("10_60");
        Truth.assertThat(map.getOverlappers(20)).containsExactly("10_60", "20_50");
        Truth.assertThat(map.getOverlappers(33)).containsExactly("10_60", "20_50", "30_40");
        Truth.assertThat(map.getOverlappers(40)).containsExactly("10_60", "20_50");
        Truth.assertThat(map.getOverlappers(55)).containsExactly("10_60");
        
        Truth.assertThat(map.getOverlappers(new Interval(5, 25))).containsExactly("10_60", "20_50");
        Truth.assertThat(map.getOverlappers(new Interval(15, 55))).containsExactly("10_60", "20_50", "30_40");
        Truth.assertThat(map.getOverlappers(new Interval(40, 66))).containsExactly("20_50", "10_60");
        
        EntryIterator<Interval, String> it = map.entryIterator();
        assertNextEntryEquals(it, new Interval(10, 60), "10_60");
        assertNextEntryEquals(it, new Interval(20, 50), "20_50");
        assertNextEntryEquals(it, new Interval(30, 40), "30_40");
        assertFalse(it.next());
    }
    
    private Map<Interval, String> collectNextEntries(EntryIterator<Interval, String> it, int n) {
        HashMap<Interval, String> entries = new HashMap<>();
        for (int i = 0; i < n; ++i) {
            assertTrue(it.next());
            entries.put(it.getCurrentKey(), it.getCurrentValue());
        }
        return entries;
    }
    
    @Test
    public void overlappersOfIntervalsWithSameBoundaries() {
        assertTrue(map.add(new Interval(10, 30), "10_30"));
        assertTrue(map.add(new Interval(10, 50), "10_50"));
        assertTrue(map.add(new Interval(30, 50), "30_50"));
        assertTrue(map.add(new Interval(20, 40), "20_40"));
        assertTrue(map.add(new Interval(10, 40), "10_40"));
        assertTrue(map.add(new Interval(30, 50), "duplicate"));
        
        assertEquals(6, map.size());
        Truth.assertThat(map.getOverlappers(33)).containsExactly("10_50", "30_50", "10_40", "20_40", "duplicate");
        Truth.assertThat(map.getOverlappers(40)).containsExactly("10_50", "30_50", "duplicate");
        Truth.assertThat(map.getOverlappers(10)).containsExactly("10_50", "10_30", "10_40");
        Truth.assertThat(map.getOverlappers(50)).isEmpty();
        
        Truth.assertThat(map.getOverlappers(new Interval(5, 25))).containsExactly("10_30", "10_50", "10_40", "20_40");
        Truth.assertThat(map.getOverlappers(new Interval(35, 55))).containsExactly("10_50", "30_50", "20_40", "10_40", "duplicate");
        Truth.assertThat(map.getOverlappers(new Interval(40, 66))).containsExactly("10_50", "30_50", "duplicate");
        
        EntryIterator<Interval, String> it = map.entryIterator();
        
        // There is no ordering guarantee amongst those entries with same interval
        // start.
        Map<Interval, String> entries10 = collectNextEntries(it, 3);
        Truth.assertThat(entries10.keys())
                .containsExactly(new Interval(10, 30), new Interval(10, 40), new Interval(10, 50));
        assertEquals("10_30", entries10.get(new Interval(10, 30)));
        assertEquals("10_40", entries10.get(new Interval(10, 40)));
        assertEquals("10_50", entries10.get(new Interval(10, 50)));
        
        assertNextEntryEquals(it, new Interval(20, 40), "20_40");
        
        HashSet<String> values30 = new HashSet<>();
        assertTrue(it.next());
        assertEquals(new Interval(30, 50), it.getCurrentKey());
        values30.add(it.getCurrentValue());
        assertTrue(it.next());
        assertEquals(new Interval(30, 50), it.getCurrentKey());
        values30.add(it.getCurrentValue());
        Truth.assertThat(values30).containsExactly("duplicate", "30_50");
        
        assertFalse(it.next());
    }
    
    @Test
    public void triggerLeftRotate() {
        assertTrue(map.add(new Interval(110, 111), "11"));
        assertTrue(map.add(new Interval(60, 66), "6"));
        assertTrue(map.add(new Interval(140, 141), "14"));
        assertTrue(map.add(new Interval(20, 22), "2"));
        assertTrue(map.add(new Interval(80, 88), "8"));
        assertTrue(map.add(new Interval(120, 122), "12"));
        assertTrue(map.add(new Interval(160, 166), "16"));
        assertTrue(map.add(new Interval(10, 11), "1"));
        assertTrue(map.add(new Interval(40, 44), "4"));
        assertTrue(map.add(new Interval(70, 85), "critical"));
        assertTrue(map.add(new Interval(90, 99), "9"));
        assertTrue(map.add(new Interval(130, 133), "13"));
        assertTrue(map.add(new Interval(150, 151), "15"));
        assertTrue(map.add(new Interval(170, 177), "17"));
        assertTrue(map.add(new Interval(30, 33), "3"));
        assertTrue(map.add(new Interval(50, 55), "5"));
        assertTrue(map.add(new Interval(100, 101), "10"));
        assertEquals(17, map.size());
        
        assertTrue(map.remove(new Interval(60, 66), "6"));
    }
    
    @Test
    public void overlappersOfLargeCollection() {
        Random random = new Random(55);
        
        final int startRange = 100;
        final int maxLength = 100;
        final int n = 1000;
        
        ArrayList<Interval> intervals = generateRandomIntervals(random, startRange, maxLength, n);
        
        for (int i = 0; i < intervals.size(); ++i) {
            assertTrue(map.add(intervals.get(i), String.valueOf(i)));
        }
        
        for (int point = 0; point < startRange; point += 10) {
            HashBag<String> expected = new HashBag<>();
            for (int i = 0; i < intervals.size(); ++i) {
                if (intervals.get(i).contains(point)) {
                    expected.add(String.valueOf(i));
                }
            }
            
            Iterable<String> actual = map.getOverlappers(point);
            Truth.assertThat(actual).containsExactlyElementsIn(expected);
        }
    }
    
    @Test
    public void overlappersOfLargeCollectionInterval() {
        Random random = new Random(55);
        
        final int startRange = 100;
        final int maxLength = 100;
        final int n = 10000;
        
        ArrayList<Interval> intervals = generateRandomIntervals(random, startRange, maxLength, n);
        
        for (int i = 0; i < intervals.size(); ++i) {
            assertTrue(map.add(intervals.get(i), String.valueOf(i)));
        }
        
        for (int point = 0; point < startRange; point += 10) {
            Interval test = new Interval(point, point + 17);
            HashBag<String> expected = new HashBag<>();
            for (int i = 0; i < intervals.size(); ++i) {
                if (test.overlaps(intervals.get(i))) {
                    expected.add(String.valueOf(i));
                }
            }
            
            Iterable<String> actual = map.getOverlappers(test);
            Truth.assertThat(actual).containsExactlyElementsIn(expected);
        }
    }
    
    private ArrayList<Interval> generateRandomIntervals(Random random, final int startRange, final int maxLength,
            final int n) {
        ArrayList<Interval> intervals = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            int start = random.nextInt(startRange);
            int length = random.nextInt(maxLength) + 1;
            intervals.push(new Interval(start, start + length));
        }
        return intervals;
    }
    
    @Test
    public void removeSingleElement() {
        assertTrue(map.add(new Interval(10, 20), "hello"));
        
        assertFalse(map.remove(new Interval(10, 20), "hi!"));
        assertFalse(map.remove(new Interval(10, 21), "hello"));
        assertTrue(map.remove(new Interval(10, 20), "hello"));
        assertEquals(0, map.size());
        Truth.assertThat(map.getOverlappers(15)).isEmpty();
    }
    
    @Test
    public void removeFromDisjunct() {
        assertTrue(map.add(new Interval(10, 20), "x"));
        assertTrue(map.add(new Interval(30, 40), "y"));
        assertTrue(map.add(new Interval(50, 60), "z"));
        
        assertTrue(map.remove(new Interval(30, 40), "y"));
        Truth.assertThat(map.getOverlappers(14)).containsExactly("x");
        Truth.assertThat(map.getOverlappers(24)).isEmpty();
        
        assertTrue(map.remove(new Interval(10, 20), "x"));
        Truth.assertThat(map.getOverlappers(14)).isEmpty();
        Truth.assertThat(map.getOverlappers(55)).containsExactly("z");
    }
    
    @Test
    public void removeFromNested() {
        assertTrue(map.add(new Interval(10, 60), "10_60"));
        assertTrue(map.add(new Interval(20, 50), "20_50"));
        assertTrue(map.add(new Interval(30, 40), "30_40"));
        
        assertTrue(map.remove(new Interval(10, 60), "10_60"));
        assertTrue(map.remove(new Interval(30, 40), "30_40"));
        
        assertFalse(map.remove(new Interval(22, 33), "ff"));
        
        assertEquals(1, map.size());
        Truth.assertThat(map.getOverlappers(15)).isEmpty();
        Truth.assertThat(map.getOverlappers(20)).containsExactly("20_50");
    }
    
    @Test
    public void removeLargeCollection() {
        Random random = new Random(22);
        
        final int startRange = 100;
        final int maxLength = 100;
        final int n = 10000;
        
        ArrayList<Interval> intervals1 = generateRandomIntervals(random, startRange, maxLength, n);
        ArrayList<Interval> intervals2 = generateRandomIntervals(random, startRange, maxLength, n);
        
        for (int i = 0; i < intervals1.size(); ++i) {
            map.add(intervals1.get(i), "remove-me");
        }
        for (int i = 0; i < intervals2.size(); ++i) {
            assertTrue(map.add(intervals2.get(i), String.valueOf(i)));
        }
        for (int i = 0; i < intervals1.size(); ++i) {
            map.remove(intervals1.get(i), "remove-me");
        }
        
        for (int point = 0; point < startRange; point += 10) {
            HashBag<String> expected = new HashBag<>();
            for (int i = 0; i < intervals2.size(); ++i) {
                if (intervals2.get(i).contains(point)) {
                    expected.add(String.valueOf(i));
                }
            }
            
            Iterable<String> actual = map.getOverlappers(point);
            Truth.assertThat(actual).containsExactlyElementsIn(expected);
        }
    }
    
}
