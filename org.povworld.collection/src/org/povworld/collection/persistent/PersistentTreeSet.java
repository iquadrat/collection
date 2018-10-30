package org.povworld.collection.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.Comparator;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.EmptyIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.ImmutableTreeSetNode;
import org.povworld.collection.tree.Path;
import org.povworld.collection.tree.ReverseTreeIterator;
import org.povworld.collection.tree.TreeBuilder;
import org.povworld.collection.tree.TreeIterator;
import org.povworld.collection.tree.TreeUtil;

// TODO javadoc
public class PersistentTreeSet<E, N extends ImmutableTreeSetNode<E, N>> extends AbstractOrderedCollection<E> implements
        PersistentOrderedSet<E> {
    
    public enum BalancerType {
        AVL(new AvlTreeSetBuilder<Object>()),
        NON_BALANCED(new NonBalancingTreeSetBuilder<Object>());
        
        private final TreeSetBuilder<?, ?> builder;
        
        BalancerType(TreeSetBuilder<?, ?> balancer) {
            this.builder = balancer;
        }
        
        @SuppressWarnings("unchecked")
        private <E, N extends ImmutableTreeSetNode<E, N>> TreeSetBuilder<E, N> get() {
            return (TreeSetBuilder<E, N>)builder;
        }
    }
    
    public static <E> PersistentOrderedSet<E> empty(Comparator<? super E> comparator) {
        return empty(comparator, BalancerType.AVL);
    }
    
    public static <E> PersistentOrderedSet<E> empty(Comparator<? super E> comparator, BalancerType balancerType) {
        TreeSetBuilder<E, ?> balancer = balancerType.get();
        return empty(comparator, balancer);
    }
    
    public static <E> PersistentOrderedSet<E> empty(Comparator<? super E> comparator, TreeSetBuilder<E, ?> balancer) {
        return new EmptySet<>(comparator, balancer);
    }
    
    private static class EmptySet<E, N extends ImmutableTreeSetNode<E, N>> extends AbstractOrderedCollection<E> implements PersistentOrderedSet<E> {
        
        private final Comparator<? super E> comparator;
        
        private final TreeSetBuilder<E, N> treeBuilder;
        
        EmptySet(Comparator<? super E> comparator, TreeSetBuilder<E, N> treeBalancer) {
            this.treeBuilder = treeBalancer;
            this.comparator = comparator;
        }
        
        @Override
        public Identificator<? super E> getIdentificator() {
            return comparator;
        }
        
        @Override
        public int size() {
            return 0;
        }
        
        @Override
        @CheckForNull
        public E getFirstOrNull() {
            return null;
        }
        
        @Override
        public Iterator<E> iterator() {
            return EmptyIterator.getInstance();
        }
        
        @Override
        public boolean contains(E element) {
            return false;
        }
        
        @Override
        public E findEqualOrNull(E element) {
            return null;
        }
        
        @Override
        public Iterator<E> reverseIterator() {
            return EmptyIterator.getInstance();
        }
        
        @Override
        @CheckForNull
        public E getLastOrNull() {
            return null;
        }
        
        @Override
        public PersistentOrderedSet<E> with(E element) {
            return new PersistentTreeSet<>(treeBuilder, comparator, treeBuilder.createNode(element), 1);
        }
        
        @Override
        public PersistentOrderedSet<E> withAll(Collection<? extends E> elements) {
            PersistentOrderedSet<E> result = null;
            for (E element: elements) {
                if (result == null) {
                    result = this.with(element);
                } else {
                    result = result.with(element);
                }
            }
            return (result == null) ? this : result;
        }
        
        @Override
        public PersistentOrderedSet<E> without(E element) {
            return this;
        }
        
        @Override
        public PersistentOrderedSet<E> withoutAll(Collection<? extends E> elements) {
            return this;
        }
        
        @Override
        public PersistentOrderedSet<E> cleared() {
            return this;
        }
        
    }
    
    private final TreeSetBuilder<E, N> treeBuilder;
    
    private final Comparator<? super E> comparator;
    
    @CheckForNull
    private final N root;
    
    private final int size;
    
    private PersistentTreeSet(TreeSetBuilder<E, N> treeBalancer, Comparator<? super E> comparator, N root, int size) {
        PreConditions.paramNotNull(root);
        this.treeBuilder = treeBalancer;
        this.comparator = comparator;
        this.root = root;
        this.size = size;
    }
    
    @Override
    public boolean contains(E element) {
        return findEqualOrNull(element) != null;
    }
    
    @Override
    @CheckForNull
    public E findEqualOrNull(E element) {
        N subTree = root;
        do {
            int cmp = comparator.compare(subTree.getElement(), element);
            if (cmp < 0) {
                subTree = subTree.getRight();
            } else if (cmp > 0) {
                subTree = subTree.getLeft();
            } else {
                return subTree.getElement();
            }
        } while (subTree != null);
        return null;
    }
    
    /**
     * Checks the invariants of the tree using the {@link TreeBuilder}. This method is useful for tests only. 
     */
    public void checkInvariants() {
        TreeUtil.validateOrder(root, createNodeComparator());
        treeBuilder.checkInvariants(root);
    }
    
    private N createNode(E element) {
        return treeBuilder.createNode(element);
    }
    
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        return getFirst();
    }
    
    @Override
    public E getFirst() throws NoSuchElementException {
        return TreeUtil.getMinNode(root).getElement();
    }
    
    @Override
    public E getLastOrNull() {
        return getLast();
    }
    
    @Override
    public E getLast() throws NoSuchElementException {
        return TreeUtil.getMaxNode(root).getElement();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new TreeIterator<E, N>(root, TreeIterator.DEFAULT_HEIGHT) {
            
            @Override
            protected E getElement(N node) {
                return node.getElement();
            }
        };
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return comparator;
    }
    
    @Override
    public PersistentOrderedSet<E> with(E element) {
        PreConditions.paramNotNull(element);
        N node = createNode(element);
        Comparator<N> nodeComparator = createNodeComparator();
        Path<N> path = treeBuilder.pathTo(root, node, nodeComparator);
        if (path.getEnd() != null) {
            return this;
        }
        N newRoot = treeBuilder.replace(path, node);
        return new PersistentTreeSet<E, N>(treeBuilder, comparator, newRoot, size + 1);
    }
    
    private Comparator<N> createNodeComparator() {
        return new Comparator<N>() {
            @Override
            public boolean isIdentifiable(Object object) {
                return (object instanceof ImmutableTreeSetNode);
            }
            
            @Override
            public boolean equals(N node1, N node2) {
                return node1.getElement().equals(node2.getElement());
            }
            
            @Override
            public int hashCode(N node) {
                return node.getElement().hashCode();
            }
            
            @Override
            public int compare(N node1, N node2) {
                return comparator.compare(node1.getElement(), node2.getElement());
            }
        };
    }
    
    @Override
    public PersistentOrderedSet<E> without(E element) {
        PreConditions.paramNotNull(element);
        N node = createNode(element);
        Path<N> path = treeBuilder.pathTo(root, node, createNodeComparator());
        if (path.getEnd() == null) {
            // Element not found.
            return this;
        }
        N newRoot = treeBuilder.remove(path);
        if (newRoot == null) {
            return cleared();
        }
        return new PersistentTreeSet<E, N>(treeBuilder, comparator, newRoot, size - 1);
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ReverseTreeIterator<E, N>(root, TreeIterator.DEFAULT_HEIGHT) {
            
            @Override
            protected E getElement(N node) {
                return node.getElement();
            }
        };
    }
    
    @Override
    @CheckReturnValue
    public PersistentOrderedSet<E> withAll(Collection<? extends E> elements) {
        PersistentOrderedSet<E> result = this;
        for (E element: elements) {
            PreConditions.paramNotNull(element);
            result = result.with(element);
        }
        return result; // TODO make more efficient
    }
    
    @Override
    @CheckReturnValue
    public PersistentOrderedSet<E> withoutAll(Collection<? extends E> elements) {
        PersistentOrderedSet<E> result = this;
        for (E element: elements) {
            PreConditions.paramNotNull(element);
            result = result.without(element);
        }
        return result; // TODO make more efficient
    }
    
    @Override
    @CheckReturnValue
    public PersistentOrderedSet<E> cleared() {
        return new EmptySet<E, N>(comparator, treeBuilder);
    }
    
    public static <E> Builder<E> newBuilder(Comparator<? super E> comparator) {
        return new Builder<>(comparator);
    }
    
    public static <E> Builder<E> newBuilder(Comparator<? super E> comparator, BalancerType balancer) {
        return new Builder<>(comparator, balancer);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentOrderedSet<E>> {
        
        private PersistentOrderedSet<E> set;
        
        public Builder(Comparator<? super E> comparator) {
            this(comparator, BalancerType.AVL);
        }
        
        public Builder(Comparator<? super E> comparator, BalancerType balancer) {
            set = empty(comparator, balancer);
        }
        
        @Override
        protected void _add(E element) {
            set = set.with(element);
        }
        
        @Override
        protected PersistentOrderedSet<E> _createCollection() {
            return set;
        }
        
        @Override
        protected void _reset() {
            set = set.cleared();
        }
    }
    
}
