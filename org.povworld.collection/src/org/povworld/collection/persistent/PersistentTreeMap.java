package org.povworld.collection.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.Identificator;
import org.povworld.collection.Map;
import org.povworld.collection.common.AbstractMap;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.EntryValueIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.immutable.ImmutableCollection;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.immutable.ImmutableContainer;
import org.povworld.collection.tree.ImmutableTreeMapNode;
import org.povworld.collection.tree.Path;
import org.povworld.collection.tree.ReverseTreeIterator;
import org.povworld.collection.tree.TreeIterator;
import org.povworld.collection.tree.TreeUtil;

// TODO create PersistentMultiMap implementation based on tree which allows duplicate keys
public class PersistentTreeMap<K, V, N extends ImmutableTreeMapNode<K, V, N>> extends AbstractMap<K, V> implements PersistentMap<K, V> {
    
    public static <K, V, N extends ImmutableTreeMapNode<K, V, N>> Path<N> pathTo(N tree, K key, Comparator<? super K> comparator) {
        Path.Builder<N> builder = Path.newBuilder(tree, 12);
        N subTree = tree;
        while (subTree != null) {
            int cmp = comparator.compare(subTree.getKey(), key);
            boolean left;
            if (cmp > 0) {
                subTree = subTree.getLeft();
                left = true;
            } else if (cmp < 0) {
                subTree = subTree.getRight();
                left = false;
            } else { // cmp == 0
                break;
            }
            builder.append(left, subTree);
        }
        return builder.build();
    }
    
    public enum BalancerType {
        AVL(new AvlTreeMapBuilder<Object, Object>()),
        NON_BALANCED(new NonBalancingTreeMapBuilder<Object, Object>());
        
        private final TreeMapBuilder<?, ?, ?> builder;
        
        BalancerType(TreeMapBuilder<?, ?, ?> balancer) {
            this.builder = balancer;
        }
        
        @SuppressWarnings("unchecked")
        private <K, V, N extends ImmutableTreeMapNode<K, V, N>> TreeMapBuilder<K, V, N> get() {
            return (TreeMapBuilder<K, V, N>)builder;
        }
    }
    
    public static <K extends Comparable<K>, V> PersistentMap<K, V> empty(Class<K> keyClass) {
        return empty(keyClass, BalancerType.AVL);
    }
    
    public static <K extends Comparable<K>, V> PersistentMap<K, V> empty(Class<K> keyClass, BalancerType balancerType) {
        TreeMapBuilder<K, V, ?> balancer = balancerType.get();
        return empty(keyClass, balancer);
    }
    
    public static <K extends Comparable<K>, V> PersistentMap<K, V> empty(Class<K> keyClass, TreeMapBuilder<K, V, ?> balancer) {
        return new EmptyMap<>(CollectionUtil.getDefaultComparator(keyClass), balancer);
    }
    
    public static <K, V, N extends ImmutableTreeMapNode<K, V, N>> PersistentMap<K, V> empty(
            Comparator<? super K> keyComparator) {
        TreeMapBuilder<K, V, N> balancer = BalancerType.AVL.get();
        return empty(keyComparator, balancer);
    }
    
    public static <K, V> PersistentMap<K, V> empty(Comparator<? super K> keyComparator, TreeMapBuilder<K, V, ?> balancer) {
        return new EmptyMap<>(keyComparator, balancer);
    }
    
    private final Comparator<? super K> keyComparator;
    
    private final TreeMapBuilder<K, V, N> builder;
    
    private final N root;
    
    private final int size;
    
    private PersistentTreeMap(Comparator<? super K> keyComparator, N root, int size, TreeMapBuilder<K, V, N> balancer) {
        PreConditions.paramNotNull(root);
        this.keyComparator = keyComparator;
        this.root = root;
        this.size = size;
        this.builder = balancer;
    }
    
    @Override
    public final Identificator<? super K> getKeyIdentificator() {
        return keyComparator;
    }
    
    @Override
    // TODO implement customization of value identificator
    public final Identificator<? super V> getValueIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    public void checkInvariants() {
        builder.checkInvariants(root);
    }
    
