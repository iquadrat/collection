package org.povworld.collection.tree;

import javax.annotation.CheckForNull;

import org.povworld.collection.Comparator;

/**
 * A tree with immutable nodes where on each update a new root is created leaving the
 * existing nodes unchanged.
 * 
 * @param <N> the node type
 */
// TODO remove?
public class PersistentTree<N extends ImmutableTreeNode<N>> {
    
    @CheckForNull
    private final N root;
    
    private final int size;
    
    private final TreeBuilder<N> treeBuilder;
    
    private final Comparator<? super N> comparator;
    
    public PersistentTree(TreeBuilder<N> treeManager, Comparator<? super N> comparator) {
        this(treeManager, comparator, null, 0);
    }
    
    private PersistentTree(TreeBuilder<N> treeManager, Comparator<? super N> comparator, @CheckForNull N root, int size) {
        this.treeBuilder = treeManager;
        this.comparator = comparator;
        this.root = root;
        this.size = size;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public N getRoot() {
        return root;
    }
    
    @CheckForNull
    public N getNode(N node) {
        if (isEmpty()) {
            return null;
        }
        return TreeUtil.findNode(root, node, comparator);
    }
    
    public PersistentTree<N> insert(N node) {
        Path<N> path = treeBuilder.pathTo(root, node, comparator);
        N newRoot = treeBuilder.replace(path, node); // TODO should be insert
        return new PersistentTree<>(treeBuilder, comparator, newRoot, size + 1);
    }
    
    public PersistentTree<N> replaceOrInsert(N node) {
        Path<N> path = treeBuilder.pathTo(root, node, comparator);
        N newRoot = treeBuilder.replace(path, node);
        return new PersistentTree<>(treeBuilder, comparator, newRoot, size + (path.getEnd() == null ? 1 : 0));
    }
    
    public PersistentTree<N> remove(N node) {
        Path<N> path = treeBuilder.pathTo(root, node, comparator);
        if (path.getEnd() == null) {
            return this;
        }
        N newRoot = treeBuilder.remove(path);
        return new PersistentTree<N>(treeBuilder, comparator, newRoot, size - 1);
    }
    
    public PersistentTree<N> clear() {
        return new PersistentTree<>(treeBuilder, comparator, null, 0);
    }
    
    public void checkInvariants() {
        treeBuilder.checkInvariants(root);
    }
}
