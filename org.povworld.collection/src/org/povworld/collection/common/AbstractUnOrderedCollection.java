package org.povworld.collection.common;

import java.util.Set;

import javax.annotation.CheckForNull;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.UnOrderedCollection;

/**
 * Implements {@link #hashCode()} and {@link #equals(Object)} as specified by {@link UnOrderedCollection}.
 * 
 * @author micha
 *
 * @param <E>
 */
public abstract class AbstractUnOrderedCollection<E> implements UnOrderedCollection<E> {
    
    /**
     * This is the default implementation for all collections which are {@link Set}s. {@link UnOrderedCollection}s
     * which allow duplicate elements need to override this method!
     */
    @Override
    public int getCount(E element) {
        return contains(element) ? 1 : 0;
    }
    
    @Override
    public boolean equals(@CheckForNull Object object) {
        if (this == object) return true;
        
        UnOrderedCollection<?> other = ObjectUtil.castOrNull(object, UnOrderedCollection.class);
        if (other == null || !getIdentificator().equals(other.getIdentificator())) return false;
        if (other.size() != size()) return false;
        
        return compareElementCounts(other);
    }
    
    private <F> boolean compareElementCounts(UnOrderedCollection<F> other) {
        for (F elementOther: other) {
            // As both collections have the same identificator, it is safe to assume that our collection can deal
            // with all the elements contained in the other collection.
            // TODO use isComparable check?
            // TODO write a test which implements a Collection for a specific type only
            @SuppressWarnings("unchecked")
            E element = (E)elementOther;
            if (getCount(element) != other.getCount(elementOther)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        final Identificator<? super E> identificator = getIdentificator();
        int result = 0;
        for (E element: this) {
            result += identificator.hashCode(element);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return CollectionUtil.toSetString(this);
    }
    
}
