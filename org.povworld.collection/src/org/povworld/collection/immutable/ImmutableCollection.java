package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;

/**
 * Interface for collection implementations that are guaranteed to never change.
 *
 * @param <E> the element type
 */
@Immutable
public interface ImmutableCollection<E> extends Collection<E> {
    
}
