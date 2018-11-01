package org.povworld.collection.persistent;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.tree.AbstractTreeBuilder;
import org.povworld.collection.tree.ImmutableTreeMapNode;

/**
 * Persistent tree map balancer that does not do any balancing at all.
 * 
 * @param <K> the map's key type
 * @param <V> the map's value type
 */
class NonBalancingTreeMapBuilder<K, V> extends AbstractTreeBuilder<NonBalancingTreeMapBuilder.PlainTreeMapNode<K, V>>
        implements TreeMapBuilder<K, V, NonBalancingTreeMapBuilder.PlainTreeMapNode<K, V>> {
    
    private static final int ESTIMATED_PATH_LENGTH = 12;
    
    @Override
    protected int getEstimatedHeight(PlainTreeMapNode<K, V> node) {
        return ESTIMATED_PATH_LENGTH;
    }
    
    @Override
    public PlainTreeMapNode<K, V> createNode(@CheckForNull PlainTreeMapNode<K, V> left, @CheckForNull PlainTreeMapNode<K, V> right, K key, V value) {
        return new PlainTreeMapNode<K, V>(left, right, key, value);
    }
    
    @Override
    public PlainTreeMapNode<K, V> createSubTree(@CheckForNull PlainTreeMapNode<K, V> left, PlainTreeMapNode<K, V> top,
            @CheckForNull PlainTreeMapNode<K, V> right) {
        return createNode(left, right, top.key, top.value);
    }
    
    @Override
    public PlainTreeMapNode<K, V> balance(@CheckForNull PlainTreeMapNode<K, V> left, PlainTreeMapNode<K, V> top,
            @CheckForNull PlainTreeMapNode<K, V> right) {
        return createSubTree(left, top, right);
    }
    
    @Override
    public void checkInvariants(PlainTreeMapNode<K, V> tree) {
        // There are no balancing invariants.
    }
    
    @Immutable
    static class PlainTreeMapNode<K, V> implements ImmutableTreeMapNode<K, V, PlainTreeMapNode<K, V>> {
        
        @CheckForNull
        private final PlainTreeMapNode<K, V> left;
        
        @CheckForNull
        private final PlainTreeMapNode<K, V> right;
        
        private final K key;
        
        @CheckForNull
        private final V value;
        
        public PlainTreeMapNode(K key, @CheckForNull V value) {
            this(null, null, key, value);
        }
        
        public PlainTreeMapNode(@CheckForNull PlainTreeMapNode<K, V> left, @CheckForNull PlainTreeMapNode<K, V> right, K key, @CheckForNull V value) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.value = value;
        }
        
        @Override
        public PlainTreeMapNode<K, V> getLeft() {
            return left;
        }
        
        @Override
        public PlainTreeMapNode<K, V> getRight() {
            return right;
        }
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public V getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return "[" + left + "," + key + "=" + value + "," + right + "]";
        }
    }
}