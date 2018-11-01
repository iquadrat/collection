package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.Sequence;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.PreConditions;

/**
 * Map that keeps insertion order of values.
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
@NotThreadSafe
public class OrderedHashMap<K, V> extends AbstractMap<K, V> {
    
    private static final int DEFAULT_INITIAL_SIZE = 12;
    
    private static final class Link<K, V> extends AbstractIntrusiveLinkedSequence.AbstractLink<Link<K, V>> {
        private final K key;
        
        private V value;
        
        Link(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private final HashMap<K, Link<K, V>> map;
    
    private IntrusiveLinkedSequence<Link<K, V>> linkedList;
    
    public OrderedHashMap() {
        this(DEFAULT_INITIAL_SIZE);
    }
    
    public OrderedHashMap(int initialSize) {
        linkedList = new IntrusiveLinkedSequence<>();
        map = new HashMap<>(initialSize);
    }
    
    public void put(K key, V value) {
        PreConditions.paramNotNull(value);
        Link<K, V> link = map.get(key);
        if (link != null) {
            link.value = value;
        } else {
            link = new Link<>(key, value);
            map.put(key, link);
            linkedList.insertBack(link);
        }
    }
    
    /**
     * @return the value to which this map maps the specified key, or
     *         {@code null} if the map contains no mapping for this key.
     */
    @Override
    @CheckForNull
    public V get(K key) {
        Link<K, V> link = map.get(key);
        return (link == null) ? null : link.value;
    }
    
    /**
     * @return The number of elements in this map.
     */
    @Override
    public int keyCount() {
        return linkedList.size();
    }
    
    /**
     * For test-purposes only.
     */
    public void testInvariants() {
        Assert.assertTrue(linkedList.size() == map.keyCount(), "Map inconsistent: " + linkedList.size() + "!=" + map.keyCount());
    }
    
    /**
     * @return iterator over the keys in insertion order
     */
    public Iterable<K> keyIterable() {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new KeyIterator(linkedList.iterator());
            }
        };
    }
    
    /**
     * @return iterator over the keys in reverse insertion order
     */
    public Iterable<K> reverseKeyIterable() {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new KeyIterator(linkedList.reverseIterator());
            }
        };
    }
    
    /**
     * @return iterator over the values in insertion order
     */
    public Iterable<V> valueIterable() {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new ValueIterator(linkedList.iterator());
            }
        };
    }
    
    /**
     * @return iterator over the values in reverse insertion order
     */
    public Iterable<V> reverseValueIterable() {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new ValueIterator(linkedList.reverseIterator());
            }
        };
    }
    
    @Override
    public EntryIterator<K, V> entryIterator() {
        return new MapEntryIterator(linkedList.iterator());
    }
    
    private abstract class AbstractIterator {
        
        private final Iterator<Link<K, V>> linkIterator;
        
        @CheckForNull
        private Link<K, V> current = null;
        
        AbstractIterator(Iterator<Link<K, V>> linkIterator) {
            this.linkIterator = linkIterator;
        }
        
        public boolean hasNext() {
            return linkIterator.hasNext();
        }
        
        protected Link<K, V> nextLink() {
            current = linkIterator.next();
            return current;
        }
        
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            map.remove(current.key);
            linkedList.remove(current);
            current = null;
        }
    }
    
    private class KeyIterator extends AbstractIterator implements Iterator<K> {
        KeyIterator(Iterator<Link<K, V>> linkIterator) {
            super(linkIterator);
        }
        
        @Override
        public K next() {
            return nextLink().key;
        }
    }
    
    private class ValueIterator extends AbstractIterator implements Iterator<V> {
        ValueIterator(Iterator<Link<K, V>> linkIterator) {
            super(linkIterator);
        }
        
        @Override
        public V next() {
            return nextLink().value;
        }
    }
    
    private class MapEntryIterator extends AbstractIterator implements EntryIterator<K, V> {
        @CheckForNull
        private Link<K, V> current = null;
        
        MapEntryIterator(Iterator<Link<K, V>> linkIterator) {
            super(linkIterator);
        }
        
        @Override
        public boolean next() {
            if (!hasNext()) {
                current = null;
                return false;
            }
            current = nextLink();
            return true;
        }
        
        @Override
        public K getCurrentKey() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.key;
        }
        
        @Override
        public V getCurrentValue() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.value;
        }
    }
    
    @Override
    public Sequence<V> values() {
        return new Values();
    }
    
    private class Values extends AbstractOrderedCollection<V> implements Sequence<V> {
        @Override
        public int size() {
            return linkedList.size();
        }
        
        @Override
        public boolean isEmpty() {
            return linkedList.isEmpty();
        }
        
        @Override
        @CheckForNull
        public V getFirstOrNull() {
            Link<K, V> first = linkedList.getFirstOrNull();
            return first == null ? null : first.value;
        }
        
        @Override
        public Iterator<V> iterator() {
            return new ValueIterator(linkedList.iterator());
        }
        
        @Override
        public Iterator<V> reverseIterator() {
            return new ValueIterator(linkedList.reverseIterator());
        }
        
        @Override
        public V getLastOrNull() {
            Link<K, V> last = linkedList.getLastOrNull();
            return last == null ? null : last.value;
        }
    }
    
    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
    
    @Override
    public OrderedSet<K> keys() {
        return new Keys();
    }
    
    private class Keys extends AbstractOrderedCollection<K> implements OrderedSet<K> {
        @Override
        public int size() {
            return linkedList.size();
        }
        
        @Override
        public boolean isEmpty() {
            return linkedList.isEmpty();
        }
        
        @Override
        @CheckForNull
        public K getFirstOrNull() {
            Link<K, ?> first = linkedList.getFirstOrNull();
            return first == null ? null : first.key;
        }
        
        @Override
        public Iterator<K> iterator() {
            return new KeyIterator(linkedList.iterator());
        }
        
        @Override
        public Iterator<K> reverseIterator() {
            return new KeyIterator(linkedList.reverseIterator());
        }
        
        @Override
        public K getLastOrNull() {
            Link<K, ?> last = linkedList.getLastOrNull();
            return last == null ? null : last.key;
        }
        
        @Override
        public boolean contains(K element) {
            return map.containsKey(element);
        }
        
        @Override
        public K findEqualOrNull(K element) {
            return map.findEqualKeyOrNull(element);
        }
    }
    
    @CheckForNull
    public K getFirstKeyOrNull() {
        return map.getFirstKeyOrNull();
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public void clear() {
        map.clear();
        linkedList = new IntrusiveLinkedSequence<>();
    }
    
    @Override
    // TODO implementation of key/value identificator customization
    public final Identificator<? super K> getKeyIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    public final Identificator<? super V> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
}
