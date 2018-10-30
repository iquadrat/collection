package org.povworld.collection.mutable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Container;
import org.povworld.collection.List;
import org.povworld.collection.common.AbstractCollectionBuilder;

/**
 * An implementation of {@link List} that supports fast {@link #contains(Object)} queries.
 * <p>
 * Does not support removal of individual elements as this would be expensive. 
 * If removal is required, use a {@link TreeList}.
 *  
 * @param <E> the element type
 */
@NotThreadSafe
public class HashList<E> extends AbstractListSetCollection<E> implements List<E>, Container<E> {
    
    public static <E> HashList<E> create(Iterable<E> elements) {
        return HashList.<E>newBuilder().addAll(elements).build();
    }
    
    public HashList() {}
    
    public HashList(int initialCapacity) {
        super(initialCapacity);
    }
    
    public void add(E element) {
        addToListAndSet(element);
    }
    
    /**
     * Adds all elements of the given {@code iterable}.
     * <p>
     * Note: The iteration may not contain {@code null}, otherwise the addition of elements
     *       will abort in the middle of the iteration and a {@code NullPointerException} is thrown.
     */
    public void addAll(Iterable<? extends E> iterable) {
        for (E element: iterable) {
            add(element);
        }
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    public static <E> Builder<E> newBuilder(int expectedSize) {
        return new Builder<>(expectedSize);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, HashList<E>> {
        
        @Nullable
        private HashList<E> list;
        
        public Builder() {
            list = new HashList<>();
        }
        
        public Builder(int expectedSize) {
            list = new HashList<>(expectedSize);
        }
        
        @Override
        protected void _add(E element) {
            list.add(element);
        }
        
        @Override
        protected void _addAll(Iterable<? extends E> elements) {
            list.addAll(elements);
        }
        
        @Override
        protected HashList<E> _createCollection() {
            HashList<E> result = list;
            list = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            list = new HashList<>();
        }
        
    }
    
}
