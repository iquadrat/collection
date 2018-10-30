package org.povworld.collection.common;

import javax.annotation.CheckForNull;

import org.povworld.collection.Identificator;

public class IdentityIdentificator<T> implements Identificator<T> {
    
    private static final Identificator<?> INSTANCE = new IdentityIdentificator<>();
    
    @SuppressWarnings("unchecked")
    public static <T> Identificator<T> getInstance() {
        return (Identificator<T>)INSTANCE;
    }
    
    private IdentityIdentificator() {}
    
    @Override
    public boolean isIdentifiable(Object object) {
        return true;
    }
    
    @Override
    public boolean equals(T object1, T object2) {
        return object1 == object2;
    }
    
    // Methods inherited from Object
    
    @Override
    public boolean equals(@CheckForNull Object object) {
        return this == object;
    }
    
    @Override
    public int hashCode(T object) {
        return System.identityHashCode(object);
    }
    
}
