package org.povworld.collection.persistent;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.ReverseListIterator;
import org.povworld.collection.mutable.ArrayList;

/**
 * Implementation of {@link PersistentList} that uses a tree of buckets to store the elements.
 * 
 * @param <E> list element type
 */
public class PersistentArrayList<E> extends AbstractOrderedCollection<E> implements PersistentList<E> {
    
    private static final int DEFAULT_BUCKET_MAX_SIZE = 60;
    
    private static final EmptyBucket<Object> EMPTY_BUCKET = new EmptyBucket<>();
    
    private static final PersistentArrayList<?> EMPTY_LIST = new PersistentArrayList<Object>(EMPTY_BUCKET, DEFAULT_BUCKET_MAX_SIZE);
    
    @SuppressWarnings("unchecked")
    private static <E> Bucket<E> emptyBucket() {
        return (Bucket<E>)EMPTY_BUCKET;
    }
    
    private interface Bucket<E> {
        E get(int index);
        
        int size();
        
        int bucketSize();
        
        Bucket<E> with(E element, int bucketSizeMax);
        
        Bucket<E> with(E element, int index, int bucketSizeMax);
        
        Bucket<E> without(int index); // TODO pass in bucketSizeMin
        
        Bucket<E> withReplacementAt(E element, int index);
        
        Bucket<E> withAllReplaced(E oldElement, E newElement);
        
        boolean findNext(BucketIterator<E> iterator);
        
        Bucket<E>[] split();
    }
    
    private static class EmptyBucket<E> implements Bucket<E> {
        
        @Override
        public E get(int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        public int bucketSize() {
            return 0;
        }
        
        @Override
        public Bucket<E> with(E element, int bucketSizeMax) {
            return new LeafBucket<>(ArrayUtil.<E>unsafeCast(new Object[] {element}));
        }
        
        @Override
        public Bucket<E> with(E element, int index, int bucketSizeMax) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return with(element, bucketSizeMax);
        }
        
