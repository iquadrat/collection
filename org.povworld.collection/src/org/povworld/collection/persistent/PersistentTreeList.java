package org.povworld.collection.persistent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.EmptyIterator;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AbstractAvlTreeBuilder;
import org.povworld.collection.tree.AbstractImmutableAvlTreeNode;
import org.povworld.collection.tree.AvlTreeNode;
import org.povworld.collection.tree.ReverseTreeIterator;
import org.povworld.collection.tree.TreeIterator;
import org.povworld.collection.tree.TreeUtil;

// TODO implement on higher-fanout tree?
public class PersistentTreeList<E> extends AbstractOrderedCollection<E> implements PersistentList<E> {
    
    protected static class TreeListManager<E> extends AbstractAvlTreeBuilder<ListTreeNode<E>> {
        
        @Override
        public ListTreeNode<E> createSubTree(ListTreeNode<E> left, ListTreeNode<E> top, ListTreeNode<E> right) {
            return new ListTreeNode<E>(left, right, top.getElement());
        }
        
        protected ListTreeNode<E> createSubTree(E element) {
            return new ListTreeNode<E>(element);
        }
        
        ListTreeNode<E> insert(@CheckForNull ListTreeNode<E> tree, int position, E element) {
            if (tree == null) {
                if (position != 0) {
                    throw new IndexOutOfBoundsException();
                }
                return createSubTree(element);
            }
            int cmp = position - (tree.getLeft() == null ? 0 : tree.getLeft().size());
            if (cmp <= 0) {
                ListTreeNode<E> newSubTree = insert(tree.getLeft(), position, element);
                return balance(newSubTree, tree, tree.getRight());
            } else {
                ListTreeNode<E> newSubTree = insert(tree.getRight(), cmp - 1, element);
                return balance(tree.getLeft(), tree, newSubTree);
            }
        }
        
        ListTreeNode<E> remove(@CheckForNull ListTreeNode<E> tree, int position) {
            if (tree == null) {
                throw new IndexOutOfBoundsException();
            }
            
            int cmp = position - size(tree.getLeft());
            if (cmp < 0) {
                ListTreeNode<E> newSubTree = remove(tree.getLeft(), position);
                return balance(newSubTree, tree, tree.getRight());
            }
            if (cmp > 0) {
                ListTreeNode<E> newSubTree = remove(tree.getRight(), cmp - 1);
                return balance(tree.getLeft(), tree, newSubTree);
            }
            
            // found the element to remove
            if (tree.getRight() == null) {
                return tree.getLeft();
            }
            
            // replace the root of the tree with the successor's element
            ListTreeNode<E> successor = TreeUtil.getMinNode(tree.getRight());
            ListTreeNode<E> newRight = remove(tree.getRight(), 0);
            return balance(tree.getLeft(), successor, newRight);
        }
        
        @CheckForNull
        E get(@CheckForNull ListTreeNode<E> tree, int position) {
            ListTreeNode<E> subTree = tree;
            do {
                int cmp = position - size(subTree.getLeft());
                if (cmp < 0) {
                    subTree = subTree.getLeft();
                } else if (cmp > 0) {
                    subTree = subTree.getRight();
                    position = cmp - 1;
                } else {
                    return subTree.getElement();
                }
            } while (subTree != null);
            throw new IndexOutOfBoundsException();
        }
        
        @CheckForNull
        ListTreeNode<E> set(@CheckForNull ListTreeNode<E> tree, int position, E value) {
            if (tree == null) {
                throw new IndexOutOfBoundsException();
            }
            int cmp = position - size(tree.getLeft());
            if (cmp < 0) {
                ListTreeNode<E> newSubTree = set(tree.getLeft(), position, value);
                if (newSubTree == null) {
                    return null;
                }
                return createSubTree(newSubTree, tree, tree.getRight()); // No re-balancing as the tree structure did not change
            }
            if (cmp > 0) {
                ListTreeNode<E> newSubTree = set(tree.getRight(), cmp - 1, value);
                if (newSubTree == null) {
                    return null;
                }
                return createSubTree(tree.getLeft(), tree, newSubTree); // No re-balancing as the tree structure did not change
            }
            // compare using == not equals as required by PersistentList.set
            if (tree.getElement() == value) return null;
            return createSubTree(tree.getLeft(), createSubTree(value), tree.getRight());
        }
    }
    
    @Immutable
    protected static class ListTreeNode<E> extends AbstractImmutableAvlTreeNode<ListTreeNode<E>> {
        
        private final E element;
        
        private final int size;
        
        ListTreeNode(E element) {
            super(null, null);
            this.element = element;
            this.size = 1;
        }
        
        ListTreeNode(ListTreeNode<E> left, ListTreeNode<E> right, E element) {
            super(left, right);
            this.element = element;
            this.size = PersistentTreeList.size(left) + PersistentTreeList.size(right) + 1;
        }
        
        @Override
        public String toString() {
            return "[" + left + "," + element + "," + right + "]";
        }
        
