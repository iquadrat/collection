package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.OrderedSet;

/**
 * Interface for ordered sets that are guaranteed to never change.
 *
 * @param <E>
 */
@Immutable
public interface ImmutableOrderedSet<E> extends ImmutableContainer<E>, OrderedSet<E> {
    
}
