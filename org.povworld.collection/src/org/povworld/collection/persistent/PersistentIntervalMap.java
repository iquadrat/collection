package org.povworld.collection.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.common.Interval;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.tree.AbstractAvlTreeBuilder;
import org.povworld.collection.tree.AbstractImmutableAvlTreeNode;
import org.povworld.collection.tree.AvlTreeNode;
import org.povworld.collection.tree.Path;
import org.povworld.collection.tree.TreeUtil;

// TODO implement some map interface
@Immutable
public class PersistentIntervalMap<V> {
    
    private static final class Entry<V> {
        private final Interval interval;
        private final V value;
        
        Entry(Interval interval, V value) {
            this.interval = interval;
            this.value = value;
        }
        
        @Override
        public String toString() {
            return interval + "=" + value;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + interval.hashCode();
            result = prime * result + value.hashCode();
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Entry))
                return false;
            Entry<?> other = (Entry<?>)obj;
            if (interval == null) {
                if (other.interval != null)
                    return false;
            } else if (!interval.equals(other.interval))
                return false;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }
    
    @Immutable
    private static final class IntervalNode<V> extends AbstractImmutableAvlTreeNode<IntervalNode<V>> {
        
        final int start;
        final int max;
        final PersistentSet<Entry<V>> entries;
        
        IntervalNode(Interval interval, V value) {
            this(interval.getStart(), interval.getEnd(),
                    PersistentHashSet.<Entry<V>>empty().with(new Entry<>(interval, value)), null, null);
        }
        
        IntervalNode(int start, int max, PersistentSet<Entry<V>> entries, IntervalNode<V> left, IntervalNode<V> right) {
            super(left, right);
            this.start = start;
            this.max = max;
            this.entries = entries;
        }
        
        public IntervalNode<V> with(Interval interval, V value) {
            PersistentSet<Entry<V>> newEntries = entries.with(new Entry<>(interval, value));
            if (entries == newEntries) {
                return this;
            }
            return new IntervalNode<V>(start, Math.max(interval.getEnd(), max), newEntries, left, right);
        }
        
        @CheckForNull
        public IntervalNode<V> without(Interval interval, V value) {
            PersistentSet<Entry<V>> newEntries = entries.without(new Entry<>(interval, value));
            if (newEntries == entries) {
                return this;
            }
            if (newEntries.isEmpty()) {
                return null;
            }
            int newMax = Math.max(getMaxEnd(newEntries), getMax(left, right));
            return new IntervalNode<V>(start, newMax, newEntries, left, right);
        }
    }
    
    private static <V> int getMaxEnd(Iterable<Entry<V>> entries) {
        int max = Integer.MIN_VALUE;
        for (Entry<V> entry: entries) {
            max = Math.max(max, entry.interval.getEnd());
        }
        return max;
    }
    
    private static int getMax(@CheckForNull IntervalNode<?> left, @CheckForNull IntervalNode<?> right) {
        int leftMax = (left == null) ? Integer.MIN_VALUE : left.max;
        int rightMax = (right == null) ? Integer.MIN_VALUE : right.max;
        return Math.max(leftMax, rightMax);
    }
    
    private class TreeManipulator extends AbstractAvlTreeBuilder<IntervalNode<V>> {
        @Override
        public IntervalNode<V> createSubTree(IntervalNode<V> left, IntervalNode<V> top, IntervalNode<V> right) {
            int newMax = Math.max(getMaxEnd(top.entries), getMax(left, right));
            return new IntervalNode<>(top.start, newMax, top.entries, left, right);
        }
    }
    
    @CheckForNull
    private final IntervalNode<V> root;
    private final int size;
    private final TreeManipulator manipulator = new TreeManipulator();
    
    private PersistentIntervalMap(IntervalNode<V> root, int size) {
        this.root = root;
        this.size = size;
    }
    
    public static <V> PersistentIntervalMap<V> empty() {
        return new PersistentIntervalMap<>(null, 0);
    }
    
    private static <V> Path<IntervalNode<V>> pathTo(IntervalNode<V> root, int point) {
        Path.Builder<IntervalNode<V>> path = Path.newBuilder(root, AvlTreeNode.getHeight(root));
        IntervalNode<V> subTree = root;
        while (subTree != null) {
            boolean left;
            if (subTree.start > point) {
                subTree = subTree.getLeft();
                left = true;
            } else if (subTree.start < point) {
                subTree = subTree.getRight();
                left = false;
            } else { // subTree.start == point
                break;
            }
            path.append(left, subTree);
        }
        return path.build();
    }
    
    public Collection<V> getOverlappers(int point) {
        return getOverlappers(new Interval(point, point + 1));
    }
    
    public Collection<V> getOverlappers(Interval interval) {
        ArrayList.Builder<V> overlappers = ArrayList.newBuilder();
        collectOverlappers(root, interval, overlappers);
        return overlappers.build();
    }
    
    private void collectOverlappers(@CheckForNull IntervalNode<V> node, Interval interval, ArrayList.Builder<V> overlappers) {
        if (node == null) {
            return;
        }
        
        if (node.start < interval.getEnd()) {
            for (Entry<V> entry: node.entries) {
                if (entry.interval.getEnd() > interval.getStart()) {
                    overlappers.add(entry.value);
                }
            }
        }
        
        if (node.getLeft() != null && node.getLeft().max > interval.getStart()) {
            collectOverlappers(node.getLeft(), interval, overlappers);
        }
        if (node.start < interval.getEnd()) {
            collectOverlappers(node.getRight(), interval, overlappers);
        }
    }
    
    private static class IntervalEntryIterator<V> implements EntryIterator<Interval, V> {
        
        private final Iterator<IntervalNode<V>> subTreeIterator;
        
        @CheckForNull
        private Iterator<Entry<V>> currentEntryIterator = null;
        
        @CheckForNull
        private Entry<V> currentEntry = null;
        
        public IntervalEntryIterator(@CheckForNull IntervalNode<V> root) {
            subTreeIterator = TreeUtil.iterateNodes(root);
        }
        
        @Override
        public boolean next() {
            while (currentEntryIterator == null || !currentEntryIterator.hasNext()) {
                if (!subTreeIterator.hasNext()) {
                    currentEntryIterator = null;
                    currentEntry = null;
                    return false;
                }
                currentEntryIterator = subTreeIterator.next().entries.iterator();
            }
            currentEntry = currentEntryIterator.next();
            return true;
        }
        
        @Override
        public Interval getCurrentKey() throws NoSuchElementException {
            if (currentEntry == null) {
                throw new NoSuchElementException();
            }
            return currentEntry.interval;
        }
        
        @Override
        public V getCurrentValue() throws NoSuchElementException {
            if (currentEntry == null) {
                throw new NoSuchElementException();
            }
            return currentEntry.value;
        }
    }
    
    @CheckReturnValue
    public EntryIterator<Interval, V> entryIterator() {
        return new IntervalEntryIterator<V>(root);
    }
    
    @CheckReturnValue
    public PersistentIntervalMap<V> with(Interval interval, V value) {
        PreConditions.paramNotNull(value);
        IntervalNode<V> newRoot = root;
        
        Path<IntervalNode<V>> pathToStart = pathTo(newRoot, interval.getStart());
        IntervalNode<V> end = pathToStart.getEnd();
        if (end == null) {
            newRoot = manipulator.replace(pathToStart, new IntervalNode<V>(interval, value));
        } else {
            IntervalNode<V> newNode = end.with(interval, value);
            if (newNode == end) {
                return this;
            }
            newRoot = manipulator.replace(pathToStart, newNode);
        }
        return new PersistentIntervalMap<V>(newRoot, size + 1);
    }
    
    @CheckReturnValue
    public PersistentIntervalMap<V> without(Interval interval, V value) {
        Path<IntervalNode<V>> pathToStart = pathTo(root, interval.getStart());
        IntervalNode<V> end = pathToStart.getEnd();
        if (end == null) {
            return this;
        }
        
        IntervalNode<V> newNode = end.without(interval, value);
        if (newNode == end) {
            return this;
        }
        
        IntervalNode<V> newRoot;
        if (newNode == null) {
            newRoot = manipulator.remove(pathToStart);
        } else {
            newRoot = manipulator.replace(pathToStart, newNode);
        }
        return new PersistentIntervalMap<>(newRoot, size - 1);
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
}
