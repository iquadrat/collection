package org.povworld.collection.persistent;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

import org.povworld.collection.tree.AbstractAvlTreeBuilder;
import org.povworld.collection.tree.AbstractImmutableAvlTreeNode;
import org.povworld.collection.tree.ImmutableTreeMapNode;
import org.povworld.collection.tree.ImmutableTreeNode;

/**
 * Persistent tree map balancer that uses AVL tree nodes.
 * 
 * @param <K> the map's key type
 * @param <V> the map's value type
 */
class AvlTreeMapBuilder<K, V> extends AbstractAvlTreeBuilder<AvlTreeMapBuilder.AvlTreeMapNode<K, V>> implements
        TreeMapBuilder<K, V, AvlTreeMapBuilder.AvlTreeMapNode<K, V>> {
    
    @Override
    public AvlTreeMapNode<K, V> createSubTree(AvlTreeMapNode<K, V> left,
            AvlTreeMapNode<K, V> top, AvlTreeMapNode<K, V> right) {
        return createNode(left, right, top.key, top.value);
    }
    
    @Override
    public AvlTreeMapNode<K, V> createNode(AvlTreeMapNode<K, V> left, AvlTreeMapNode<K, V> right, K key, V value) {
        return new AvlTreeMapNode<K, V>(key, value, left, right);
    }
    
    /**
     * AVL implementation of {@link ImmutableTreeNode} that has a key and a value in each node.
     */
    @Immutable
    static class AvlTreeMapNode<K, V> extends AbstractImmutableAvlTreeNode<AvlTreeMapNode<K, V>> implements
            ImmutableTreeMapNode<K, V, AvlTreeMapNode<K, V>> {
        
        private final K key;
        
        private final V value;
        
        private AvlTreeMapNode(K key, V value, @CheckForNull AvlTreeMapNode<K, V> left, @CheckForNull AvlTreeMapNode<K, V> right) {
            super(left, right);
            this.key = key;
            this.value = value;
        }
        
        @Override
        public AvlTreeMapNode<K, V> getLeft() {
            return left;
        }
        
        @Override
        public AvlTreeMapNode<K, V> getRight() {
            return right;
        }
        
        @Override
        public int getHeight() {
            return height;
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
