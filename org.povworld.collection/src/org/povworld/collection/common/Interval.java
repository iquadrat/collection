package org.povworld.collection.common;

import javax.annotation.CheckForNull;

// TODO make generic
public class Interval {
    private final int start;
    private final int end;
    
    public Interval(int start, int end) {
        PreConditions.conditionCheck("start > end", start <= end);
        this.start = start;
        this.end = end;
    }
    
    public boolean contains(int point) {
        return (start <= point) && (point < end);
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public int length() {
        return end - start;
    }
    
    public boolean overlaps(Interval interval) {
        return (start < interval.end) && (end > interval.start);
    }
    
    @CheckForNull
    public static Interval intersect(@CheckForNull Interval i1, @CheckForNull Interval i2) {
        if (i1 == null || i2 == null) {
            return null;
        }
        int newStart = Math.max(i1.start, i2.start);
        int newEnd = Math.min(i1.end, i2.end);
        if (newStart > newEnd) {
            return null;
        }
        return new Interval(newStart, newEnd);
    }
    
    @Override
    public int hashCode() {
        return start + 31 * end;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Interval)) {
            return false;
        }
        Interval other = (Interval)obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "[" + start + "," + end + ")";
    }
}
