package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.IndexedSet;

/**
 * Interface for immutable indexed sets.
 *
 * @param <E> the element type
 */
@Immutable
public interface ImmutableIndexedSet<E> extends ImmutableOrderedSet<E>, IndexedSet<E> {
    
}
