package org.povworld.collection.mutable;

import org.povworld.collection.Identificator;
import org.povworld.collection.common.IdentityIdentificator;

public class IdentityHashMap<K, V> extends HashMap<K, V> {
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return IdentityIdentificator.<K>getInstance();
    }
    
    @Override
    public Identificator<? super V> getValueIdentificator() {
        return IdentityIdentificator.<V>getInstance();
    }
}
