package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.Sequence;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.mutable.IntrusiveLinkedSequence.ElementLink;

/**
 * Ordered collection that stores its contained elements in a double linked list.
 * 
 * @param <E> the element type 
 */
@NotThreadSafe
public class LinkedSequence<E> extends AbstractOrderedCollection<E> implements Sequence<E> {
    
    private static class LinkIterator<T> implements Iterator<T> {
        
        private final Iterator<ElementLink<T>> delegate;
        
        LinkIterator(Iterator<ElementLink<T>> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }
        
        @Override
        public T next() {
            return delegate.next().getElement();
        }
        
        @Override
        public void remove() {
            delegate.remove();
        }
        
    }
    
    private final IntrusiveLinkedSequence<ElementLink<E>> delegate = new IntrusiveLinkedSequence<>();
    
    @Override
    public int size() {
        return delegate.size();
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        ElementLink<E> first = delegate.getFirstOrNull();
        if (first == null) {
            return null;
        }
        return first.getElement();
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        ElementLink<E> lastOrNull = delegate.getLastOrNull();
        if (lastOrNull == null) {
            return null;
        }
        return lastOrNull.getElement();
    }
    
    public void insertFront(E element) {
        delegate.insertFront(new ElementLink<E>(element));
    }
    
    public void insertBack(E element) {
        delegate.insertBack(new ElementLink<E>(element));
    }
    
    public E removeFirst() throws NoSuchElementException {
        return delegate.removeHead().getElement();
    }
    
    public E removeLast() throws NoSuchElementException {
        return delegate.removeTail().getElement();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new LinkIterator<>(delegate.iterator());
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new LinkIterator<>(delegate.reverseIterator());
    }
    
    public Iterator<E> modifyingIterator() {
        return new LinkIterator<>(delegate.modifyingIterator());
    }
    
    public static CollectionBuilder<String, LinkedSequence<String>> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static class Builder<E> extends AbstractCollectionBuilder<E, LinkedSequence<E>> {
        @Nullable
        private LinkedSequence<E> collection = new LinkedSequence<>();
        
        @Override
        protected void _add(E element) {
            collection.insertBack(element);
        }
        
        @Override
        protected void _reset() {
            collection = new LinkedSequence<>();
        }
        
        @Override
        protected LinkedSequence<E> _createCollection() {
            LinkedSequence<E> result = collection;
            collection = null;
            return result;
        }
    }
    
}
