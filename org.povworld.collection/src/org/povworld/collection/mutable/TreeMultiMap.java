package org.povworld.collection.mutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.MultiMap;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.AbstractMultiMap;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.EntryKeyIterator;
import org.povworld.collection.common.EntryValueIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.tree.AbstractAvlTreeNode;
import org.povworld.collection.tree.AvlTree;
import org.povworld.collection.tree.MutableAvlTreeNode;
import org.povworld.collection.tree.TreeUtil;

/**
 * Map implementation that uses a AVL tree to keep the mappings sorted in the
 * order of the keys. Allows duplicate keys.
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class TreeMultiMap<K, V> extends AbstractMap<K, Set<V>> implements MultiMap<K, V> {
    
    protected interface Node<K, V> extends MutableAvlTreeNode<Node<K, V>> {
        
        K getKey();
        
        V getValue();
    }
    
    private static class NodeImpl<K, V> extends AbstractAvlTreeNode<Node<K, V>> implements Node<K, V> {
        
        private final K key;
        
        private V value;
        
        NodeImpl(K key, V value) {
            PreConditions.paramNotNull(value);
            this.key = key;
            this.value = value;
        }
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public V getValue() {
            return value;
        }
    }
    
    private static class KeyComparator<K, V> implements Comparator<Node<K, V>> {
        
        private final Comparator<? super K> comparator;
        
        KeyComparator(Comparator<? super K> keyComparator) {
            this.comparator = keyComparator;
        }
        
        @Override
        public boolean equals(Node<K, V> node1, Node<K, V> node2) {
            return compare(node1, node2) == 0;
        }
        
        @Override
        public int hashCode(Node<K, V> node) {
            return node.getKey().hashCode();
        }
        
        @Override
        public int compare(Node<K, V> node1, Node<K, V> node2) {
            return comparator.compare(node1.getKey(), node2.getKey());
        }
        
        @Override
        public boolean isIdentifiable(Object object) {
            return object instanceof Node;
        }
    }
    
    private class KeyValueComparator implements Comparator<Node<K, V>> {
        @Override
        public boolean isIdentifiable(Object object) {
            return object instanceof Node;
        }
        
        @Override
        public boolean equals(Node<K, V> node1, Node<K, V> node2) {
            return compare(node1, node2) == 0;
        }
        
        @Override
        public int hashCode(Node<K, V> node) {
            return node.getKey().hashCode() + 31 * node.getValue().hashCode();
        }
        
        @Override
        public int compare(Node<K, V> node1, Node<K, V> node2) {
            int cmp = keyComparator.comparator.compare(node1.getKey(), node2.getKey());
            if (cmp != 0) {
                return cmp;
            }
            return valueComparator.compare(node1.getValue(), node2.getValue());
        }
    }
    
    private final KeyComparator<K, V> keyComparator;
    private final Comparator<? super V> valueComparator;
    private final AvlTree<Node<K, V>, Node<K, V>> tree;
    
    private int keyCount = 0;
    private int valueCount = 0;
    
    public TreeMultiMap(Comparator<? super K> keyComparator, Comparator<? super V> valueComparator) {
        this.keyComparator = new KeyComparator<>(keyComparator);
        this.valueComparator = PreConditions.paramNotNull(valueComparator);
        this.tree = AvlTree.create(new KeyValueComparator());
    }
    
    public static <K extends Comparable<K>, V extends Comparable<V>> TreeMultiMap<K, V> create(Class<K> keys, Class<V> values) {
        return new TreeMultiMap<>(CollectionUtil.getDefaultComparator(keys), CollectionUtil.getDefaultComparator(values));
    }
    
    @Override
    public int keyCount() {
        return keyCount;
    }
    
    public int valueCount() {
        return valueCount;
    }
    
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return keyComparator.comparator;
    }
    
    @Override
    public Identificator<? super Set<V>> getValueIdentificator() {
        return new AbstractMultiMap.SetIdentificator<>(valueComparator);
    }
    
    protected static class KeySearchNode<K, V> extends AbstractAvlTreeNode<Node<K, V>> implements Node<K, V> {
        
        final K key;
        
        KeySearchNode(K key) {
            this.key = key;
        }
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public V getValue() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @CheckForNull
    public K getFirstKeyOrNull() {
        if (isEmpty()) {
            return null;
        }
        return tree.getMinElement().getKey();
    }
    
    @Override
    public Set<V> get(K key) {
        if (isEmpty()) {
            return ImmutableCollections.setOf();
        }
        KeySearchNode<K, V> node = new KeySearchNode<K, V>(key);
        Node<K, V> top = tree.find(node, keyComparator);
        if (top == null) {
            return ImmutableCollections.setOf();
        }
        return new ValueSet(top);
    }
    
    @Override
    public boolean containsKey(K key) {
        return tree.find(new KeySearchNode<K, V>(key), keyComparator) != null;
    }
    
    @Override
    public boolean contains(K key, V value) {
        return tree.find(new NodeImpl<>(key, value)) != null;
    }
    
    @Override
    public int numberOfValues(K key) {
        // TODO optimize
        return get(key).size();
    }
    
    private class ValueSet extends AbstractUnOrderedCollection<V> implements Set<V> {
        private final Node<K, V> top;
        private int size = -1;
        
        public ValueSet(Node<K, V> top) {
            this.top = top;
        }
        
        private K getKey() {
            return top.getKey();
        }
        
        @Override
        public boolean contains(V value) {
            // TODO we can just start at the first node in the path that equals to value
            return TreeMultiMap.this.contains(getKey(), value);
        }
        
        @Override
        @CheckForNull
        public V findEqualOrNull(V element) {
            Node<K, V> node = tree.find(new NodeImpl<>(getKey(), element));
            return (node == null) ? null : node.getValue();
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public int size() {
            if (size == -1) {
                size = CollectionUtil.sizeOf(this);
            }
            return size;
        }
        
        @Override
        public V getFirstOrNull() {
            return top.getValue();
        }
        
        @Override
        public Iterator<V> iterator() {
            final Iterator<Node<K, V>> iterator = tree.findAll(top, keyComparator);
            return new Iterator<V>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }
                
                @Override
                public V next() {
                    return iterator.next().getValue();
                }
            };
        }
    }
    
    /**
     * Adds the given {@code value} to the values associated with the given {@code key}.
     * Does nothing if the given key-value pair is already contained.
     *
     * @return true if the collection has changed
     */
    public boolean put(K key, V value) {
        PreConditions.paramNotNull(value);
        // TODO goes through tree twice
        if (!containsKey(key)) {
            keyCount++;
        }
        boolean inserted = tree.insertIfNotPresent(new NodeImpl<>(key, value));
        if (inserted) {
            valueCount++;
        }
        return inserted;
    }
    
    /**
     * Adds all given {@code values} to the values associated with the given {@code key}.
     */
    public void putAll(K key, Collection<? extends V> values) {
        for (V value: values) {
            put(key, value);
        }
    }
    
    public void putAll(Map<? extends K, ? extends V> entries) {
        EntryIterator<? extends K, ? extends V> iterator = entries.entryIterator();
        while (iterator.next()) {
            put(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
    }
    
    /**
     * Removes all values associated with the given key.
     * @return the number of values removed
     */
    public int remove(K key) {
        if (isEmpty()) {
            return 0;
        }
        // TODO optimize, is calling tree.findAll and removing those nodes faster?
        KeySearchNode<K, V> searchNode = new KeySearchNode<K, V>(key);
        Node<K,V> node;
        int removed = 0;
        while((node = tree.find(searchNode, keyComparator)) != null) {
            tree.remove(node);
            removed++;
        };
        if (removed > 0) {
            keyCount--;
        }
        valueCount -= removed;
        return removed;
    }
    
    /**
     * Removes the first occurrence of given value from the values associated with given key.
     * 
     * @return true if the collection has changed.
     */
    public boolean remove(K key, V value) {
        if (tree.remove(new NodeImpl<>(key, value)) == null) {
            return false;
        }
        // TODO this goes through tree twice
        if (!containsKey(key)) {
            keyCount--;
        }
        valueCount--;
        return true;
    }
    
    /**
     * Removes all values from the list associated with the given key.
     * 
     * @param key the key whose values are removed
     * @param values the values to remove
     * @return true the number of values removed
     */
    public int removeAll(K key, Iterable<? extends V> values) {
        int removed = 0;
        for (V value: values) {
            if (remove(key, value)) {
                removed++;
            }
        }
        return removed;
    }
    
    /**
     * Removes all mappings.
     */
    public void clear() {
        tree.clear();
        keyCount = 0;
        valueCount = 0;
    }
    
    @Override
    public OrderedSet<K> keys() {
        return new Keys();
    }
    
    private class Keys extends AbstractOrderedCollection<K> implements OrderedSet<K> {
        
        @Override
        public boolean contains(K element) {
            return containsKey(element);
        }
        
        @Override
        public K findEqualOrNull(K element) {
            Node<K, V> node = tree.find(new KeySearchNode<K, V>(element), keyComparator);
            return (node == null) ? null : node.getKey();
        }
        
        @Override
        public int size() {
            return TreeMultiMap.this.keyCount();
        }
        
        @Override
        @CheckForNull
        public K getFirstOrNull() {
            return getFirstKeyOrNull();
        }
        
        @Override
        @CheckForNull
        public K getLastOrNull() {
            if (isEmpty()) {
                return null;
            }
            return tree.getMaxElement().getKey();
        }
        
        @Override
        public Iterator<K> iterator() {
            return new EntryKeyIterator<>(entryIterator());
        }
        
        @Override
        public Iterator<K> reverseIterator() {
            return new EntryKeyIterator<>(new MultiMapEntryIterator(
                    TreeUtil.reverseIterateNodes(tree.getRoot())));
        }
    }
    
    @Override
    public Collection<Set<V>> values() {
        return new Values();
    }
    
    public Iterable<V> flatValues() {
        // TODO cache instances of Values and FlatValues?
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new FlatValueIterator();
            }
        };
    }
    
    private class FlatValueIterator implements Iterator<V> {
        final Iterator<Node<K, V>> nodeIterator = TreeUtil.iterateNodes(tree.getRoot());
        
        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }
        
        @Override
        public V next() {
            return nodeIterator.next().getValue();
        }
    }
    
    private class Values implements Collection<Set<V>> {
        
        @Override
        public int size() {
            return TreeMultiMap.this.keyCount();
        }
        
        @Override
        public boolean isEmpty() {
            return TreeMultiMap.this.isEmpty();
        }
        
        @Override
        @CheckForNull
        public Set<V> getFirstOrNull() {
            Node<K, V> min = tree.getMinElement();
            if (min == null) {
                return null;
            }
            // TODO this goes through twice
            Node<K, V> top = tree.find(min);
            return new ValueSet(top);
        }
        
        @Override
        public Iterator<Set<V>> iterator() {
            return new EntryValueIterator<>(entryIterator());
        }
    }
    
    private class MultiMapEntryIterator implements EntryIterator<K, Set<V>> {
        private final Iterator<Node<K, V>> baseIterator;
        
        @CheckForNull
        private K lastKey = null;
        
        @CheckForNull
        private Node<K, V> current = null;
        
        public MultiMapEntryIterator(Iterator<Node<K, V>> baseIterator) {
            this.baseIterator = baseIterator;
        }
        
        @Override
        public boolean next() {
            if (current != null) {
                lastKey = current.getKey();
            }
            do {
                if (!baseIterator.hasNext()) {
                    current = null;
                    lastKey = null;
                    return false;
                }
                current = baseIterator.next();
            } while (lastKey != null && keyComparator.comparator.equals(lastKey, current.getKey()));
            return true;
        }
        
        @Override
        public K getCurrentKey() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            return current.getKey();
        }
        
        @Override
        public Set<V> getCurrentValue() throws NoSuchElementException {
            if (current == null) {
                throw new NoSuchElementException();
            }
            // TODO optimize: get path from iterator
            return get(current.getKey());
        }
    }
    
    @Override
    public EntryIterator<K, Set<V>> entryIterator() {
        return new MultiMapEntryIterator(TreeUtil.iterateNodes(tree.getRoot()));
    }
}
