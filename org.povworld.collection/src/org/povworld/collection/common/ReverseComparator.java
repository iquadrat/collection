package org.povworld.collection.common;

import javax.annotation.CheckForNull;

import org.povworld.collection.Comparator;

public class ReverseComparator<E> implements Comparator<E> {
    
    private final Comparator<? super E> baseComparator;
    
    public ReverseComparator(Comparator<? super E> baseComparator) {
        this.baseComparator = baseComparator;
    }
    
    @Override
    public int compare(E o1, E o2) {
        return baseComparator.compare(o2, o1);
    }
    
    @Override
    public boolean isIdentifiable(Object object) {
        return baseComparator.isIdentifiable(object);
    }
    
    @Override
    public boolean equals(E object1, E object2) {
        return baseComparator.equals(object1, object2);
    }
    
    @Override
    public int hashCode(E object) {
        return baseComparator.hashCode(object);
    }
    
    @Override
    public int hashCode() {
        return baseComparator.hashCode() + 31252977;
    }
    
    @Override
    public boolean equals(@CheckForNull Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ReverseComparator<?> other = (ReverseComparator<?>)obj;
        return baseComparator.equals(other.baseComparator);
    }
    
}
