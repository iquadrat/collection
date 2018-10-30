package org.povworld.collection.immutable;

import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Container;

@Immutable
public interface ImmutableContainer<E> extends Container<E>, ImmutableCollection<E> {
    
}
