package org.povworld.collection.mutable;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.PreConditions;

/**
 * An intrusive collection with customizable link elements.
 *
 * @param <L> the type of the link
 */
@NotThreadSafe
public class IntrusiveLinkedSequence<L extends IntrusiveLinkedSequence.AbstractLink<L>> extends AbstractIntrusiveLinkedSequence<L> {
    
    /**
     * Link implementation the contains a single element of type {@code E}. 
     */
    @NotThreadSafe
    public static class ElementLink<E> extends AbstractLink<ElementLink<E>> {
        private E value;
        
        public ElementLink(E value) {
            PreConditions.paramNotNull(value);
            this.value = value;
        }
        
        public E getElement() {
            return value;
        }
        
        public void setValue(E value) {
            PreConditions.paramNotNull(value);
            this.value = value;
        }
    }
    
    /**
     * Moves the given {@code link} to the front of the list. The link must be already 
     * part of this collection. 
     * @pre !isDetached()
     */
    public void moveToFront(L link) {
        PreConditions.paramCheck(link, "Must not be detached!", !link.isDetached());
        link.internalDetach();
        link.internalAttach(sentinel, link, sentinel.next);
    }
    
    public static <L extends AbstractLink<L>> Builder<L> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<L extends IntrusiveLinkedSequence.AbstractLink<L>> extends
            AbstractCollectionBuilder<L, IntrusiveLinkedSequence<L>> {
        
        @Nullable
        private IntrusiveLinkedSequence<L> list = new IntrusiveLinkedSequence<>();
        
        @Override
        protected void _add(L element) {
            list.insertBack(element);
        }
        
        @Override
        protected IntrusiveLinkedSequence<L> _createCollection() {
            IntrusiveLinkedSequence<L> result = list;
            list = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            list = new IntrusiveLinkedSequence<>();
        }
        
    }
    
}
