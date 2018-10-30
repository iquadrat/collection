package org.povworld.collection.mutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.ReverseListIterator;

/**
 * List implementation which stores the elements in an array. Dynamically grows array
 * when adding elements.
 * <p>
 * Elements can only be added and removed add the end of the list. Hence, this class
 * implements a stack.
 * 
 * @param <E> the element type
 */
@NotThreadSafe
public class ArrayList<E> extends AbstractOrderedCollection<E> implements List<E> {
    
    private static final int DEFAULT_SIZE = 12;
    
    private E[] elements;
    
    private int size;
    
    @SafeVarargs
    public static <E> ArrayList<E> of(E... elements) {
        return new ArrayList<>(elements.clone(), elements.length);
    }
    
    /**
     * Creates an empty instance.
     */
    public ArrayList() {
        this(DEFAULT_SIZE);
    }
    
    /**
     * Creates an empty instance with the initial capacity of {@code initialCapacity}. 
     */
    public ArrayList(int initialCapacity) {
        this(new Object[initialCapacity], 0);
    }
    
    private ArrayList(Object[] elements, int initialSize) {
        this.elements = ArrayUtil.unsafeCast(elements.clone());
        this.size = initialSize;
    }
    
    public static <E> ArrayList<E> copyOf(Collection<? extends E> elements) {
        return new ArrayList<E>(CollectionUtil.toObjectArray(elements), elements.size());
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        if (isEmpty()) return null;
        return get(0);
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        if (isEmpty()) return null;
        return get(size() - 1);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ArrayListIterator();
    }
    
    private class ArrayListIterator implements Iterator<E> {
        private int nextIndex = 0;
        
        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }
        
        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            E next = elements[nextIndex];
            nextIndex++;
            return next;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ReverseListIterator<E>(this);
    }
    
    @Override
    public E get(int index) {
        checkIndex(index);
        return elements[index];
    }
    
    /**
     * Adds the {@code element} to the end of the list.
     */
    public void push(E element) {
        PreConditions.paramNotNull(element);
        ensureCapacity(size + 1);
        elements[size] = element;
        size++;
    }
    
    /**
     * Adds all elements of the collection to the end of the list.
     * <p>
     * Note: The collection may not contain {@code null}, otherwise the addition of elements
     *       will abort in the middle of the iteration and a {@code NullPointerException} is thrown.
     */
    public void pushAll(Collection<? extends E> elements) {
        ensureCapacity(size + elements.size());
        for (E element: elements) {
            this.elements[size] = ObjectUtil.checkNotNull(element);
            size++;
        }
    }
    
    /**
     * @see #getLastOrNull()
     */
    @CheckForNull
    public E peek() {
        return getLastOrNull();
    }
    
    /**
     * Removes the last element (as given by {@link #getLast()} from the list.
     * 
     * @return the removed element or null if the list is empty
     */
    public E pop() {
        if (isEmpty()) {
            return null;
        }
        size--;
        E element = elements[size];
        // Clear array entry to not prevent garbage collection of object.
        elements[size] = null;
        return element;
    }
    
    /**
     * Adds the {@code element} to the end of the list. A synonym for {@link #push(Object)}.
     */
    public void add(E element) {
        push(element);
    }
    
    private void ensureCapacity(int capacity) {
        if (elements.length >= capacity) return;
        // TODO should we use phi as multiplication factor instead?
        int newCapacity = capacity + capacity / 2;
        resizeElementArray(newCapacity);
    }
    
    private void resizeElementArray(int newCapacity) {
        E[] newElements = ArrayUtil.<E>unsafeCastedNewArray(newCapacity);
        
        // newElements = Arrays.copyOf(elements, newCapacity);
        // TODO check which variant is faster
        
        for (int i = 0; i < size; ++i) {
            newElements[i] = elements[i];
        }
        elements = newElements;
    }
    
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }
    
    public E set(int index, E element) {
        checkIndex(index);
        E oldElement = elements[index];
        elements[index] = element;
        return oldElement;
    }
    
    public void clear() {
        for (int i = 0; i < size; ++i) {
            elements[i] = null;
        }
        size = 0;
    }
    
    public void trimToSize() {
        resizeElementArray(size);
    }
    
    public void sort(java.util.Comparator<? super E> comparator) {
        Arrays.sort(elements, 0, size, comparator);
    }
    
    public static <E> Builder<E> newBuilder() {
        return newBuilder(DEFAULT_SIZE);
    }
    
    public static <E> Builder<E> newBuilder(int expectedSize) {
        return new Builder<E>(expectedSize);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, ArrayList<E>> {
        @Nullable
        private ArrayList<E> list;
        
        private Builder(int expectedSize) {
            list = new ArrayList<>(expectedSize);
        }
        
        @Override
        protected void _add(E element) {
            list.push(element);
        }
        
        @Override
        protected ArrayList<E> _createCollection() {
            ArrayList<E> result = list;
            list = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            list = new ArrayList<>();
        }
    }
}
