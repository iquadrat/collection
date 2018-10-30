package org.povworld.collection.mutable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.IndexedSet;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.PreConditions;

/**
 * A generic ordered set of items.
 * <p>
 * This collection supports fast lookups by both index and hash value.
 * <p>
 * Does not support removal of individual elements as this would be expensive. 
 * If removal is required, use a {@link IndexedTreeSet}.
 * 
 * 
 * @param <E> the element type
 */
//TODO implement IndexedTreeSet
@NotThreadSafe
public final class IndexedHashSet<E> extends AbstractListSetCollection<E> implements IndexedSet<E> {
    
    public IndexedHashSet() {}
    
    public IndexedHashSet(int initialCapacity) {
        super(initialCapacity);
    }
    
    public boolean add(E element) {
        PreConditions.paramNotNull(element);
        if (contains(element)) {
            return false;
        }
        addToListAndSet(element);
        return true;
    }
    
    /**
     * Adds all elements of the {@code Iterable}.
     * <p>
     * Note: The iteration may not contain {@code null}, otherwise the addition of elements
     *       will abort in the middle of the iteration and a {@code NullPointerException} is thrown.
     */
    public int addAll(Iterable<? extends E> elements) {
        int count = 0;
        for (E element: elements) {
            if (add(element)) {
                count++;
            }
        }
        return count;
    }
    
    public E set(int index, E element) {
        if (contains(element) && !list.get(index).equals(element)) {
            throw new IllegalArgumentException("Element is already contained in the set: " + element);
        }
        return uncheckedSet(index, element);
    }
    
    private E uncheckedSet(int index, E element) {
        E old = list.set(index, element);
        if (set != null) {
            set.remove(old);
            set.add(element);
        }
        modified();
        return old;
    }
    
    // TODO is this faster than CollectionUtil.sort?
//  public void sort(Comparator<? super E> comparator) {
//    Collections.sort(new JavaAdapters.ListAdapter<E>(this) {
//      
//      @Override
//      public E set(int index, E element) {
//        return IndexedHashSet.this.uncheckedSet(index, element);
//      }
//      
//    }, comparator);
//  }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, IndexedHashSet<E>> {
        
        @Nullable
        private IndexedHashSet<E> set;
        
        public Builder() {
            this(12);
        }
        
        public Builder(int expectedSize) {
            set = new IndexedHashSet<>(expectedSize);
        }
        
        @Override
        protected void _add(E element) {
            set.add(element);
        }
        
        @Override
        protected IndexedHashSet<E> _createCollection() {
            IndexedHashSet<E> result = set;
            set = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            set = new IndexedHashSet<>();
        }
        
    }
    
}
