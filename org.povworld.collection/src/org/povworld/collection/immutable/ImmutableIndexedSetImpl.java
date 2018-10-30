package org.povworld.collection.immutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.Container;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.Assert;
import org.povworld.collection.mutable.TreeList;

/**
 * Implementation of an immutable indexed set.
 * 
 * <p>It holds an immutable list of the elements to store the element order
 * and a second data structure for doing fast containment lookups.   
 *
 * @param <E> the element type
 */
@Immutable
public class ImmutableIndexedSetImpl<E> extends AbstractOrderedCollection<E> implements ImmutableIndexedSet<E> {
    
    private final ImmutableList<E> list;
    
    private final Container<E> container;
    
    private ImmutableIndexedSetImpl(ImmutableList<E> list, Container<E> container) {
        Assert.assertFalse(list.isEmpty(), "Tried to create an empty ImmutableIndexedSetImpl!");
        this.list = list;
        this.container = container;
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public E getFirstOrNull() {
        return list.get(0);
    }
    
    @Override
    public E getLastOrNull() {
        return list.get(size() - 1);
    }
    
    @Override
    public boolean contains(E element) {
        return container.contains(element);
    }
    
    @Override
    @CheckForNull
    public E findEqualOrNull(E element) {
        return container.findEqualOrNull(element);
    };
    
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return list.reverseIterator();
    }
    
    @Override
    public E get(int index) {
        return list.get(index);
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    public static <E> Builder<E> newBuilder(int expectedSize) {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, ImmutableIndexedSet<E>> {
        
        @Nullable
        private TreeList<E> treeList = new TreeList<>();
        
        @Override
        public Builder<E> add(E element) {
            super.add(element);
            return this;
        }
        
        @Override
        protected void _add(E element) {
            if (!treeList.contains(element)) {
                treeList.add(element);
            }
        }
        
        @Override
        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll(elements);
            return this;
        }
        
        public Builder<E> remove(E element) {
            checkUsable();
            treeList.remove(element);
            return this;
        }
        
        @Override
        public ImmutableIndexedSet<E> _createCollection() {
            if (treeList.isEmpty()) {
                return ImmutableCollections.indexedSetOf();
            }
            ImmutableIndexedSetImpl<E> result = new ImmutableIndexedSetImpl<E>(
                    ImmutableCollections.asList(treeList),
                    ImmutableCollections.asSet(treeList));
            treeList = null;
            return result;
        }
        
        @Override
        public CollectionBuilder<E, ImmutableIndexedSet<E>> reset() {
            super.reset();
            return this;
        }
        
        @Override
        protected void _reset() {
            treeList = new TreeList<>();
        }
    }
    
}
