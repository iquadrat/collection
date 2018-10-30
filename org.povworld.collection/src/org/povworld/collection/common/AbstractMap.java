package org.povworld.collection.common;

import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;

public abstract class AbstractMap<K, V> implements Map<K, V> {
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        Map<?, ?> other = ObjectUtil.castOrNull(obj, Map.class);
        if (other == null) {
            return false;
        }
        
        final Identificator<? super K> keyIdentificator = getKeyIdentificator();
        final Identificator<? super V> valueIdentificator = getValueIdentificator();
        if (!keyIdentificator.equals(other.getKeyIdentificator())) {
            return false;
        }
        if (!valueIdentificator.equals(other.getValueIdentificator())) {
            return false;
        }
        
        if (keyCount() != other.keyCount()) {
            return false;
        }
        
        EntryIterator<?, ?> iterator = other.entryIterator();
        while (iterator.next()) {
            Object key = iterator.getCurrentKey();
            if (!keyIdentificator.isIdentifiable(key)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            V otherValue = (V)iterator.getCurrentValue();
            if (!valueIdentificator.isIdentifiable(otherValue)) {
                return false;
            }
            // As the keyIdentificator says that the given key object is identifable we can safely assume that
            // the get operation with that key will work.
            @SuppressWarnings("unchecked")
            V myValue = get((K)key);
            if (myValue == null || !valueIdentificator.equals(myValue, otherValue)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hashcode = -1;
        EntryIterator<K, ? extends V> iter = entryIterator();
        while (iter.next()) {
            int pairHash =
                    getKeyIdentificator().hashCode(iter.getCurrentKey()) +
                            255 * getValueIdentificator().hashCode(iter.getCurrentValue());
            hashcode += pairHash;
        }
        return hashcode;
    }
    
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        EntryIterator<K, ? extends V> iter = entryIterator();
        while (iter.next()) {
            sb.append(iter.getCurrentKey() + "=" + iter.getCurrentValue() + ", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append('}');
        return sb.toString();
    }
    
}
