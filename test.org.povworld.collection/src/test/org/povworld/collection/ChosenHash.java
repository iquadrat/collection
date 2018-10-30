package test.org.povworld.collection;

import org.povworld.collection.common.ObjectUtil;

public class ChosenHash {
    
    private final String value;
    
    private final int hash;
    
    public ChosenHash(String value, int hash) {
        this.value = value;
        this.hash = hash;
    }
    
    @Override
    public boolean equals(Object object) {
        ChosenHash other = ObjectUtil.castOrNull(object, ChosenHash.class);
        if (other == null) return false;
        
        return value.equals(other.value) && hash == other.hash;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
    @Override
    public String toString() {
        return value + ":" + hash;
    }
    
}