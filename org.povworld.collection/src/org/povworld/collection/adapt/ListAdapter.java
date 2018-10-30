package org.povworld.collection.adapt;

import java.util.Iterator;

import org.povworld.collection.List;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.ReadOnlyIterator;
import org.povworld.collection.common.ReverseListIterator;

/**
 * Adapts from {@link java.util.List} to {@link List}.
 */
public class ListAdapter<E> extends AbstractOrderedCollection<E> implements List<E> {
    
    private final java.util.List<E> list;
    
    public ListAdapter(java.util.List<E> list) {
        this.list = list;
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
    public E getFirstOrNull() {
        if (list.isEmpty()) return null;
        return list.get(0);
    }
    
    @Override
    public E getLastOrNull() {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ReadOnlyIterator<>(list.iterator());
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ReverseListIterator<E>(this);
    }
    
    public Iterator<E> modifyingIterator() {
        return list.iterator();
    }
    
    @Override
    public E get(int index) {
        return list.get(index);
    }
    
    public void add(E element) {
        list.add(element);
    }
    
    public void add(int index, E element) {
        list.add(index, element);
    }
    
    public E remove(int index) {
        return list.remove(index);
    }
    
}
