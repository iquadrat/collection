package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.List;

/**
 * Interface for lists that are guaranteed to never change.
 *
 * @param <E> the element type
 */
@Immutable
public interface ImmutableList<E> extends ImmutableCollection<E>, List<E> {
    
}
