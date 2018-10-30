package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.PreConditions;

/**
 * Intrusive list which supports safe concurrent removal and addition of objects while being iterated.
 * If elements are inserted or removed at a position after the current position of the iteration, then
 * these changes will be reflected by the iterator. All changes before the iterator's current
 * position will be ignored.
 * 
 * @param <L> the concrete link type
 */
@ThreadSafe
public class ConcurrentIntrusiveLinkedSequence<L extends ConcurrentIntrusiveLinkedSequence.AbstractLink<L>> extends
        AbstractIntrusiveLinkedSequence<L> {
    
    /**
     * Link implementation that contains a single value of type {@code E}. The element cannot be changed.
     * <p>
     * Accessing the element through {@link #getElement()} is always thread safe. All accesses that read or
     * modify it's linking state are synchronized by the owning's list {@code Sentinel}.
     * 
     * @param <L> the concrete link type
     */
    @NotThreadSafe
    public static class ElementLink<E> extends AbstractLink<ElementLink<E>> {
        private final E value;
        
        public ElementLink(E value) {
            PreConditions.paramNotNull(value);
            this.value = value;
        }
        
        public E getValue() {
            return value;
        }
    }
    
    /**
     * Base class for all link types usable with {@code ConcurrentIntrusiveLinkedCollection}.
     * <p>
     * Links can only be inserted once. I.e., re-inserting a previously removed link into 
     * a collection will result in a {@link IllegalStateException}.
     * <p>
     * All accesses that read or modify it's linking state are synchronized by the owning's
     * list {@code Sentinel}.
     *
     * @param <L> the concrete link type
     */
    @NotThreadSafe
    public abstract static class AbstractLink<L extends ConcurrentIntrusiveLinkedSequence.AbstractLink<L>> extends
            AbstractIntrusiveLinkedSequence.AbstractLink<L> {
        
        private enum State {
            NEW,
            ATTACHED,
            DETACHED
        };
        
        private State state = State.NEW;
        
        @Override
        protected boolean isDetached() {
            return state != State.ATTACHED;
        }
        
        @Override
        protected void internalAttach(L previous, L self, L next) {
            if (state != State.NEW) {
                PreConditions.conditionCheck("Only new links can be attached but state was " + state, false);
            }
            super.internalAttach(previous, self, next);
            state = State.ATTACHED;
        }
        
        @Override
        protected void internalDetach() {
            state = State.DETACHED;
            next.previous = previous;
            previous.next = next;
            // do not clear next/previous pointer as we need it for safe iteration
        }
    }
    
    private static class Sentinel extends AbstractLink<Sentinel> {
        
        @SuppressWarnings("unchecked")
        public static <L extends AbstractLink<L>> L create() {
            return (L)new Sentinel();
        }
        
        private Sentinel() {
            next = this;
            previous = this;
        }
        
        @Override
        protected void internalDetach() {
            throw Assert.fail("Tried to detach the sentinel link!");
        }
        
        @Override
        protected void internalAttach(Sentinel previous, Sentinel self, Sentinel next) {
            throw Assert.fail("Tried to attach the sentinel link!");
        }
    }
    
    private abstract static class AbstractSafeListIterator<L extends AbstractLink<L>> implements Iterator<L> {
        
        protected final ConcurrentIntrusiveLinkedSequence<L> list;
        
        /**
         * Points to the last element returned by {@link #next()} or to the list's sentinel
         * if the iteration hash not begun.
         */
        protected L current;
        
        /**
         * Is set on calling {@link #hasNext()} and cleared in {@link #next()}.
         */
        @CheckForNull
        protected L next = null;
        
        private boolean removable = false;
        
        public AbstractSafeListIterator(ConcurrentIntrusiveLinkedSequence<L> list) {
            this.list = list;
            this.current = list.sentinel;
        }
        
        @Override
        public boolean hasNext() {
            if (next == null) {
                searchNext();
            }
            return next != list.sentinel;
        }
        
        protected abstract void searchNext();
        
        @Override
        public L next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            PreConditions.conditionCheck("next is null", next != null);
            current = next;
            next = null;
            removable = true;
            return current;
        }
        
        /**
         * {@inheritDoc}
         * <p>
         * Note that if the element has been concurrently removed from outside of this iterator,
         * this operation will do nothing and this condition will be silently ignored.
         */
        @Override
        public void remove() {
            if (!removable) {
                throw new IllegalStateException();
            }
            list.remove(current);
            removable = false;
        }
        
    }
    
    private static class ForwardSafeListIterator<L extends AbstractLink<L>> extends AbstractSafeListIterator<L> {
        
        public ForwardSafeListIterator(ConcurrentIntrusiveLinkedSequence<L> list) {
            super(list);
        }
        
        @Override
        protected void searchNext() {
            synchronized (list.sentinel) {
                
                next = current.next;
                while (next != list.sentinel && next.isDetached()) {
                    // link must have been detached during iteration
                    next = next.next;
                }
                
            }
        }
    }
    
    private static class ReverseSafeListIterator<L extends AbstractLink<L>> extends AbstractSafeListIterator<L> {
        
        public ReverseSafeListIterator(ConcurrentIntrusiveLinkedSequence<L> list) {
            super(list);
        }
        
        @Override
        protected void searchNext() {
            synchronized (list.sentinel) {
                
                next = current.previous;
                while (next != list.sentinel && next.isDetached()) {
                    // link must have been detached during iteration
                    next = next.previous;
                }
                
            }
        }
        
    }
    
    /**
     * Creates an empty {@code ConcurrentIntrusiveLinkedCollection}.
     */
    public ConcurrentIntrusiveLinkedSequence() {
        super(Sentinel.<L>create());
    }
    
    @Override
    public void insertFront(L link) {
        synchronized (sentinel) {
            super.insertFront(link);
        }
    }
    
    @Override
    public void insertBack(L link) {
        synchronized (sentinel) {
            super.insertBack(link);
        }
    }
    
    /**
     * Atomically clears all elements of the list. This guarantees that all iterators
     * created before calling {@code clear()} will reach the end of the iteration in
     * the next step.
     * 
     * <p>WARNING: This operation takes O(size) time.
     */
    @Override
    public void clear() {
        synchronized (sentinel) {
            super.clear();
        }
    }
    
    @Override
    public Iterator<L> iterator() {
        return new ForwardSafeListIterator<L>(this) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public Iterator<L> modifyingIterator() {
        return new ForwardSafeListIterator<L>(this);
    }
    
    @Override
    public boolean remove(L element) {
        synchronized (sentinel) {
            return super.remove(element);
        }
    }
    
    @Override
    public L removeHead() {
        synchronized (sentinel) {
            return super.removeHead();
        }
    }
    
    @Override
    public L removeTail() {
        synchronized (sentinel) {
            return super.removeTail();
        }
    }
    
    @Override
    public int size() {
        synchronized (sentinel) {
            return super.size();
        }
    }
    
    @Override
    public L getFirstOrNull() {
        synchronized (sentinel) {
            return super.getFirstOrNull();
        }
    }
    
    @Override
    public L getLastOrNull() {
        synchronized (sentinel) {
            return super.getLastOrNull();
        }
    }
    
    @Override
    public boolean isEmpty() {
        synchronized (sentinel) {
            return super.isEmpty();
        }
    }
    
    @Override
    public Iterator<L> reverseIterator() {
        return new ReverseSafeListIterator<L>(this) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public Iterator<L> modifyingReverseIterator() {
        return new ReverseSafeListIterator<L>(this);
    }    
    
    public static <L extends ConcurrentIntrusiveLinkedSequence.AbstractLink<L>> Builder<L> newBuilder() {
        return new Builder<L>();
    }
    
    @NotThreadSafe
    public static final class Builder<L extends ConcurrentIntrusiveLinkedSequence.AbstractLink<L>> extends
            AbstractCollectionBuilder<L, ConcurrentIntrusiveLinkedSequence<L>> {
        
        @Nullable
        private ConcurrentIntrusiveLinkedSequence<L> list = new ConcurrentIntrusiveLinkedSequence<L>();
        
        @Override
        protected void _add(L element) {
            list.insertBack(element);
        }
        
        @Override
        protected ConcurrentIntrusiveLinkedSequence<L> _createCollection() {
            ConcurrentIntrusiveLinkedSequence<L> result = list;
            list = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            list = new ConcurrentIntrusiveLinkedSequence<L>();
        }
        
    }
    
}
