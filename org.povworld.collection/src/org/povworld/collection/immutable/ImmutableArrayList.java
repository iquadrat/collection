package org.povworld.collection.immutable;

import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.ArrayIterator;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.ReverseListIterator;

/**
 * Immutable list which holds elements in an array.
 *
 * <p>Note that the list can never be empty. The builder returns {@link ImmutableEmptyOrderedCollection}
 * for empty lists instead.
 *
 * @param <E> the element type
 */
@Immutable
public class ImmutableArrayList<E> extends AbstractOrderedCollection<E> implements ImmutableList<E> {
    
    private static final int DEFAULT_CAPACITY = 12;
    
    private final E[] elements;
    
    private ImmutableArrayList(Object[] elements) {
        this.elements = ArrayUtil.unsafeCast(elements);
    }
    
    @Override
    public boolean isEmpty() {
        return elements.length == 0;
    }
    
    @Override
    public int size() {
        return elements.length;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        if (elements.length == 0) {
            return null;
        }
        return get(0);
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        if (elements.length == 0) {
            return null;
        }
        return get(size() - 1);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ArrayIterator<>(elements);
    }
    
    @Override
    public E get(int index) {
        return elements[index];
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ReverseListIterator<>(this);
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    public static <E> Builder<E> newBuilder(int expectedSize) {
        return new Builder<>(expectedSize);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, ImmutableList<E>> {
        private final int expectedSize;
        
        private E[] elements;
        
        private int size;
        
        public Builder() {
            this(DEFAULT_CAPACITY);
        }
        
        public Builder(int expectedSize) {
            this.expectedSize = expectedSize;
            this.elements = ArrayUtil.unsafeCastedNewArray(expectedSize);
            this.size = 0;
        }
        
        public int getSize() {
            return size;
        }
        
        @Override
        protected void _add(E element) {
            PreConditions.paramNotNull(element);
            ensureCapacity(size + 1);
            elements[size] = element;
            size++;
        }
        
        private void ensureCapacity(int capacity) {
            if (elements.length >= capacity) {
                return;
            }
            int newCapacity = Math.max(DEFAULT_CAPACITY, size * 2);
            elements = Arrays.copyOf(elements, newCapacity);
        }
        
        @Override
        protected ImmutableList<E> _createCollection() {
            // TODO return special object if empty
            E[] exactElementArray;
            if (elements.length == size) {
                exactElementArray = elements;
            } else {
                exactElementArray = Arrays.copyOf(elements, size);
            }
            // Allow the builder's array to be garbage collected immediately.
            elements = ArrayUtil.unsafeCastedEmptyArray();
            return new ImmutableArrayList<>(exactElementArray);
        }
        
        @Override
        protected void _reset() {
            elements = ArrayUtil.unsafeCastedNewArray(expectedSize);
            size = 0;
        }
    }
    
}
