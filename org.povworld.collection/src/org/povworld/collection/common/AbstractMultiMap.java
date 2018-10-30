package org.povworld.collection.common;

import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Container;
import org.povworld.collection.Identificator;
import org.povworld.collection.List;
import org.povworld.collection.Map;
import org.povworld.collection.Set;

/**
 * Abstract implementation of a map that can associate multiple values with a single key.
 * 
 * @author micha
 *
 * @param <K> Type of the keys.
 * @param <V> Type of the values.
 */
@NotThreadSafe
public abstract class AbstractMultiMap<K, V, C extends Collection<V>> extends AbstractMap<K, C> {
    
    public static class ListIdentificator<V> implements Identificator<List<V>> {
        
        private final Identificator<? super V> identificator;
        
        public ListIdentificator(Identificator<? super V> identificator) {
            this.identificator = identificator;
        }
        
        public Identificator<? super V> getIdentificator() {
            return identificator;
        }
        
        @Override
        public boolean isIdentifiable(Object object) {
            return (object instanceof List);
        }
        
        @Override
        public boolean equals(List<V> list1, List<V> list2) {
            if (list1.size() != list2.size()) {
                return false;
            }
            return CollectionUtil.iteratesEqualSequence(list1, list2);
        }
        
        @Override
        public int hashCode(List<V> list) {
            int hashCode = 1;
            for (V value: list) {
                hashCode = 31 * hashCode + identificator.hashCode(value);
                
            }
            return hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            ListIdentificator<?> other = ObjectUtil.castOrNull(obj, ListIdentificator.class);
            if (other == null) {
                return false;
            }
            return other.identificator.equals(this.identificator);
        }
        
        @Override
        public int hashCode() {
            return identificator.hashCode() + 86393747;
        }
    }
    
    public static class SetIdentificator<V> implements Identificator<Set<V>> {
        
        private final Identificator<? super V> identificator;
        
        public SetIdentificator(Identificator<? super V> identificator) {
            this.identificator = identificator;
        }
        
        @Override
        public boolean isIdentifiable(Object object) {
            return (object instanceof Set);
        }
        
        @Override
        public boolean equals(Set<V> set1, Set<V> set2) {
            if (set1.size() != set2.size()) {
                return false;
            }
            return set1.containsAll(set2);
        }
        
        @Override
        public int hashCode(Set<V> set) {
            int hashCode = 0;
            for (V value: set) {
                hashCode += identificator.hashCode(value);
            }
            return hashCode;
        }
        
        @Override
        public boolean equals(Object obj) {
            SetIdentificator<?> other = ObjectUtil.castOrNull(obj, SetIdentificator.class);
            if (other == null) {
                return false;
            }
            return other.identificator.equals(this.identificator);
        }
        
        @Override
        public int hashCode() {
            return identificator.hashCode() + 389445959;
        }
    }
    
    /**
     * @return the map that is used to store the entries
     */
    protected abstract Map<K, ? extends C> getMap();
    
    @Override
    public C getOrDefault(K key, C defaultValues) {
        C values = getMap().get(key);
        return (values != null) ? values : defaultValues;
    }
    
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return getMap().getKeyIdentificator();
    }
    
    public boolean containsKey(K key) {
        return getMap().containsKey(key);
    }
    
    public int numberOfValues(K key) {
        Collection<V> values = getMap().get(key);
        return (values == null) ? 0 : values.size();
    }
    
    public boolean isEmpty() {
        return getMap().isEmpty();
    }
    
    public int keyCount() {
        return getMap().keyCount();
    }
    
    public Container<K> keys() {
        return getMap().keys();
    }
    
    public K getFirstKeyOrNull() {
        return getMap().getFirstKeyOrNull();
    }
    
    @Override
    public Collection<C> values() {
        @SuppressWarnings("unchecked")
        Collection<C> values = (Collection<C>)getMap().values();
        return values;
    }
}