    @Override
    public int keyCount() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public K getFirstKeyOrNull() {
        return TreeUtil.getMinNode(root).getKey();
    }
    
    @Override
    @CheckForNull
    public V get(K key) {
        N subTree = root;
        do {
            int cmp = keyComparator.compare(subTree.getKey(), key);
            if (cmp < 0) {
                subTree = subTree.getRight();
            } else if (cmp > 0) {
                subTree = subTree.getLeft();
            } else {
                return subTree.getValue();
            }
        } while (subTree != null);
        return null;
    }
    
    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }
    
    private static class MapEntryIterator<K, V, N extends ImmutableTreeMapNode<K, V, N>> implements EntryIterator<K, V> {
        
        private final Iterator<N> subTreeIterator;
        
        @CheckForNull
        private N current = null;
        
        public MapEntryIterator(@CheckForNull N root) {
            subTreeIterator = TreeUtil.iterateNodes(root);
        }
        
        @Override
        public boolean next() {
            if (!subTreeIterator.hasNext()) {
                current = null;
                return false;
            }
            current = subTreeIterator.next();
            return true;
        }
        
        @Override
        public K getCurrentKey() throws NoSuchElementException {
            if (current == null) throw new NoSuchElementException();
            return current.getKey();
        }
        
        @Override
        public V getCurrentValue() throws NoSuchElementException {
            if (current == null) throw new NoSuchElementException();
            return current.getValue();
        }
        
    }
    
    @Override
    public EntryIterator<K, V> entryIterator() {
        return new MapEntryIterator<K, V, N>(root);
    }
    
    private static class Keys<K, V, N extends ImmutableTreeMapNode<K, V, N>> extends AbstractOrderedCollection<K>
            implements ImmutableContainer<K> {
        
        // Invariant: map is never empty
        private final PersistentTreeMap<K, V, N> map;
        
        public Keys(PersistentTreeMap<K, V, N> map) {
            this.map = map;
        }
        
        @Override
        public Identificator<? super K> getIdentificator() {
            return map.getKeyIdentificator();
        }
        
        @Override
        public boolean contains(K key) {
            return map.containsKey(key);
        }
        
        @Override
        public K findEqualOrNull(K element) {
            return map.keys().findEqualOrNull(element);
        }
        
        @Override
        public int size() {
            return map.keyCount();
        }
        
        @Override
        @CheckForNull
        public K getFirstOrNull() {
            return TreeUtil.getMinNode(map.root).getKey();
        }
        
        @Override
        public K getLastOrNull() {
            return TreeUtil.getMaxNode(map.root).getKey();
        }
        
        @Override
        public Iterator<K> iterator() {
            return new TreeIterator<K, N>(map.root, TreeIterator.DEFAULT_HEIGHT) {
                @Override
                protected K getElement(N tree) {
                    return tree.getKey();
                }
            };
        }
        
        @Override
        public Iterator<K> reverseIterator() {
            return new ReverseTreeIterator<K, N>(map.root, TreeIterator.DEFAULT_HEIGHT) {
                @Override
                protected K getElement(N tree) {
                    return tree.getKey();
                }
            };
        }
    }
    
    @Override
    public ImmutableContainer<K> keys() {
        return new Keys<K, V, N>(this);
    }
    
    private static class Values<V> implements ImmutableCollection<V> {
        
        private final PersistentTreeMap<?, V, ?> map;
        
        Values(PersistentTreeMap<?, V, ?> map) {
            this.map = map;
        }
        
        @Override
        public V getFirstOrNull() {
            return getFirst();
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public int size() {
            return map.keyCount();
        }
        
        @Override
        public Iterator<V> iterator() {
            return new EntryValueIterator<V>(map.entryIterator());
        }
        
        @Override
        public V getFirst() throws NoSuchElementException {
            return TreeUtil.getMinNode(map.root).getValue();
        }
        
    }
    
    @Override
    public ImmutableCollection<V> values() {
        return new Values<V>(this);
    }
    
    private static class EmptyMap<K, V, N extends ImmutableTreeMapNode<K, V, N>> extends AbstractMap<K, V> implements PersistentMap<K, V> {
        
        private final Comparator<? super K> keyComparator;
        
        private final TreeMapBuilder<K, V, N> balancer;
        
        private EmptyMap(Comparator<? super K> keyComparator, TreeMapBuilder<K, V, N> balancer) {
            this.keyComparator = keyComparator;
            this.balancer = balancer;
        }
        
        @Override
        public int keyCount() {
            return 0;
        }
        
        @Override
        public boolean isEmpty() {
            return true;
        }
        
        @Override
        @CheckForNull
        public K getFirstKeyOrNull() {
            return null;
        }
        
        @Override
        public Identificator<? super K> getKeyIdentificator() {
            return keyComparator;
        }
        
        @Override
        // TODO pass on from outer class when implemented
        public Identificator<? super V> getValueIdentificator() {
            return CollectionUtil.getObjectIdentificator();
        }
        
        @Override
        @CheckForNull
        public V get(K key) {
            return null;
        }
        
        @Override
        public boolean containsKey(K key) {
            return false;
        }
        
        @Override
        public EntryIterator<K, V> entryIterator() {
            return new MapEntryIterator<K, V, N>(null);
        }
        
        @Override
        public ImmutableContainer<K> keys() {
            return PersistentTreeSet.empty(keyComparator);
        }
        
        @Override
        public ImmutableCollection<V> values() {
            return ImmutableCollections.listOf();
        }
        
        @Override
        public PersistentMap<K, V> with(K key, V value) {
            PreConditions.paramNotNull(key);
            PreConditions.paramNotNull(value);
            return new PersistentTreeMap<K, V, N>(keyComparator, balancer.createNode(null, null, key, value), 1, balancer);
        }
        
        @Override
        public PersistentMap<K, V> without(K key) {
            return this;
        }
        
        @Override
        public PersistentMap<K, V> withAll(Map<? extends K, ? extends V> map) {
            PersistentMap<K, V> result = null;
            EntryIterator<? extends K, ? extends V> iterator = map.entryIterator();
            while (iterator.next()) {
                if (result == null) {
                    result = this.with(iterator.getCurrentKey(), iterator.getCurrentValue());
                } else {
                    result = result.with(iterator.getCurrentKey(), iterator.getCurrentValue());
                }
            }
            return (result == null) ? this : result;
        }
        
        @Override
        public PersistentMap<K, V> cleared() {
            return this;
        }
    }
    
    @Override
    public PersistentTreeMap<K, V, N> with(K key, V value) {
        PreConditions.paramNotNull(value);
        // TODO replace keyComparator by nodeCmp?
        Path<N> path = PersistentTreeMap.pathTo(root, key, keyComparator);
        
        int newSize = size;
        N end = path.getEnd();
        
        N node;
        if (end != null) {
            // Key was found.
            if (getValueIdentificator().equals(end.getValue(), value)) {
                return this;
            }
            node = builder.createNode(end.getLeft(), end.getRight(), key, value);
        } else {
            // Key was not found.
            node = builder.createNode(null, null, key, value);
            newSize++;
        }
        
        N subTree = builder.replace(path, node);
        return new PersistentTreeMap<K, V, N>(keyComparator, subTree, newSize, builder);
    }
    
    @Override
    public PersistentMap<K, V> without(K key) {
        Path<N> path = PersistentTreeMap.pathTo(root, key, keyComparator);
        if (path.getEnd() == null) {
            // Key not found.
            return this;
        }
        N newRoot = builder.remove(path);
        if (newRoot == null) {
            return cleared();
        }
        return new PersistentTreeMap<K, V, N>(keyComparator, newRoot, size - 1, builder);
    }
    
    @Override
    public PersistentMap<K, V> withAll(Map<? extends K, ? extends V> map) {
        PersistentMap<K, V> result = this;
        EntryIterator<? extends K, ? extends V> iter = map.entryIterator();
        while (iter.next()) {
            result = result.with(iter.getCurrentKey(), iter.getCurrentValue());
        }
        return result;
    }
    
    @Override
    public PersistentMap<K, V> cleared() {
        return new EmptyMap<K, V, N>(keyComparator, builder);
    }
}
