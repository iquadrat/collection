package org.povworld.collection.mutable;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Comparator;
import org.povworld.collection.Container;
import org.povworld.collection.Identificator;
import org.povworld.collection.Sequence;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AvlTree;
import org.povworld.collection.tree.ElementTreeNode;
import org.povworld.collection.tree.TreeUtil;

/**
 * A collection of naturally ordered elements that allows duplicates.
 *
 * @param <E> the element type
 */
public class TreeSequence<E> extends AbstractOrderedCollection<E> implements Sequence<E>, Container<E> {
    
    private final Identificator<? super E> identificator;
    private final AvlTree<E, ElementTreeNode<E>> tree;
    
    private int size = 0;
    
    public TreeSequence(Comparator<? super E> comparator) {
        this.tree = new AvlTree<E, ElementTreeNode<E>>(comparator, ElementTreeNode.getTransformer());
        this.identificator = comparator;
    }
    
    public static <E extends Comparable<E>> TreeSequence<E> create(Class<E> comparable) {
        return new TreeSequence<E>(CollectionUtil.<E>getDefaultComparator(comparable));
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return identificator;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean contains(E element) {
        return tree.find(element) != null;
    }
    
    @Override
    public E findEqualOrNull(E element) {
        ElementTreeNode<E> node = tree.find(element);
        return (node == null) ? null : node.getElement();
    }
    
    /**
     * @return occurrence count of the given element 
     */
    public int getCount(E element) {
        Iterator<ElementTreeNode<E>> iterator = tree.findAll(element);
        return CollectionUtil.remainingElementCount(iterator);
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        ElementTreeNode<E> min = TreeUtil.getMinNode(tree.getRoot());
        return (min == null) ? null : min.getElement();
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        ElementTreeNode<E> max = TreeUtil.getMaxNode(tree.getRoot());
        return (max == null) ? null : max.getElement();
    }
    
    public void add(E element) {
        PreConditions.paramNotNull(element);
        tree.insert(new ElementTreeNode<>(element));
        size++;
    }
    
    /**
     * Adds all {@code elements} to the sequence.
     */
    public void addAll(Iterable<E> elements) {
        for (E element: elements) {
            add(element);
        }
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
    
    public static <E> CollectionBuilder<E, TreeSequence<E>> newBuilder(Comparator<? super E> comparator) {
        return new Builder<>(comparator);
    }
    
    public static <C extends Comparable<C>> CollectionBuilder<C, TreeSequence<C>> newBuilder(Class<C> class_) {
        return new Builder<>(CollectionUtil.getDefaultComparator(class_));
    }
    
    @NotThreadSafe
    public static class Builder<E> extends AbstractCollectionBuilder<E, TreeSequence<E>> {
        private final Comparator<? super E> comparator;
        
        @Nullable
        private TreeSequence<E> sequence;
        
        public Builder(Comparator<? super E> comparator) {
            this.comparator = comparator;
            _reset();
        }
        
        @Override
        protected void _add(E element) {
            sequence.add(element);
        }
        
        @Override
        protected final void _reset() {
            this.sequence = new TreeSequence<E>(comparator);
        }
        
        @Override
        protected TreeSequence<E> _createCollection() {
            TreeSequence<E> collection = sequence;
            sequence = null;
            return collection;
        }
    }

}
