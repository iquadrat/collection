package org.povworld.collection.persistent;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.List;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.EmptyIterator;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.persistent.PersistentTreeList.ListTreeNode;
import org.povworld.collection.persistent.PersistentTreeList.TreeListBuilder;

public class PersistentIndexedSetImpl<E> extends AbstractOrderedCollection<E> implements PersistentIndexedSet<E> {
    
    private static class EmptyIndexedSet<E> extends AbstractOrderedCollection<E> implements PersistentIndexedSet<E> {
        
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
        @CheckForNull
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
        public E get(int index) {
            throw new IndexOutOfBoundsException();
        }
        
        @Override
        public PersistentIndexedSet<E> with(E element) {
            return new PersistentIndexedSetImpl<>(new IndexedPersistentTreeList<>(element));
        }
        
        @Override
        public PersistentIndexedSet<E> withAll(Collection<? extends E> elements) {
            // TODO optimize
            PersistentIndexedSet<E> result = this;
            for (E element: elements) {
                result = result.with(element);
            }
            return result;
        }
        
        @Override
        public PersistentIndexedSet<E> without(E element) {
            return this;
        }
        
        @Override
        public PersistentIndexedSet<E> withoutAll(Collection<? extends E> elements) {
            return this;
        }
        
        @Override
        public PersistentIndexedSet<E> cleared() {
            return this;
        }
        
        @Override
        public PersistentIndexedSet<E> with(E element, int index) {
            return with(element);
        }
        
        @Override
        public PersistentIndexedSet<E> withoutElementAt(int index) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }
    
    private static class NewNodeCollectingTreeListBuilder<E> extends TreeListBuilder<E> {
        private final ArrayList<ListTreeNode<E>> newNodes;
        private final ArrayList<ListTreeNode<E>> removedNodes;
        
        public NewNodeCollectingTreeListBuilder(int height) {
            newNodes = new ArrayList<>(height); // TODO *2?
            removedNodes = new ArrayList<>(height);
        }
        
        @Override
        public ListTreeNode<E> createSubTree(ListTreeNode<E> left, ListTreeNode<E> top, ListTreeNode<E> right) {
            ListTreeNode<E> newNode = super.createSubTree(left, top, right);
            removedNodes.add(top);
            newNodes.push(newNode);
            return newNode;
        }
        
        @Override
        protected ListTreeNode<E> createSubTree(E element) {
            ListTreeNode<E> newNode = super.createSubTree(element);
            newNodes.push(newNode);
            return newNode;
        }
        
        @Override
        protected void dispose(ListTreeNode<E> node) {
            removedNodes.add(node);
        }
        
    }
    
    private static class IndexedPersistentTreeList<E> extends PersistentTreeList<E> {
        
        private final PersistentMap<E, ListTreeNode<E>> nodeMap;
        
        private final PersistentMap<ListTreeNode<E>, ListTreeNode<E>> parentMap;
        
        IndexedPersistentTreeList(E element) {
            super(new ListTreeNode<>(element));
            this.nodeMap = PersistentHashMap.<E, ListTreeNode<E>>empty().with(element, root);
            this.parentMap = PersistentHashMap.empty();
        }
        
        IndexedPersistentTreeList(ListTreeNode<E> root, PersistentMap<E, ListTreeNode<E>> nodeMap,
                PersistentMap<ListTreeNode<E>, ListTreeNode<E>> parentMap) {
            super(root);
            this.nodeMap = nodeMap;
            this.parentMap = parentMap;
            Assert.assertEquals(root.size(), nodeMap.keyCount());
            Assert.assertEquals(root.size() - 1, parentMap.keyCount());
        }
        
        public int indexOf(E element) {
            ListTreeNode<E> node = nodeMap.get(element);
            if (node == null) {
                return -1;
            }
            return indexOf(node);
        }
        
        private int indexOf(ListTreeNode<E> node) {
            ListTreeNode<E> parent = parentMap.get(node);
            if (parent == null) {
                // this node is the root
                return size(node.getLeft());
            }
            
            if (parent.getLeft() == node) {
                // node is left child
                return indexOf(parent) - size(node.getRight()) - 1;
            } else {
                // node is right child
                return indexOf(parent) + size(node.getLeft()) + 1;
            }
        }
        
        private static int size(ListTreeNode<?> node) {
            return (node == null) ? 0 : node.size();
        }
        
        @Override
        public IndexedPersistentTreeList<E> with(E element) {
            return with(element, size());
        }
        
        @Override
        public IndexedPersistentTreeList<E> with(E element, int index) {
            PreConditions.paramNotNull(element);
            NewNodeCollectingTreeListBuilder<E> treeListBuilder = new NewNodeCollectingTreeListBuilder<>(root.getHeight());
            ListTreeNode<E> newRoot = treeListBuilder.insert(root, index, element);
            PersistentMap<E, ListTreeNode<E>> newNodeMap =
                    updateNodeMap(nodeMap, treeListBuilder.newNodes, treeListBuilder.newNodes);
            PersistentMap<ListTreeNode<E>, ListTreeNode<E>> newParentMap =
                    updateParentMap(parentMap, treeListBuilder.newNodes, treeListBuilder.removedNodes);
            return new IndexedPersistentTreeList<E>(newRoot, newNodeMap, newParentMap);
        }
        
