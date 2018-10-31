package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.common.Interval;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AbstractAvlTreeNode;
import org.povworld.collection.tree.AvlTree;
import org.povworld.collection.tree.TreeUtil;

// TODO implement some map interface
public class IntervalMap<V> {
    
    private static final class IntervalNode<V> extends AbstractAvlTreeNode<IntervalNode<V>> {
        final Interval interval;
        final V value;
        int max;
        
        IntervalNode(Interval interval, V value) {
            super(null, null);
            this.interval = interval;
            this.value = value;
            max = interval.getEnd();
        }
        
        @Override
        public void setLeft(IntervalNode<V> left) {
            super.setLeft(left);
            updateMax();
        }
        
        @Override
        public void setRight(IntervalNode<V> right) {
            super.setRight(right);
            updateMax();
        }
        
        private void updateMax() {
            max = Math.max(interval.getEnd(), getMax(left, right));
        }
    }
    
    private static int getMax(@CheckForNull IntervalNode<?> left, @CheckForNull IntervalNode<?> right) {
        if (left == null) {
            if (right == null) {
                return Integer.MIN_VALUE;
            }
            return right.max;
        }
        if (right == null) {
            return left.max;
        }
        return Math.max(left.max, right.max);
    }
    
    private static class KeyValueComparator<V> implements Comparator<IntervalNode<V>> {
        private final Comparator<? super V> valueComparator;
        
        KeyValueComparator(Comparator<? super V> valueComparator) {
            this.valueComparator = valueComparator;
        }
        
        @Override
        public boolean isIdentifiable(Object object) {
            return object instanceof IntervalNode;
        }
        
        @Override
        public boolean equals(IntervalNode<V> node1, IntervalNode<V> node2) {
            return compare(node1, node2) == 0;
        }
        
        @Override
        public int hashCode(IntervalNode<V> node) {
            // This comparator is never publicly exposed and internally we do not use hash codes.
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int compare(IntervalNode<V> node1, IntervalNode<V> node2) {
            int cmp = compare(node1.interval, node2.interval);
            if (cmp != 0) {
                return cmp;
            }
            return valueComparator.compare(node1.value, node2.value);
        }
        
        private static int compare(Interval interval1, Interval interval2) {
            if (interval1.getStart() != interval2.getStart()) {
                return Integer.compare(interval1.getStart(), interval2.getStart());
            }
            // Compare the end in reverse order:
            return Integer.compare(interval2.getEnd(), interval1.getEnd());
        }
    }
    
    private final AvlTree<IntervalNode<V>, IntervalNode<V>> tree;
    private int size = 0;
    
    public IntervalMap(Comparator<? super V> valueComparator) {
        this.tree = AvlTree.<IntervalNode<V>>create(new KeyValueComparator<>(valueComparator));
    }
    
    public static <V extends Comparable<V>> IntervalMap<V> create(Class<V> clazz) {
        return new IntervalMap<>(CollectionUtil.getDefaultComparator(clazz));
    }
    
    public Iterable<V> getOverlappers(int point) {
        // TODO optimize
        return getOverlappers(new Interval(point, point + 1));
    }
    
    public Iterable<V> getOverlappers(Interval interval) {
        return new Overlappers(interval);
    }
    
    private class Overlappers implements Iterable<V> {
        
        private final Interval interval;
        
        public Overlappers(Interval interval) {
            this.interval = interval;
        }
        
        @Override
        public Iterator<V> iterator() {
            return new OverlapperIterator<>(interval, tree.getRoot());
        }
    }
    
    private static class OverlapperIterator<V> implements Iterator<V> {
        // Invariant: The left subtrees of the stack top has already been visited
        private final Interval interval;
        private final ArrayList<IntervalNode<V>> stack;
        
        public OverlapperIterator(Interval interval, IntervalNode<V> root) {
            this.stack = new ArrayList<>(root == null ? 0 : root.getHeight());
            this.interval = interval;
            pushLeftSubTrees(root);
            findNext();
        }
        
        @CheckForNull
        private V current = null;
        
        private boolean needsVisiting(@CheckForNull IntervalNode<V> subTree) {
            return subTree != null && subTree.max > interval.getStart();
        }
        
        private void pushLeftSubTrees(@CheckForNull IntervalNode<V> root) {
            IntervalNode<V> subTree = root;
            while (needsVisiting(subTree)) {
                stack.push(subTree);
                subTree = subTree.getLeft();
            }
        }
        
        @Override
        public boolean hasNext() {
            return current != null;
        }
        
        private void findNext() {
            current = null;
            while (!stack.isEmpty() && current == null) {
                IntervalNode<V> subTree = stack.peek();
                if (subTree.interval.overlaps(interval)) {
                    current = subTree.value;
                }
                if (!needsVisiting(subTree.getRight())) {
                    do {
                        subTree = stack.pop();
                        if (stack.isEmpty()) {
                            return;
                        }
                    } while (subTree == stack.peek().getRight());
                } else {
                    pushLeftSubTrees(subTree.getRight());
                    // We could check following more often to strictly avoid visiting any node that 
                    // starts after the search interval. But it is more efficient to do this here only.
                    if (stack.peek().interval.getStart() >= interval.getEnd()) {
                        stack.clear();
                    }
                }
            }
        }
        
        @Override
        public V next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            V result = current;
            findNext();
            return result;
        }
        
    }
    
    private static class IntervalEntryIterator<V> implements EntryIterator<Interval, V> {
        
        private final Iterator<IntervalNode<V>> subTreeIterator;
        
        public IntervalEntryIterator(IntervalNode<V> root) {
            subTreeIterator = TreeUtil.iterateNodes(root);
        }
        
        @CheckForNull
        private IntervalNode<V> current = null;
        
        @Override
        public boolean next() {
            if (!subTreeIterator.hasNext()) {
                return false;
            }
            current = subTreeIterator.next();
            return true;
        }
        
        @Override
        public Interval getCurrentKey() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.interval;
        }
        
        @Override
        public V getCurrentValue() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.value;
        }
    }
    
    @CheckReturnValue
    public EntryIterator<Interval, V> entryIterator() {
        return new IntervalEntryIterator<V>(tree.getRoot());
    }
    
    public boolean add(Interval interval, V value) {
        PreConditions.paramNotNull(value);
        if (!tree.insertIfNotPresent(new IntervalNode<>(interval, value))) {
            return false;
        }
        size++;
        return true;
    }
    
    public boolean remove(Interval interval, V value) {
        if (tree.remove(new IntervalNode<>(interval, value)) == null) {
            return false; 
        }
        size--;
        return true;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
}
