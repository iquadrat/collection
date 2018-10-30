package org.povworld.collection.persistent;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.Collection;
import org.povworld.collection.immutable.ImmutableIndexedSet;

@Immutable
public interface PersistentIndexedSet<E> extends PersistentOrderedSet<E>, ImmutableIndexedSet<E> {
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> with(E element);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> withAll(Collection<? extends E> elements);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> without(E element);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> withoutAll(Collection<? extends E> elements);
    
    @Override
    @CheckReturnValue
    public PersistentIndexedSet<E> cleared();
    
}
