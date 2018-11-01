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
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AbstractAvlTreeNode;
import org.povworld.collection.tree.AvlTree;
import org.povworld.collection.tree.AvlTreeNode;
import org.povworld.collection.tree.NodeElementTransformer;
import org.povworld.collection.tree.ReverseTreeIterator;
import org.povworld.collection.tree.TreeIterator;
import org.povworld.collection.tree.TreeUtil;

/**
 * Map implementation that uses a AVL tree to keep the mappings sorted in the
 * order of the keys.
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class TreeMap<K, V> extends AbstractMap<K, V> {
    
    private static class TreeMapNode<K, V> extends AbstractAvlTreeNode<TreeMapNode<K, V>> {
        
        private final K key;
        
        private V value;
        
        TreeMapNode(K key, V value) {
            PreConditions.paramNotNull(value);
            this.key = key;
            this.value = value;
        }
        
        public K getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
        
        public void setValue(V value) {
            this.value = value;
        }
        
    }
    
    private static class NodeKeyTransformer<K, V> implements NodeElementTransformer<K, TreeMapNode<K, V>> {
        @Override
        public K getElement(TreeMapNode<K, V> node) {
            return node.getKey();
        }

        @Override
        public TreeMapNode<K, V> createNode(K element) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final NodeKeyTransformer<?, ?> TRANSFORMER = new NodeKeyTransformer<>();
    
    @SuppressWarnings("unchecked")
    private static <K,V> NodeKeyTransformer<K, V> getTransformer() {
        return (NodeKeyTransformer<K, V>)TRANSFORMER;
    }
     
    private final Comparator<? super K> keyComparator;
    private final AvlTree<K, TreeMapNode<K,V>> tree;

    private int entries = 0;
    
    public TreeMap(Comparator<? super K> keyComparator) {
        PreConditions.paramNotNull(keyComparator);
        this.keyComparator = keyComparator;
        this.tree = new AvlTree<>(keyComparator, getTransformer());
    }
    
    public static <K extends Comparable<K>, V> TreeMap<K, V> create(Class<K> comparable) {
        return new TreeMap<>(CollectionUtil.getDefaultComparator(comparable));
    }
    
    @Override
    public int keyCount() {
        return entries;
    }
    
    @Override
    public boolean isEmpty() {
        return entries == 0;
    }
    
    @Override
    public Identificator<? super K> getKeyIdentificator() {
        return keyComparator;
    }
    
    @Override
    public Identificator<? super V> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    @CheckForNull
    public V get(K key) {
        TreeMapNode<K, V> node = tree.find(key);
        return (node == null) ? null : node.getValue();
    }
    
    /**
     * Assigns the given {@code value} with the given {@code key}.
     *
     * @return the value previously assigned with {@code key} or null
     */
    @CheckForNull
    public V put(K key, V value) {
        PreConditions.paramNotNull(value);
        TreeMapNode<K, V> node = tree.find(key);
        if (node != null) {
            V oldValue = node.getValue();
            node.setValue(value);
            return oldValue;
        }
        // TODO goes through tree twice
        tree.insert(new TreeMapNode<>(key, value));
        entries++;
        return null;
    }
    
    public void putAll(Map<? extends K, ? extends V> entries) {
        EntryIterator<? extends K, ? extends V> iterator = entries.entryIterator();
        while (iterator.next()) {
            put(iterator.getCurrentKey(), iterator.getCurrentValue());
        }
    }
    
    /**
     * Removes the mapping for the given {@code key}.
     *
     * @return the value previously assigned with {@code key} or null
     */
    @CheckForNull
    public V remove(K key) {
        TreeMapNode<K, V> removed = tree.remove(key);
        if (removed == null) {
            return null;
        }
        entries--;
        return removed.getValue();
    }
    
    /**
     * Removes all mappings.
     */
    public void clear() {
        tree.clear();
        entries = 0;
    }
    
    @Override
    public boolean containsKey(K key) {
        return tree.find(key) != null;
    }
    
    @Override
    public OrderedSet<K> keys() {
        return new Keys();
    }
    
    @CheckForNull
    public K getFirstKeyOrNull() {
        return tree.getMinElement();
    }
    
    private class Keys extends AbstractOrderedCollection<K> implements OrderedSet<K> {
        
        @Override
        public boolean contains(K element) {
            return containsKey(element);
        }
        
        @Override
        public K findEqualOrNull(K element) {
            TreeMapNode<K, V> node = tree.find(element);
            return node == null ? null : node.key;
        }
        
        @Override
        public int size() {
            return TreeMap.this.keyCount();
        }
        
        @Override
        @CheckForNull
        public K getFirstOrNull() {
            return getFirstKeyOrNull();
        }
        
        @Override
        @CheckForNull
        public K getLastOrNull() {
            return tree.getMaxElement();
        }
        
        @Override
        public Iterator<K> iterator() {
            return new TreeIterator<K, TreeMapNode<K, ?>>(tree.getRoot(), AvlTreeNode.getHeight(tree.getRoot())) {
                
                @Override
                protected K getElement(TreeMapNode<K, ?> node) {
                    return node.getKey();
                }
            };
        }
        
        @Override
        public Iterator<K> reverseIterator() {
            return new ReverseTreeIterator<K, TreeMapNode<K, ?>>(tree.getRoot(), AvlTreeNode.getHeight(tree.getRoot())) {
                
                @Override
                protected K getElement(TreeMapNode<K, ?> node) {
                    return node.getKey();
                }
            };
        }
    }
    
    @Override
    public Collection<V> values() {
        return new Values();
    }
    
    private class Values implements Collection<V> {
        
        @Override
        public int size() {
            return entries;
        }
        
        @Override
        public boolean isEmpty() {
            return TreeMap.this.isEmpty();
        }
        
        @Override
        @CheckForNull
        public V getFirstOrNull() {
            if (isEmpty()) {
                return null;
            }
            return TreeUtil.getMinNode(tree.getRoot()).getValue();
        }
        
        @Override
        public Iterator<V> iterator() {
            return new TreeIterator<V, TreeMapNode<?, V>>(tree.getRoot(), AvlTreeNode.getHeight(tree.getRoot())) {
                @Override
                protected V getElement(TreeMapNode<?, V> node) {
                    return node.getValue();
                }
            };
        }
    }
    
    @Override
    public EntryIterator<K, V> entryIterator() {
        final Iterator<TreeMapNode<K, V>> baseIterator = TreeUtil.iterateNodes(tree.getRoot());
        return new EntryIterator<K, V>() {
            
            @CheckForNull
            private TreeMapNode<K, V> current = null;
            
            @Override
            public boolean next() {
                if (!baseIterator.hasNext()) {
                    current = null;
                    return false;
                }
                current = baseIterator.next();
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
            public V getCurrentValue() throws NoSuchElementException {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                return current.getValue();
            }
        };
    }
}