        @Override
        public Bucket<E> without(int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public Bucket<E> withReplacementAt(E element, int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public Bucket<E> withAllReplaced(E oldElement, E newElement) {
            return this;
        }
        
        @Override
        public boolean findNext(BucketIterator<E> iterator) {
            return false;
        }
        
        @Override
        public Bucket<E>[] split() {
            throw new UnsupportedOperationException("Empty bucket cannot be split");
        }
        
    }
    
    private static class LeafBucket<E> implements Bucket<E> {
        
        private final E[] elements;
        
        LeafBucket(E[] elements) {
            PreConditions.conditionCheck("LeafBucket must not be empty", elements.length > 0);
            this.elements = elements;
        }
        
        @Override
        public E get(int index) {
            return elements[index];
        }
        
        @Override
        public int size() {
            return elements.length;
        }
        
        @Override
        public int bucketSize() {
            return size();
        }
        
        @Override
        public Bucket<E> with(E element, int bucketSizeMax) {
            return new LeafBucket<>(ArrayUtil.appendArrayElement(elements, element));
        }
        
        @Override
        public Bucket<E> with(E element, int index, int bucketSizeMax) {
            return new LeafBucket<>(ArrayUtil.insertArrayElement(elements, index, element));
        }
        
        @Override
        public Bucket<E> without(int index) {
            if (elements.length == 1) {
                return emptyBucket();
            }
            return new LeafBucket<>(ArrayUtil.removeArrayElement(elements, index));
        }
        
        @Override
        public Bucket<E> withReplacementAt(E element, int index) {
            if (elements[index].equals(element)) {
                return this;
            }
            return new LeafBucket<>(ArrayUtil.replaceArrayElement(elements, index, element));
        }
        
        @Override
        public Bucket<E> withAllReplaced(E oldElement, E newElement) {
            E[] newElements = null;
            for (int i = 0; i < elements.length; ++i) {
                if (!oldElement.equals(elements[i])) {
                    continue;
                }
                if (newElements == null) {
                    newElements = elements.clone();
                }
                newElements[i] = newElement;
            }
            if (newElements == null) {
                return this;
            }
            return new LeafBucket<>(newElements);
        }
        
        @Override
        public Bucket<E>[] split() {
            int splitPoint = size() / 2;
            Bucket<E>[] buckets = ArrayUtil.unsafeCast(new Bucket[2]);
            buckets[0] = new LeafBucket<E>(Arrays.copyOf(elements, splitPoint));
            buckets[1] = new LeafBucket<E>(Arrays.copyOfRange(elements, splitPoint, elements.length));
            return buckets;
        }
        
        @Override
        public boolean findNext(BucketIterator<E> iterator) {
            int index = iterator.getAndIncrementIndex();
            if (index == elements.length) {
                return false;
            }
            iterator.setCurrent(elements[index]);
            return true;
        }
    }
    
    private static class InnerBucket<E> implements Bucket<E> {
        
        private final Bucket<E>[] buckets;
        
        private final int[] splitPositions; // TODO we could shorten the array by one
        
        InnerBucket(Bucket<E>[] buckets, int[] splitPositions) {
            this.buckets = buckets;
            this.splitPositions = splitPositions;
        }
        
        @Override
        public E get(int index) {
            int position = findChildBucket(index);
            if (position != 0) {
                index -= splitPositions[position - 1];
            }
            return buckets[position].get(index);
        }
        
        @Override
        public int size() {
            return splitPositions[splitPositions.length - 1];
        }
        
        @Override
        public int bucketSize() {
            return buckets.length;
        }
        
        @Override
        public Bucket<E> with(E element, int bucketSizeMax) {
            // TODO just call with(element, size())?
            Bucket<E> childBucket = buckets[splitPositions.length - 1];
            if (childBucket.bucketSize() == bucketSizeMax) {
                // Split child.
                Bucket<E>[] childBuckets = childBucket.split();
                Bucket<E>[] newBuckets = ArrayUtil.appendArrayElement(buckets, childBuckets[1]);
                newBuckets[buckets.length - 1] = childBuckets[0];
                int[] newChildEndIndices = ArrayUtil.appendArrayElement(splitPositions, splitPositions[splitPositions.length - 1]);
                newChildEndIndices[splitPositions.length - 1] = newChildEndIndices[splitPositions.length] - newBuckets[splitPositions.length].size();
                return new InnerBucket<>(newBuckets, newChildEndIndices).with(element, bucketSizeMax);
            } else {
                Bucket<E> newChildBucket = childBucket.with(element, bucketSizeMax);
                int[] newChildEndIndices = ArrayUtil.replaceArrayElement(splitPositions, splitPositions.length - 1,
                        splitPositions[splitPositions.length - 1] + 1);
                Bucket<E>[] newBuckets = ArrayUtil.replaceArrayElement(buckets, splitPositions.length - 1, newChildBucket);
                return new InnerBucket<>(newBuckets, newChildEndIndices);
            }
        }
        
        @Override
        public Bucket<E> with(E element, int index, int bucketSizeMax) {
            int position = findChildBucket(index);
            position = Math.min(buckets.length - 1, position);
            if (position != 0) {
                index -= splitPositions[position - 1];
            }
            Bucket<E> childBucket = buckets[position];
            if (childBucket.bucketSize() == bucketSizeMax) {
//                TODO Split child.
//                Bucket<E>[] childBuckets = childBucket.split();
//                Bucket<E>[] newBuckets = ArrayUtil.appendArrayElement(buckets, childBuckets[1]);
//                newBuckets[buckets.length - 1] = childBuckets[0];
//                int[] newChildEndIndices = ArrayUtil.appendArrayElement(splitPositions, splitPositions[splitPositions.length - 1]);
//                newChildEndIndices[splitPositions.length - 1] = newChildEndIndices[splitPositions.length] - newBuckets[splitPositions.length].size();
//                return new InnerBucket<>(newBuckets, newChildEndIndices).with(element, bucketSizeMax);
            }
            Bucket<E> newChildBucket = childBucket.with(element, index, bucketSizeMax);
            int[] newChildEndIndices = splitPositions.clone();
            for (int i = position; i < splitPositions.length; ++i) {
                newChildEndIndices[i]++;
            }
            Bucket<E>[] newBuckets = ArrayUtil.replaceArrayElement(buckets, position, newChildBucket);
            return new InnerBucket<>(newBuckets, newChildEndIndices);
        }
        
        @Override
        public Bucket<E> without(int index) {
            int position = findChildBucket(index);
            if (position != 0) {
                index -= splitPositions[position - 1];
            }
            
            int[] newSplitPositions = splitPositions.clone();
            for (int i = position; i < splitPositions.length; ++i) {
                newSplitPositions[i] -= 1;
            }
            
            Bucket<E> newBucket = buckets[position].without(index);
            Bucket<E>[] newBuckets;
            if (newBucket == EMPTY_BUCKET) {
                if (newSplitPositions.length == 1) {
                    return emptyBucket();
                }
                newBuckets = ArrayUtil.removeArrayElement(buckets, position);
                // TODO do not copy split positions twice
                newSplitPositions = ArrayUtil.removeArrayElement(newSplitPositions, position);
            } else {
                newBuckets = ArrayUtil.replaceArrayElement(buckets, position, newBucket);
            }
            // TODO implement merging
            return new InnerBucket<>(newBuckets, newSplitPositions);
        }
        
        @Override
        public Bucket<E> withReplacementAt(E element, int index) {
            int position = findChildBucket(index);
            if (position != 0) {
                index -= splitPositions[position - 1];
            }
            Bucket<E> replacedBucket = buckets[position].withReplacementAt(element, index);
            if (replacedBucket == buckets[position]) {
                return this;
            }
            Bucket<E>[] newBuckets = ArrayUtil.replaceArrayElement(buckets, position, replacedBucket);
            return new InnerBucket<>(newBuckets, splitPositions);
        }
        
        @Override
        public Bucket<E> withAllReplaced(E oldElement, E newElement) {
            // TODO Auto-generated method stub
            return null;
        }
        
        private int findChildBucket(int index) {
            int position = Arrays.binarySearch(splitPositions, index);
            if (position < 0) {
                return -position - 1;
            } else {
                return position + 1;
            }
        }
        
        @Override
        public boolean findNext(BucketIterator<E> iterator) {
            int idx;
            while ((idx = iterator.getAndIncrementIndex()) < buckets.length) {
                Bucket<E> bucket = buckets[idx];
                iterator.push(bucket);
                return bucket.findNext(iterator);
            }
            return false;
        }
        
        @Override
        public Bucket<E>[] split() {
            int splitPoint = bucketSize() / 2;
            Bucket<E>[] result = ArrayUtil.unsafeCast(new Bucket[2]);
            int[] splitPositions0 = Arrays.copyOf(splitPositions, splitPoint);
            int[] splitPositions1 = new int[bucketSize() - splitPoint];
            for (int i = 0; i < splitPositions1.length; ++i) {
                splitPositions1[i] = splitPositions[i + splitPoint] - splitPositions[splitPoint - 1];
            }
            result[0] = new InnerBucket<E>(Arrays.copyOf(buckets, splitPoint), splitPositions0);
            result[1] = new InnerBucket<E>(Arrays.copyOfRange(buckets, splitPoint, bucketSize()), splitPositions1);
            return result;
        }
    }
    
    public static <E> PersistentArrayList<E> empty() {
        return empty(DEFAULT_BUCKET_MAX_SIZE);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> PersistentArrayList<E> empty(int bucketSizeMax) {
        if (bucketSizeMax == DEFAULT_BUCKET_MAX_SIZE) {
            return (PersistentArrayList<E>)EMPTY_LIST;
        }
        return new PersistentArrayList<>((Bucket<E>)EMPTY_BUCKET, bucketSizeMax);
    }
    
    private final Bucket<E> root;
    
    private final int bucketSizeMax;
    
    @SafeVarargs
    public PersistentArrayList(E... elements) {
        this(CollectionUtil.wrap(elements));
    }
    
    public PersistentArrayList(E element) {
        this(PersistentArrayList.<E>empty().with(element).root, DEFAULT_BUCKET_MAX_SIZE);
    }
    
    public PersistentArrayList(Collection<E> elements) {
        this(PersistentArrayList.<E>empty().withAll(elements).root, DEFAULT_BUCKET_MAX_SIZE);
    }
    
    private PersistentArrayList(Bucket<E> root, int bucketMaxSize) {
        this.root = root;
        this.bucketSizeMax = bucketMaxSize;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new BucketIterator<E>(root);
    }
    
    protected static class BucketIterator<E> implements Iterator<E> {
        
        private static final int MAX_HEIGHT = 32;                          // TODO
        
        /**
         * Stack of buckets which marks the current path of the iteration in the
         * tree.
         */
        private final Deque<Bucket<E>> bucketStack = new ArrayDeque<>(MAX_HEIGHT); // TODO use ArrayList
        
        /**
         * For each entry on the stack there is an index counter available.
         */
        private final int[] position = new int[MAX_HEIGHT];
        
        @CheckForNull
        private E currentElement = null;
        
        BucketIterator(Bucket<E> root) {
            bucketStack.push(root);
            findNext();
        }
        
        int getAndIncrementIndex() {
            int top = bucketStack.size() - 1;
            return position[top]++;
        }
        
        void push(Bucket<E> bucket) {
            position[bucketStack.size()] = 0;
            bucketStack.push(bucket);
        }
        
        void setCurrent(E element) {
            currentElement = element;
        }
        
        @Override
        public boolean hasNext() {
            return currentElement != null;
        }
        
        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            E result = currentElement;
            findNext();
            return result;
        }
        
        private boolean findNext() {
            while (!bucketStack.isEmpty()) {
                Bucket<E> top = bucketStack.peek();
                if (top.findNext(this)) {
                    return true;
                }
                bucketStack.pop();
            }
            currentElement = null;
            return false;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        // TODO optimize
        return new ReverseListIterator<>(this);
    }
    
    @Override
    public int size() {
        return root.size();
    }
    
    @Override
    public PersistentArrayList<E> with(E element) {
        PreConditions.paramNotNull(element);
        Bucket<E> newRoot = maybeGrowRoot(root.with(element, bucketSizeMax));
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    @Override
    public PersistentArrayList<E> with(E element, int index) {
        PreConditions.paramNotNull(element);
        Bucket<E> newRoot = maybeGrowRoot(root.with(element, index, bucketSizeMax));
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    @Override
    public PersistentArrayList<E> withAll(Collection<? extends E> elements) {
        // TODO optimize
        Bucket<E> newRoot = root;
        for (E element: elements) {
            newRoot = maybeGrowRoot(newRoot.with(element, bucketSizeMax));
        }
        if (newRoot == root) {
            return this;
        }
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    private Bucket<E> maybeGrowRoot(Bucket<E> newRoot) {
        if (newRoot.bucketSize() == bucketSizeMax) {
            Bucket<E>[] buckets = newRoot.split();
            int[] splitPositions = new int[2];
            splitPositions[0] = buckets[0].size();
            splitPositions[1] = splitPositions[0] + buckets[1].size();
            newRoot = new InnerBucket<>(buckets, splitPositions);
        }
        return newRoot;
    }
    
    @Override
    public E getFirstOrNull() {
        if (isEmpty()) return null;
        return root.get(0);
    }
    
    @Override
    public E getLastOrNull() {
        if (isEmpty()) return null;
        return root.get(size() - 1);
    }
    
    @Override
    public PersistentList<E> without(int index) {
        Bucket<E> newRoot = root.without(index);
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    @Override
    public E get(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException();
        }
        return root.get(index);
    }
    
    @Override
    public PersistentArrayList<E> withReplacementAt(E element, int index) {
        Bucket<E> newRoot = root.withReplacementAt(element, index);
        if (newRoot == root) {
            return this;
        }
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    @Override
    public PersistentArrayList<E> withAllReplaced(E oldElement, E newElement) {
        PreConditions.paramNotNull(oldElement);
        PreConditions.paramNotNull(newElement);
        Bucket<E> newRoot = root.withAllReplaced(oldElement, newElement);
        if (newRoot == root) {
            return this;
        }
        return new PersistentArrayList<>(newRoot, bucketSizeMax);
    }
    
    @Override
    public PersistentArrayList<E> cleared() {
        return empty(bucketSizeMax);
    }
    
    public static <E> Builder<E> newBuilder() {
        return newBuilder(DEFAULT_BUCKET_MAX_SIZE);
    }
    
    public static <E> Builder<E> newBuilder(int bucketSizeMax) {
        return new Builder<E>(bucketSizeMax);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentList<E>> {
        
        private final int bucketSizeMax;
        
        @Nullable
        private ArrayList<E> list = new ArrayList<>();
        
        Builder(int bucketSizeMax) {
            this.bucketSizeMax = bucketSizeMax;
        }
        
        @Override
        protected void _add(E element) {
            list.push(element);
        }
        
        public boolean isEmpty() {
            return list.isEmpty();
        }
        
        @Override
        protected PersistentList<E> _createCollection() {
            PersistentArrayList<E> result = empty(bucketSizeMax);
            if (!list.isEmpty()) {
                result = result.withAll(list);
            }
            list = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            list = new ArrayList<>();
        }
        
    }
}