        @Override
        @CheckForNull
        public IndexedPersistentTreeList<E> without(int index) {
            NewNodeCollectingTreeListBuilder<E> treeListBuilder = new NewNodeCollectingTreeListBuilder<>(root.getHeight());
            ListTreeNode<E> newRoot = treeListBuilder.remove(root, index);
            if (newRoot == null) {
                return null;
            }
            PersistentMap<E, ListTreeNode<E>> newNodeMap =
                    updateNodeMap(nodeMap, treeListBuilder.newNodes, treeListBuilder.removedNodes);
            PersistentMap<ListTreeNode<E>, ListTreeNode<E>> newParentMap =
                    updateParentMap(parentMap, treeListBuilder.newNodes, treeListBuilder.removedNodes);
            return new IndexedPersistentTreeList<E>(newRoot, newNodeMap, newParentMap);
        }
        
        private PersistentMap<ListTreeNode<E>, ListTreeNode<E>> updateParentMap(
                PersistentMap<ListTreeNode<E>, ListTreeNode<E>> parentMap,
                List<ListTreeNode<E>> newNodes, List<ListTreeNode<E>> removedNodes) {
            for (ListTreeNode<E> node: removedNodes) {
                if (node.getLeft() != null) {
                    parentMap = parentMap.without(node.getLeft());
                }
                if (node.getRight() != null) {
                    parentMap = parentMap.without(node.getRight());
                }
            }
            for (ListTreeNode<E> node: newNodes) {
                if (node.getLeft() != null) {
                    parentMap = parentMap.with(node.getLeft(), node);
                }
                if (node.getRight() != null) {
                    parentMap = parentMap.with(node.getRight(), node);
                }
            }
            return parentMap;
        }
        
        private PersistentMap<E, ListTreeNode<E>> updateNodeMap(PersistentMap<E, ListTreeNode<E>> nodeMap,
                List<ListTreeNode<E>> newNodes, List<ListTreeNode<E>> removedNodes) {
            for (ListTreeNode<E> node: removedNodes) {
                // TODO add withoutAll
                nodeMap = nodeMap.without(node.getElement());
            }
            for (ListTreeNode<E> node: newNodes) {
                nodeMap = nodeMap.with(node.getElement(), node);
            }
            return nodeMap;
        }
        
    }
    
    private static final PersistentIndexedSet<?> EMPTY = new EmptyIndexedSet<Object>();
    
    private final IndexedPersistentTreeList<E> treeList;
    
    @SuppressWarnings("unchecked")
    public static <E> PersistentIndexedSet<E> empty() {
        return (PersistentIndexedSet<E>)EMPTY;
    }
    
    private PersistentIndexedSetImpl(IndexedPersistentTreeList<E> treeList) {
        this.treeList = ObjectUtil.checkNotNull(treeList);
    }
    
    @Override
    public int size() {
        return treeList.size();
    }
    
    @Override
    @CheckForNull
    public E getFirstOrNull() {
        return treeList.getFirstOrNull();
    }
    
    @Override
    public Iterator<E> iterator() {
        return treeList.iterator();
    }
    
    @Override
    public boolean contains(E element) {
        return treeList.nodeMap.containsKey(element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return treeList.nodeMap.keys().findEqualOrNull(element);
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        return treeList.reverseIterator();
    }
    
    @Override
    @CheckForNull
    public E getLastOrNull() {
        return treeList.getLastOrNull();
    }
    
    @Override
    public E get(int index) {
        return treeList.get(index);
    }
    
    @Override
    public PersistentIndexedSet<E> with(E element) {
        return with(element, treeList.size());
    }
    
    @Override
    public PersistentIndexedSet<E> with(E element, int index) {
        if (contains(element)) {
            return this;
        }
        return new PersistentIndexedSetImpl<>(treeList.with(element, index));
    }
    
    @Override
    public PersistentIndexedSet<E> withAll(Collection<? extends E> elements) {
        IndexedPersistentTreeList<E> newTreeList = treeList;
        for (E element: elements) {
            if (!contains(element)) {
                newTreeList = treeList.with(element);
            }
        }
        if (newTreeList == treeList) {
            // No elements added.
            return this;
        }
        return new PersistentIndexedSetImpl<>(newTreeList);
    }
    
    @Override
    public PersistentIndexedSet<E> without(E element) {
        int index = treeList.indexOf(element);
        if (index == -1) {
            return this;
        }
        return withoutElementAt(index);
    }
    
    @Override
    public PersistentIndexedSet<E> withoutElementAt(int index) {
        IndexedPersistentTreeList<E> newTreeList = treeList.without(index);
        if (newTreeList == null) {
            return empty();
        }
        return new PersistentIndexedSetImpl<>(newTreeList);
    }
    
    @Override
    public PersistentIndexedSet<E> withoutAll(Collection<? extends E> elements) {
        // TODO optimize
        PersistentIndexedSet<E> result = this;
        for (E element: elements) {
            result = result.without(element);
        }
        return result;
    }
    
    @Override
    public PersistentIndexedSet<E> cleared() {
        return empty();
    }
    
    public static <E> CollectionBuilder<E, PersistentIndexedSet<E>> newBuilder() {
        return new Builder<E>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, PersistentIndexedSet<E>> {
        
        private PersistentIndexedSet<E> set = empty();
        
        @Override
        protected void _add(E element) {
            set = set.with(element);
        }
        
        @Override
        protected PersistentIndexedSet<E> _createCollection() {
            return set;
        }
        
        @Override
        protected void _reset() {
            set = set.cleared();
        }
        
    }
    
    // For testing only.
    public void checkInvariants() {
        treeList.checkInvariants();
    }
    
}
