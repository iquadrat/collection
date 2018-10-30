package org.povworld.collection.mutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Container;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;

/**
 * Base class for collections which stores elements both in a {@link ArrayList} and in a {@link HashSet}.
 * <p>
 * This allows for both accessing elements by index and fast containment queries.
 * <p>
 * Does not support removal of individual elements as this would be expensive. 
 * If removal is required, use a {@link TreeList}.
 * 
 * @param <E> element type
 */
@NotThreadSafe
abstract class AbstractListSetCollection<E> extends AbstractOrderedCollection<E> implements Container<E> {
    
    protected static final int USE_SET_MIN_SIZE = 28;
    
    protected final ArrayList<E> list;
    
    @CheckForNull
    protected HashSet<E> set = null;
    
    private int hashCode = 0;
    
    protected AbstractListSetCollection() {
        list = new ArrayList<E>();
    }
    
    protected AbstractListSetCollection(int capacity) {
        list = new ArrayList<E>(capacity);
        if (capacity >= USE_SET_MIN_SIZE) {
            set = new HashSet<E>(capacity);
        }
    }
    
    protected void modified() {
        hashCode = 0;
    }
    
    protected void addToListAndSet(E element) {
        PreConditions.paramNotNull(element);
        list.push(element);
        addElementToSet(element);
        modified();
    }
    
    protected final void addElementToSet(E element) {
        if (set == null && list.size() >= USE_SET_MIN_SIZE) {
            set = new HashSet<E>(USE_SET_MIN_SIZE);
            set.addAll(list);
        }
        if (set != null) {
            set.add(element);
        }
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public boolean contains(E element) {
        return (set != null) ? set.contains(element) : CollectionUtil.contains(list, element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return (set != null) ? set.findEqualOrNull(element) : CollectionUtil.firstEqualOrNull(list, element);
    }
    
    public E get(int index) {
        return list.get(index);
    }
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return list.reverseIterator();
    }
    
    @Override
    public E getFirstOrNull() {
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    
    @Override
    public E getLastOrNull() {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
    
    /**
     * Removes all elements in this collection.
     */
    public void clear() {
        list.clear();
        set = null;
        modified();
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = list.hashCode();
        }
        return hashCode;
    }
}
