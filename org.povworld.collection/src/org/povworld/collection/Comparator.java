package org.povworld.collection;

/**
 * Combines the standard Java comparator with {@link Identificator}.
 * <p>
 * Note that for two {@code Comparator}s to be equal, the {{@link #compare(Object, Object)} 
 * method has to return the same values for all pairs of objects which are identifiable 
 * by both {@Code Comparator}s.
 */
public interface Comparator<T> extends java.util.Comparator<T>, Identificator<T> {
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException if any of the given {@code Object}s is {@code null}
     */
    @Override
    public int compare(T object1, T object2);
    
}
