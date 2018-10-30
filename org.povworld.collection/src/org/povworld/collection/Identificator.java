package org.povworld.collection;

/**
 * Defines an identify relation between objects of some type.
 * <p>
 * It is optional to override {@link #equals(Object)} and {@link #hashCode()} for {@code Identificator}s. 
 * Two identificators are allowed to be equal if both identificators return the same result from 
 * {@code #equals(Object, Object)} for any pair of Objects {@code object1} and {@code object2} which 
 * are identifiable by both identificators (as indicated by {@link #isIdentifiable(Object)}). 
 * This means two equal identificators have only to agree on the equality for objects they can both 
 * identify. Note that in any case the general contracts of {@code Object#equals(Object)} have to 
 * be satisfied, i.e., the implementation has to be symmetric and must be consistent with the 
 * returned {@code #hashCode()}.
 * 
 * @see http://en.wikipedia.org/wiki/Equality_(mathematics)
 *
 * @param <T> the type for which the identify relation is defined
 */
public interface Identificator<T> {
    
    /**
     * Checks whether the this identificator can be used for the given {@code object}.  
     */
    public boolean isIdentifiable(Object object);
    
    /**
     * @return true if the given two objects are equal in the sense of the identify relation
     */
    public boolean equals(T object1, T object2);
    
    /**
     * Calculates a hash code for the given object. The hash code must be consistent with
     * the values returned by {@link #equals(Object,Object)}, i.e., for any two objects {@code o1} and {@code o2}
     * such that {@code equals(o1, o2) == true} then it must hold that {@code hashCode(o1) == hashCode(o2)}.
     * (Note that this requirement is not necessary the other way around.) Further, hash codes should be spread
     * as uniquely as possible among the range of the integer value.
     * 
     * @return the hash code of the given object
     */
    public int hashCode(T object);

}
