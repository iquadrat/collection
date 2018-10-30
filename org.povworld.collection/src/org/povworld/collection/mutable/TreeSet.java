package org.povworld.collection.mutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.Identificator;
import org.povworld.collection.OrderedSet;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AvlTree;
import org.povworld.collection.tree.ElementTreeNode;

/**
 * Set implementation that uses an AVL tree to store the elements in order.
 *
 * @param <E> element type
 */
public class TreeSet<E> extends AbstractOrderedCollection<E> implements OrderedSet<E> {
    
    private final Identificator<? super E> identificator;
    private final AvlTree<E, ElementTreeNode<E>> tree;
    
    private int size = 0;
    
    public TreeSet(Comparator<? super E> comparator) {
        this.tree = new AvlTree<>(comparator, ElementTreeNode.getTransformer());
        this.identificator = comparator;
    }
    
    public static <E extends Comparable<E>> TreeSet<E> create(Class<E> comparable) {
        return new TreeSet<E>(CollectionUtil.getDefaultComparator(comparable));
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return identificator;
    }
    
    @Override
    public boolean contains(E element) {
        return tree.find(element) != null;
    }
    
    @Override
    @CheckForNull
    public E findEqualOrNull(E element) {
        ElementTreeNode<E> node = tree.find(element);
        return (node == null) ? null : node.getElement();
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        return tree.getMinElement();
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        return tree.getMaxElement();
    }
    
    public boolean add(E element) {
        PreConditions.paramNotNull(element);
        if (!tree.insertIfNotPresent(element)) {
            return false;
        }
        size++;
        return true;
    }
    
    /**
     * Adds all {@code elements} to the set.
     * 
     * @return number of actual elements inserted
     */
    public int addAll(Iterable<E> elements) {
        int added = 0;
        for (E element: elements) {
            if (add(element)) {
                added++;
            }
        }
        return added;
    }
    
    /**
     * Removes the given {@code element} from the set.
     * @return true if the {@code element} was contained in the set
     */
    public boolean remove(E element) {
        PreConditions.paramNotNull(element);
        if (tree.remove(element) == null) {
            return false;
        }
        size--;
        return true;
    }
    
    /**
     * Removes all {@code elements} from the given set. 
     * @return number of actual elements removed
     */
    public int removeAll(Iterable<E> elements) {
        int removed = 0;
        for (E element: elements) {
            if (remove(element)) {
                removed++;
            }
        }
        return removed;
    }
    
    public void clear() {
        tree.clear();
        size = 0;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new ElementTreeNode.Iterator<>(tree.getRoot());
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ElementTreeNode.ReverseIterator<>(tree.getRoot());
    }
    
    public static <E> CollectionBuilder<E, TreeSet<E>> newBuilder(Comparator<? super E> comparator) {
        return new Builder<>(comparator);
    }
    
    public static <C extends Comparable<C>> CollectionBuilder<C, TreeSet<C>> newBuilder(Class<C> class_) {
        return new Builder<>(CollectionUtil.getDefaultComparator(class_));
    }
    
    @NotThreadSafe
    public static class Builder<E> extends AbstractCollectionBuilder<E, TreeSet<E>> {
        private final Comparator<? super E> comparator;
        
        @Nullable
        private TreeSet<E> treeSet;
        
        public Builder(Comparator<? super E> comparator) {
            this.comparator = comparator;
            _reset();
        }
        
        @Override
        protected void _add(E element) {
            treeSet.add(element);
        }
        
        @Override
        protected final void _reset() {
            this.treeSet = new TreeSet<E>(comparator);
        }
        
        @Override
        protected TreeSet<E> _createCollection() {
            TreeSet<E> collection = treeSet;
            treeSet = null;
            return collection;
        }
    }
    
}
