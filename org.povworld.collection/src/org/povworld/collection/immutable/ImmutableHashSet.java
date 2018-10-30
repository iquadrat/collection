package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.Assert;
import org.povworld.collection.mutable.HashSet;

/**
 * Immutable unordered set that uses a {@link HashSet} to store the elements. 
 *
 * @param <E> the element type
 */
@Immutable
public class ImmutableHashSet<E> extends AbstractUnOrderedCollection<E> implements ImmutableSet<E> {
    
    private final HashSet<E> set;
    
    private ImmutableHashSet(HashSet<E> set) {
        Assert.assertFalse(set.isEmpty(), "Tried to create an empty ImmutableHashSet!");
        this.set = set;
    }
    
    @Override
    public int size() {
        return set.size();
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public E getFirstOrNull() {
        return set.getFirst();
    }
    
    @Override
    public boolean contains(E element) {
        return set.contains(element);
    }
    
    @Override
    @CheckForNull
    public E findEqualOrNull(E element) {
        return set.findEqualOrNull(element);
    };
    
    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    public static <E> Builder<E> newBuilder(int size) {
        return new Builder<>(size);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, ImmutableSet<E>> {
        
        private final int expectedSize;
        
        @Nullable
        private HashSet<E> elements;
        
        private Builder() {
            this(-1);
        }
        
        private Builder(int expectedSize) {
            this.expectedSize = expectedSize;
            this.elements = initHash(expectedSize);
        }
        
        @Override
        public Builder<E> add(E element) {
            super.add(element);
            return this;
        }
        
        @Override
        protected void _add(E element) {
            elements.add(element);
        }
        
        public Builder<E> remove(E element) {
            checkUsable();
            elements.remove(element);
            return this;
        }
        
        @Override
        protected ImmutableSet<E> _createCollection() {
            if (elements.isEmpty()) {
                return ImmutableCollections.setOf();
            }
            ImmutableHashSet<E> result = new ImmutableHashSet<E>(elements);
            elements = null;
            return result;
        }
        
        @Override
        public Builder<E> reset() {
            super.reset();
            return this;
        }
        
        @Override
        protected void _reset() {
            elements = initHash(expectedSize);
        }
        
        private static <E> HashSet<E> initHash(int expectedSize) {
            return (expectedSize == -1) ? new HashSet<E>() : new HashSet<E>(expectedSize);
        }
    }
}
