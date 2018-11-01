package org.povworld.collection.common;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.OrderedCollection;

/**
 * Implements {@link #hashCode()} and {@link #equals(Object)} as specified by {@link OrderedCollection}.
 * 
 * @author micha
 *
 * @param <E>
 */
public abstract class AbstractOrderedCollection<E> implements OrderedCollection<E> {
    
    @Override
    public boolean equals(@CheckForNull Object o) {
        if (this == o) return true;
        
        OrderedCollection<?> other = ObjectUtil.castOrNull(o, OrderedCollection.class);
        if (other == null) return false;
        
        if (!other.getIdentificator().equals(getIdentificator())) return false;
        
        int size = size();
        if (size != other.size()) return false;
        
        Iterator<E> it1 = iterator();
        Iterator<?> it2 = other.iterator();
        
        for (int i = 0; i < size; ++i) {
            if (!it1.next().equals(it2.next())) return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final Identificator<? super E> identificator = getIdentificator();
        int hashCode = 1;
        for (E element: this) {
            hashCode = 31 * hashCode + identificator.hashCode(element);
        }
        return hashCode;
    }
    
    @Override
    public String toString() {
        return CollectionUtil.toListString(this);
    }
    
}
