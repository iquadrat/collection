package org.povworld.collection.mutable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Sequence;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.PreConditions;

/**
 * Abstract implementation of an intrusive collection with customizable link elements.
 * 
 * @param <L> the concrete link type
 */
@NotThreadSafe
public abstract class AbstractIntrusiveLinkedSequence<L extends AbstractIntrusiveLinkedSequence.AbstractLink<L>> extends
        AbstractOrderedCollection<L> implements Sequence<L> {
    
    /**
     * Abstract base class for all links.
     * <p>
     * Instances of {@code AbstractLink} can be attached to at most one collection at a time.
     *
     * @param <L> the concrete link type
     */
    @NotThreadSafe
    public abstract static class AbstractLink<L extends AbstractIntrusiveLinkedSequence.AbstractLink<L>> {
        
        @Nullable
        protected L next = null;
        
        @Nullable
        protected L previous = null;
        
        /**
         * Attaches this link object to some collection. Note that we need to pass "this" 
         * as {@code self} explicitly as all the previous and next pointers have to be of 
         * type {@code L}.
         * 
         * @pre isDetached()
         * 
         * @param previous the link element that comes before this link
         * @param self the same as {@code this}
         * @param next the link element that comes after this link
         */
        protected final void attach(L previous, L self, L next) {
            Assert.assertTrue(this == self, "self != this");
            Assert.assertTrue(previous.next == next, "Invalid previous/next");
            internalAttach(previous, self, next);
        }
        
        /**
         * Attaches this link object into the collection. Note that we need to pass "this"
         * as {@code self} explicitly as all the previous and next pointers have to be of 
         * type {@code L}.
         * 
         * @pre isDetached()
         * 
         * @param previous the link element that comes before this link
         * @param self the same as {@code this}
         * @param next the link element that comes after this link
         */
        protected void internalAttach(L previous, L self, L next) {
            self.previous = previous;
            self.next = next;
            previous.next = self;
            next.previous = self;
        }
        
        /**
         * Detaches this link object from it's current collection.
         * 
         * @pre !isDetached()
         */
        protected final void detach() {
            Assert.assertFalse(isDetached(), "Link element not part of a list!");
            internalDetach();
        }
        
        /**
         * Detaches this link object from it's current collection.
         * 
         * @pre !isDetached()
         */
        protected void internalDetach() {
            next.previous = previous;
            previous.next = next;
            next = null;
            previous = null;
        }
        
        protected boolean isDetached() {
            return previous == null;
        }
    }
    
    /**
     * Sentinel link object which is the predecessor of the first and the successor of 
     * the last link element. Sentinels cannot are never exposed through the public interface 
     * and cannot be detached from the collection.
     */
    @NotThreadSafe
    protected static class Sentinel extends AbstractLink<Sentinel> {
        
        @SuppressWarnings("unchecked")
        public static <L extends AbstractLink<L>> L create() {
            return (L)new Sentinel();
        }
        
        protected Sentinel() {
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
    
    private static class LinkIterator<L extends AbstractLink<L>> implements Iterator<L> {
        
        private final AbstractIntrusiveLinkedSequence<L> list;
        
        @CheckForNull
        private L current = null;
        
        private L next;
        
        public LinkIterator(AbstractIntrusiveLinkedSequence<L> list) {
            this.list = list;
            this.next = ObjectUtil.checkNotNull(list.sentinel.next);
        }
        
        @Override
        public boolean hasNext() {
            return next != list.sentinel;
        }
        
        @Override
        public L next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (next.isDetached()) {
                // if element is detached, someone must have modified the list
                throw new ConcurrentModificationException();
            }
            current = next;
            next = next.next;
            return current;
        }
        
        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            if (current.isDetached()) {
                // if element is detached, someone must have modified the list
                throw new ConcurrentModificationException();
            }
            list.remove(current);
            current = null;
        }
        
    }
    
    private static class LinkReverseIterator<L extends AbstractLink<L>> implements Iterator<L> {
        
        private final AbstractIntrusiveLinkedSequence<L> list;
        
        @CheckForNull
        private L current = null;
        
        private L previous;
        
        public LinkReverseIterator(AbstractIntrusiveLinkedSequence<L> list) {
            this.list = list;
            this.previous = ObjectUtil.checkNotNull(list.sentinel.previous);
        }
        
        @Override
        public boolean hasNext() {
            return previous != list.sentinel;
        }
        
        @Override
        public L next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (previous.isDetached()) {
                // if element is detached, someone must have modified the list
                throw new ConcurrentModificationException();
            }
            current = previous;
            previous = previous.previous;
            return current;
        }
        
        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            if (current.isDetached()) {
                // if element is detached, someone must have modified the list
                throw new ConcurrentModificationException();
            }
            list.remove(current);
            current = null;
        }
        
    }
    
    protected final L sentinel;
    
    private int size = 0;
    
    protected AbstractIntrusiveLinkedSequence() {
        this(Sentinel.<L>create());
    }
    
    /**
     * Creates an empty list using the given sentinel object. The sentinel must be a 
     * link element which has initially previous and next connected to itself and can
     * never be removed from the list.
     */
    protected AbstractIntrusiveLinkedSequence(L sentinel) {
        this.sentinel = sentinel;
    }
    
    /**
     * Inserts given {@code link} at the beginning of the list.
     * 
     * @param link new link to insert
     */
    public void insertFront(L link) {
        PreConditions.paramCheck(link, "Link is already part of a list!", link.isDetached());
        link.attach(sentinel, link, sentinel.next);
        size++;
    }
    
    /**
     * Inserts given {@code link} at the end of the list.
     * 
     * @param link new link to insert
     */
    public void insertBack(L link) {
        PreConditions.paramCheck(link, "Link is already part of a list!", link.isDetached());
        link.attach(sentinel.previous, link, sentinel);
        size++;
    }
    
    /**
     * Removes the first element from the collection and returns it.
     * 
     * @throws NoSuchElementException if the collection is empty
     */
    public L removeHead() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        L removed = sentinel.next;
        removed.detach();
        size--;
        return removed;
    }
    
    /**
     * Removes the last element from collection and returns it.
     * 
     * @throws NoSuchElementException if the collection is empty
     */
    public L removeTail() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        L removed = sentinel.previous;
        removed.detach();
        size--;
        return removed;
    }
    
    public boolean remove(L element) {
        if (element.isDetached()) {
            return false;
        }
        element.detach();
        size--;
        return true;
    }
    
    @Override
    public Iterator<L> iterator() {
        return new LinkIterator<L>(this) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public Iterator<L> modifyingIterator() {
        return new LinkIterator<L>(this);
    }
    
    protected void clear() {
        while (sentinel.next != sentinel) {
            sentinel.next.detach();
        }
        size = 0;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Iterator<L> reverseIterator() {
        return new LinkReverseIterator<L>(this) {
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    public Iterator<L> modifyingReverseIterator() {
        return new LinkReverseIterator<>(this);
    }
    
    @Override
    public L getFirstOrNull() {
        if (isEmpty()) return null;
        return sentinel.next;
    }
    
    @Override
    public L getLastOrNull() {
        if (isEmpty()) return null;
        return sentinel.previous;
    }
}