        public E getElement() {
            return element;
        }
        
        public int size() {
            return size;
        }
        
    }
    
    private static int size(@CheckForNull ListTreeNode<?> node) {
        return node == null ? 0 : node.size;
    }
    
    private static final TreeListManager<Object> TREE_MANAGER = new TreeListManager<Object>();
    
    private static class EmptyList<E> extends AbstractOrderedCollection<E> implements PersistentList<E> {
        
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
        public E get(int index) {
            throw new IndexOutOfBoundsException();
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
        public PersistentList<E> with(E element) {
            return new PersistentTreeList<>(new ListTreeNode<>(element));
        }
        
        @Override
        public PersistentList<E> with(E element, int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return with(element);
        }
        
        @Override
        public PersistentList<E> withAll(Collection<? extends E> elements) {
            PersistentList<E> result = null;
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
        public PersistentList<E> withReplacementAt(E element, int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public PersistentList<E> without(int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public PersistentList<E> withAllReplaced(E oldElement, E newElement) {
            return this;
        }
        
        @Override
        public PersistentList<E> cleared() {
            return this;
        }
        
    }
    
    private static final PersistentList<Object> EMPTY = new EmptyList<>();
    
    @SuppressWarnings({"unchecked"})
    protected TreeListManager<E> getTreeManager() {
        return (TreeListManager<E>)TREE_MANAGER;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> PersistentList<E> empty() {
        return (PersistentList<E>)EMPTY;
    }
    
    protected final ListTreeNode<E> root;
    
    protected PersistentTreeList(ListTreeNode<E> root) {
        PreConditions.paramNotNull(root);
        this.root = root;
    }
    
    @Override
    public E getFirstOrNull() {
        return getFirst();
    }
    
    @Override
    public E getFirst() {
        return TreeUtil.getMinNode(root).getElement();
    }
    
    @Override
    public E get(int index) {
        return getTreeManager().get(root, index);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new TreeIterator<E, ListTreeNode<E>>(root, AvlTreeNode.getHeight(root)) {
            
            @Override
            protected E getElement(ListTreeNode<E> tree) {
                return tree.getElement();
            }
        };
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return new ReverseTreeIterator<E, ListTreeNode<E>>(root, AvlTreeNode.getHeight(root)) {
            
            @Override
            protected E getElement(ListTreeNode<E> tree) {
                return tree.getElement();
            }
        };
    }
    
    @Override
    public E getLastOrNull() {
        return getLast();
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public E getLast() throws NoSuchElementException {
        return TreeUtil.getMaxNode(root).getElement();
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return CollectionUtil.getObjectIdentificator();
    }
    
    @Override
    @CheckReturnValue
    public PersistentList<E> with(E element) {
        return with(element, size());
    }
    
    @Override
    @CheckReturnValue
    public PersistentList<E> with(E element, int index) {
        PreConditions.paramNotNull(element);
        ListTreeNode<E> newRoot = getTreeManager().insert(root, index, element);
        return new PersistentTreeList<E>(newRoot);
    }
    
    @Override
    @CheckReturnValue
    public PersistentList<E> withAll(Collection<? extends E> elements) {
        ListTreeNode<E> newRoot = root;
        for (E element: elements) {
            PreConditions.paramNotNull(element);
            newRoot = getTreeManager().insert(newRoot, newRoot.size(), element);
        }
        if (newRoot == root) {
            return this;
        }
        return new PersistentTreeList<E>(newRoot);
    }
    
    @Override
    @CheckReturnValue
    public PersistentList<E> withReplacementAt(E element, int index) {
        ListTreeNode<E> newRoot = getTreeManager().set(root, index, element);
        if (newRoot == null) {
            return this;
        }
        return new PersistentTreeList<E>(newRoot);
    }
    
    @Override
    @CheckReturnValue
    public PersistentList<E> without(int index) {
        ListTreeNode<E> newRoot = getTreeManager().remove(root, index);
        if (newRoot == null) {
            return empty();
        }
        return new PersistentTreeList<E>(newRoot);
    }
    
    @Override
    public PersistentList<E> withAllReplaced(E oldElement, E newElement) {
        PersistentList<E> newList = this;
        int index = 0;
        for (E element: this) {
            if (element.equals(oldElement)) {
                newList = newList.withReplacementAt(newElement, index);
            }
            index++;
        }
        return newList;
    }
    
    @Override
    public int size() {
        return root.size();
    }
    
    @Override
    public PersistentList<E> cleared() {
        return empty();
    }
    
    public void checkInvariants() {
        getTreeManager().checkInvariants(root);
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentList<E>> {
        
        private PersistentList<E> list = empty();
        
        @Override
        protected void _add(E element) {
            list = list.with(element);
        }
        
        @Override
        protected PersistentList<E> _createCollection() {
            return list;
        }
        
        @Override
        protected void _reset() {
            list = empty();
        }
        
    }
    
}
