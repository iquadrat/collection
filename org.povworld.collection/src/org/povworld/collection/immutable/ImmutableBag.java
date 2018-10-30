package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Bag;

/**
 * Interface for bag implementations that are guaranteed to never change. 
 *
 * @param <E> the element type
 */
@Immutable
public interface ImmutableBag<E> extends ImmutableCollection<E>, Bag<E> {
    
}
