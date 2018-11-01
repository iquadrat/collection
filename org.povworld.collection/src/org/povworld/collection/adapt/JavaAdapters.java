package org.povworld.collection.adapt;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.povworld.collection.Collection;
import org.povworld.collection.Container;
import org.povworld.collection.IndexedCollection;
import org.povworld.collection.Set;
import org.povworld.collection.common.ObjectUtil;

// TODO what about maps?
public class JavaAdapters {
    
    private JavaAdapters() {}
    
    /**
     * Adapts from a {@link IndexedCollection} to a java {@link java.util.List}.
     * The {@link IndexedCollection} cannot be modified through this adapter, 
     * modifications of the base collection are reflected by the adapter. 
     * The adapter is thread-safe if and only if the base collection is thread-safe.
     * 
     * @param <E> the element type
     */
    public static <E> java.util.List<E> asList(IndexedCollection<E> indexedCollection) {
        return new ListAdapter<E>(indexedCollection);
    }
    
    /**
     * Adapts from a {@link Set} to a java {@link java.util.Set}.
     * The {@link Set} cannot be modified through this adapter, 
     * modifications of the base set are reflected by the adapter. 
     * The adapter is thread-safe if and only if the base set is thread-safe.
     * 
     * @param <E> the element type
     */
    public static <E> java.util.Set<E> asSet(Set<E> set) {
        return new SetAdapter<E>(set);
    }
    
    /**
     * Adapts from a {@link Collection} to a java {@link java.util.Collection}.
     * The {@link Collection} cannot be modified through this adapter, 
     * modifications of the base set are reflected by the adapter. 
     * The adapter is thread-safe if and only if the base set is thread-safe.
     * 
     * @param <E> the element type
     */
    public static <E> java.util.Collection<E> asCollection(Collection<E> collection) {
        return new CollectionAdapter<E>(collection);
    }
    
    /**
     * Adapts from a {@link Container} to a java {@link java.util.Collection}.
     * The {@link Container} cannot be modified through this adapter, 
     * modifications of the base set are reflected by the adapter. 
     * The adapter is thread-safe if and only if the base set is thread-safe.
     * 
     * @param <E> the element type
     */
    public static <E> java.util.Collection<E> asCollection(Container<E> containment) {
        return new ContainerAdapter<E>(containment);
    }
    
    public static class CollectionAdapter<E> extends java.util.AbstractCollection<E> {
        
        protected final Collection<E> collection;
        
        private CollectionAdapter(Collection<E> collection) {
            this.collection = collection;
        }
        
        @Override
        public int size() {
            return collection.size();
        }
        
        @Override
        public Iterator<E> iterator() {
            return collection.iterator();
        }
        
        @Override
        public boolean isEmpty() {
            return collection.isEmpty();
        }
        
        @Override
        public boolean equals(@CheckForNull Object object) {
            CollectionAdapter<?> other = ObjectUtil.castOrNull(object, CollectionAdapter.class);
            if (other == null) return false;
            return collection.equals(other.collection);
        }
        
        @Override
        public int hashCode() {
            return collection.hashCode();
        }
        
    }
    
    private static class SetAdapter<E> extends java.util.AbstractSet<E> {
        
        protected final Set<E> set;
        
        public SetAdapter(Set<E> set) {
            this.set = set;
        }
        
        @Override
        public int size() {
            return set.size();
        }
        
        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }
        
        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }
        
    }
    
    private static class ListAdapter<E> extends java.util.AbstractList<E> {
        
        protected final IndexedCollection<E> indexedCollection;
        
        public ListAdapter(IndexedCollection<E> indexedCollection) {
            this.indexedCollection = indexedCollection;
        }
        
        @Override
        public E get(int index) {
            return indexedCollection.get(index);
        }
        
        @Override
        public int size() {
            return indexedCollection.size();
        }
        
        @Override
        public boolean isEmpty() {
            return indexedCollection.isEmpty();
        }
        
    }
    
    private static class ContainerAdapter<E> extends CollectionAdapter<E> {
        
        protected final Container<E> container;
        
        public ContainerAdapter(Container<E> containment) {
            super(containment);
            this.container = containment;
        }
        
        @Override
        public boolean contains(Object o) {
            if (!container.getIdentificator().isIdentifiable(o)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            E element = (E)o;
            return container.contains(element);
        }
        
    }
    
}
