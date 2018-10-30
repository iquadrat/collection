package org.povworld.collection.common;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;

@NotThreadSafe
public abstract class AbstractCollectionBuilder<E, C extends Collection<E>> implements CollectionBuilder<E, C> {
    
    private boolean unusable = false;
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public CollectionBuilder<E, C> add(E element) {
        checkUsable();
        _add(element);
        return this;
    }
    
    protected abstract void _add(E element);
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public CollectionBuilder<E, C> addAll(Iterable<? extends E> elements) {
        checkUsable();
        _addAll(elements);
        return this;
    }
    
    protected void _addAll(Iterable<? extends E> elements) {
        for (E element: elements) {
            _add(element);
        }
    }
    
    @Override
    @OverridingMethodsMustInvokeSuper
    public CollectionBuilder<E, C> reset() {
        _reset();
        unusable = false;
        return this;
    }
    
    protected abstract void _reset();
    
    @Override
    public final C build() {
        checkUsable();
        unusable = true;
        return _createCollection();
    }
    
    protected abstract C _createCollection();
    
    protected final void checkUsable() {
        if (unusable) {
            throw new IllegalStateException();
        }
    }
    
}
