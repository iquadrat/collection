package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Set;

/**
 * Interface for unordered sets that are guaranteed to never change.
 *
 * @param <E> the element type
 */
@Immutable
public interface ImmutableSet<E> extends ImmutableContainer<E>, Set<E> {
    
}
