package org.povworld.collection.immutable;

import static org.povworld.collection.CollectionUtil.wrap;

import org.povworld.collection.Collection;

/**
 * Static methods to create immutable collections.
 * 
 */
// TODO do an instanceof check first?
public class ImmutableCollections {
    private ImmutableCollections() {}
    
    public static <E> ImmutableOrderedSet<E> asOrderedSet(Iterable<E> elements) {
        return ImmutableIndexedSetImpl.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> ImmutableIndexedSet<E> asIndexedSet(Iterable<E> elements) {
        return ImmutableIndexedSetImpl.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> ImmutableSet<E> asSet(Iterable<E> elements) {
        return ImmutableHashSet.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> ImmutableList<E> asList(Iterable<E> elements) {
        return ImmutableArrayList.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> ImmutableOrderedSet<E> asOrderedSet(Collection<E> elements) {
        return ImmutableIndexedSetImpl.<E>newBuilder(elements.size()).addAll(elements).build();
    }
    
    public static <E> ImmutableIndexedSet<E> asIndexedSet(Collection<E> elements) {
        return ImmutableIndexedSetImpl.<E>newBuilder(elements.size()).addAll(elements).build();
    }
    
    public static <E> ImmutableSet<E> asSet(Collection<E> elements) {
        return ImmutableHashSet.<E>newBuilder(elements.size()).addAll(elements).build();
    }
    
    public static <E> ImmutableList<E> asList(Collection<E> elements) {
        return ImmutableArrayList.<E>newBuilder(elements.size()).addAll(elements).build();
    }
    
    @SafeVarargs
    public static <E> ImmutableOrderedSet<E> orderedSetOf(E... elements) {
        return asOrderedSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> ImmutableIndexedSet<E> indexedSetOf(E... elements) {
        return asIndexedSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> ImmutableSet<E> setOf(E... elements) {
        return asSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> ImmutableList<E> listOf(E... elements) {
        return asList(wrap(elements));
    }
    
    /**
     * Creates an immutable empty ordered set. 
     */
    public static <E> ImmutableOrderedSet<E> orderedSetOf() {
        return ImmutableEmptyOrderedCollection.instance();
    }
    
    /**
     * Creates an immutable empty unordered set. 
     */
    public static <E> ImmutableSet<E> setOf() {
        return ImmutableEmptyUnOrderedCollection.instance();
    }
    
    /**
     * Creates an immutable empty indexed set. 
     */
    public static <E> ImmutableIndexedSet<E> indexedSetOf() {
        return ImmutableEmptyOrderedCollection.instance();
    }
    
    /**
     * Creates an immutable empty list. 
     */
    public static <E> ImmutableList<E> listOf() {
        return ImmutableEmptyOrderedCollection.instance();
    }
    
    /**
     * Creates an immutable ordered set containing only the given {@code element}. 
     */
    public static <E> ImmutableOrderedSet<E> orderedSetOf(E element) {
        return new ImmutableSingletonOrderedCollection<>(element);
    }
    
    /**
     * Creates an immutable indexed set containing only the given {@code element}. 
     */
    public static <E> ImmutableIndexedSet<E> indexedSetOf(E element) {
        return new ImmutableSingletonOrderedCollection<>(element);
    }
    
    /**
     * Creates an immutable unordered set containing only the given {@code element}. 
     */
    public static <E> ImmutableSet<E> setOf(E element) {
        return new ImmutableSingletonUnOrderedCollection<>(element);
    }
    
    /**
     * Creates an immutable list containing only the given {@code element}. 
     */
    public static <E> ImmutableList<E> listOf(E element) {
        return new ImmutableSingletonOrderedCollection<>(element);
    }
}
