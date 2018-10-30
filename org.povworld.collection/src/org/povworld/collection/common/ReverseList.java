package org.povworld.collection.common;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;

/**
 * @see CollectionUtil#reverse(List)
 * 
 * @param <E> element type
 */
@NotThreadSafe
public class ReverseList<E> extends ReverseOrderedCollection<E> implements List<E> {
    
    protected final List<E> list;
    
    public ReverseList(List<E> list) {
        super(list);
        this.list = list;
    }
    
    @Override
    public E get(int index) {
        return list.get(list.size() - index - 1);
    }
}
