package org.povworld.collection;

import org.povworld.collection.common.AbstractCollectionBuilder;

/**
 * Builder for a specific type of {@link Collection} instances.
 * <p>
 * Example:
 * {@code
 *  ImmutableList<String> someStrings =
 *       ImmutableArrayList.<String>newBuilder()
 *           .add("foo")
 *           .addAll(CollectionUtil.wrap("1", "2", "3"))
 *           .addAll(Arrays.asList("4", "5", "6"))
 *           .add("bar)
 *           .build();
 * }
 * <p>
 * Note that when you want to reuse a builder to build another collection after calling {{@link #build()} you
 * will first need to call {@link #reset()} before adding further elements.
 * <p>
 * It is recommended to extends {@link AbstractCollectionBuilder} instead of implementing the
 * interface directly.
 *
 * @param <E> the element type
 * @param <C> the collection type
 * @see Collection
 */
public interface CollectionBuilder<E, C extends Collection<E>> {
    
    /**
     * Adds an element. If the collection does not allow duplicates, elements that are already
     * contained will be skipped.
     * 
     * @return the builder itself
     * @throws IllegalStateException if the method is called after invoking {@link #build()} without invoking {@link #reset()}
     */
    public CollectionBuilder<E, C> add(E element);
    
    /**
     * Adds a series of elements to the collection. If the collection does not allow duplicates,
     * duplicates are skipped.
     * 
     * @return the builder itself 
     * @throws IllegalStateException if the method is called after invoking {@link #build()} without invoking {@link #reset()}
     */
    public CollectionBuilder<E, C> addAll(Iterable<? extends E> elements);
    
    /** 
     * This resets the builder, i.e., the construction of a new collection begins.
     * @return the builder itself
     */
    public CollectionBuilder<E, C> reset();
    
    /**
     * Creates the collection. After calling this method the builder is in a unusable state 
     * until {@link #reset()} is called.
     * 
     * @return a collection containing all added elements since construction or last
     *         call to this method
     * @throws IllegalStateException if the method is invoked twice without invoking {@link #reset()} in-between
     */
    public C build();
    
}
