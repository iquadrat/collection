package org.povworld.collection.mutable;

import static org.povworld.collection.common.ObjectUtil.checkNotNull;

import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Container;
import org.povworld.collection.List;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractOrderedCollection;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.tree.AbstractAvlTreeNode;
import org.povworld.collection.tree.AvlTreeUtil;
import org.povworld.collection.tree.MutableAvlTreeNode;
import org.povworld.collection.tree.ReverseTreeIterator;
import org.povworld.collection.tree.TreeIterator;
import org.povworld.collection.tree.TreeUtil;

/**
 * {@code TreeList} provides fast insert and lookup by index as well as allows to find the indices 
 * of elements quickly. 
 * <p>
 * TreeList stores elements in an AVL tree nodes according to their index. Additionally,
 * it holds a hash-map which maps each node to a tree that contains all indices of the set
 * of elements which are equal to each other.
 * 
 * @param <E> the element type
 */
@NotThreadSafe
public class TreeList<E> extends AbstractOrderedCollection<E> implements Container<E>, List<E> {
    
    /**
     * Tree nodes used for storing the elements according to their index.
     */
    private static class TreeListNode<E> implements MutableAvlTreeNode<TreeListNode<E>> {
        
        private int subTreeSize;
        
        private int height;
        
        @CheckForNull
        private TreeListNode<E> left;
        
        @CheckForNull
        private TreeListNode<E> right;
        
        @CheckForNull
        private TreeListNode<E> parent = null;
        
        private E element;
        
        private final ElementIndexTreeNode<E> elementIndexTreeNode = new ElementIndexTreeNode<E>(this);
        
        public TreeListNode(E element) {
            this(null, null, element);
        }
        
        public ElementIndexTreeNode<E> getElementIndexTreeNode() {
            return elementIndexTreeNode;
        }
        
        public TreeListNode(@CheckForNull TreeListNode<E> left, @CheckForNull TreeListNode<E> right, E element) {
            this.left = left;
            this.right = right;
            this.element = element;
            calculateSizeAndHeight();
            if (left != null) {
                left.setParent(this);
            }
            if (right != null) {
                right.setParent(this);
            }
        }
        
        private void calculateSizeAndHeight() {
            subTreeSize = size(left) + size(right) + 1;
            height = Math.max(height(left), height(right)) + 1;
        }
        
        private static int height(TreeListNode<?> node) {
            return (node == null) ? 0 : node.getHeight();
        }
        
        private static int size(TreeListNode<?> node) {
            return (node == null) ? 0 : node.getSubTreeSize();
        }
        
        public int getSubTreeSize() {
            return subTreeSize;
        }
        
        /**
         * @return the parent node or {@code null} if this is the tree root
         */
        @CheckForNull
        public TreeListNode<E> getParent() {
            return parent;
        }
        
        public int getIndex() {
            return getIndex(this);
        }
        
        // TODO write non-recursive implementation?
        private static int getIndex(TreeListNode<?> node) {
            TreeListNode<?> parent = node.getParent();
            if (parent == null) {
                // this node is the root
                return size(node.getLeft());
            }
            
            if (parent.getLeft() == node) {
                // node is left child
                return getIndex(parent) - size(node.getRight()) - 1;
            } else {
                // node is right child
                return getIndex(parent) + size(node.getLeft()) + 1;
            }
        }
        
        public void setParent(@CheckForNull TreeListNode<E> parent) {
            this.parent = parent;
        }
        
        @Override
        public TreeListNode<E> getLeft() {
            return left;
        }
        
        @Override
        public TreeListNode<E> getRight() {
            return right;
        }
        
        @Override
        public int getHeight() {
            return height;
        }
        
        @Override
        public int getBalance() {
            return height(right) - height(left);
        }
        
        @Override
        public void setLeft(@CheckForNull TreeListNode<E> left) {
            this.left = left;
            if (left != null) {
                left.setParent(this);
            }
            calculateSizeAndHeight();
        }
        
        @Override
        public void setRight(@CheckForNull TreeListNode<E> right) {
            this.right = right;
            if (right != null) {
                right.setParent(this);
            }
            calculateSizeAndHeight();
        }
        
        public E getElement() {
            return element;
        }
        
        public void setElement(E element) {
            this.element = element;
        }
        
    }
    
    // TODO This is a one-off implementation of the insert/remove. Can we unify it?
    private static class ElementIndexTreeManager<E> {
        
        public ElementIndexTreeNode<E> insert(@CheckForNull ElementIndexTreeNode<E> subTree, ElementIndexTreeNode<E> node) {
            if (subTree == null) {
                return node;
            }
            if (getIndex(node) <= getIndex(subTree)) {
                ElementIndexTreeNode<E> newSubTree = insert(subTree.getLeft(), node);
                subTree.setLeft(newSubTree);
                return AvlTreeUtil.balance(subTree);
            }
            // cmp > 0
            ElementIndexTreeNode<E> newSubTree = insert(subTree.getRight(), node);
            subTree.setRight(newSubTree);
            return AvlTreeUtil.balance(subTree);
        }
        
        @CheckForNull
        public ElementIndexTreeNode<E> remove(@CheckForNull ElementIndexTreeNode<E> subTree, ElementIndexTreeNode<E> node) {
            if (subTree == null) {
                return null;
            }
            int index1 = getIndex(node);
            int index2 = getIndex(subTree);
            if (index1 < index2) {
                ElementIndexTreeNode<E> newSubTree = remove(subTree.getLeft(), node);
                subTree.setLeft(newSubTree);
                return AvlTreeUtil.balance(subTree);
            }
            if (index1 > index2) {
                ElementIndexTreeNode<E> newSubTree = remove(subTree.getRight(), node);
                subTree.setRight(newSubTree);
                return AvlTreeUtil.balance(subTree);
            }
            
            // found the element to remove
            if (subTree.getRight() == null) {
                return subTree.getLeft();
            }
            
            // replace the root of the tree with the successor's element
            ElementIndexTreeNode<E> successor = TreeUtil.getMinNode(subTree.getRight());
            ElementIndexTreeNode<E> newRight = remove(subTree.getRight(), successor);
            successor.setLeft(subTree.getLeft());
            successor.setRight(newRight);
            return AvlTreeUtil.balance(successor);
        }
        
        protected int getIndex(ElementIndexTreeNode<E> node) {
            // TODO this results in O(log d * log n) runtime
            return node.getTreeListNode().getIndex();
        }
        
    }
    
    private static class AvlIndexTreeManager<E> {
        
        public TreeListNode<E> get(@CheckForNull TreeListNode<E> subTree, int index) throws IndexOutOfBoundsException {
            int subTreeIndex = index;
            while (subTree != null) {
                if (subTreeIndex == 0) {
                    TreeListNode<E> left = subTree.getLeft();
                    if (left == null) {
                        return subTree;
                    } else {
                        subTree = left;
                        continue;
                    }
                }
                
                TreeListNode<E> left = subTree.getLeft();
                if (left == null) {
                    subTree = subTree.getRight();
                    subTreeIndex--;
                    continue;
                }
                
                int leftSize = left.getSubTreeSize();
                if (subTreeIndex > leftSize) {
                    subTreeIndex = subTreeIndex - leftSize - 1;
                    subTree = subTree.getRight();
                } else if (subTreeIndex < leftSize) {
                    subTree = left;
                } else {
                    return subTree;
                }
            }
            throw new IndexOutOfBoundsException();
        }
        
        private int size(@CheckForNull TreeListNode<E> node) {
            return (node == null) ? 0 : node.getSubTreeSize();
        }
        
        public TreeListNode<E> insertLast(final @CheckForNull TreeListNode<E> subTree, TreeListNode<E> node) {
            if (subTree == null) {
                return node;
            }
            TreeListNode<E> parent = TreeUtil.getMaxNode(subTree);
            TreeListNode<E> child = node;
            while (parent != null) {
                TreeListNode<E> grandParent = parent.getParent();
                int height = parent.getHeight();
                parent.setRight(child);
                if (height == parent.getHeight()) {
                    // If height is unchanged, the balances in the upper path to the root stay 
                    // the same and we do not need any further balancing. 
                    return subTree;
                }
                child = AvlTreeUtil.balance(parent);
                parent = grandParent;
            }
            child.setParent(null);
            return child;
        }
        
        public TreeListNode<E> insert(@CheckForNull TreeListNode<E> subTree, TreeListNode<E> node, int index) throws IndexOutOfBoundsException {
            if (subTree == null) {
                if (index == 0) {
                    return node;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            int myIndex = subTreeSize(subTree.getLeft());
            if (index <= myIndex) {
                TreeListNode<E> newSubTree = insert(subTree.getLeft(), node, index);
                subTree.setLeft(newSubTree);
                newSubTree = AvlTreeUtil.balance(subTree);
                newSubTree.setParent(subTree.getParent());
                return newSubTree;
            } else {
                TreeListNode<E> newSubTree = insert(subTree.getRight(), node, index - myIndex - 1);
                subTree.setRight(newSubTree);
                newSubTree = AvlTreeUtil.balance(subTree);
                newSubTree.setParent(subTree.getParent());
                return newSubTree;
            }
        }
        
        private int subTreeSize(TreeListNode<E> node) {
            return (node == null) ? 0 : node.getSubTreeSize();
        }
        
        public TreeListNode<E> remove(TreeListNode<E> subTree, int index) throws IndexOutOfBoundsException {
            if (subTree == null) {
                throw new IndexOutOfBoundsException();
            }
            
            int myIndex = size(subTree.getLeft());
            if (index < myIndex) {
                TreeListNode<E> newSubTree = remove(subTree.getLeft(), index);
                subTree.setLeft(newSubTree);
                return AvlTreeUtil.balance(subTree);
            }
            if (index > myIndex) {
                TreeListNode<E> newSubTree = remove(subTree.getRight(), index - myIndex - 1);
                subTree.setRight(newSubTree);
                return AvlTreeUtil.balance(subTree);
            }
            
            // found the element to remove
            if (subTree.getRight() == null) {
                return subTree.getLeft();
            }
            
            // replace the root of the tree with the successor's element
            TreeListNode<E> successor = TreeUtil.getMinNode(subTree.getRight());
            TreeListNode<E> newRight = remove(subTree.getRight(), 0);
            successor.setLeft(subTree.getLeft());
            successor.setRight(newRight);
            return AvlTreeUtil.balance(successor);
        }
    }
    
    private static final AvlIndexTreeManager<?> TREE_MANAGER = new AvlIndexTreeManager<>();
    
    private static final ElementIndexTreeManager<?> ELEMENT_INDEX_TREE_MANAGER = new ElementIndexTreeManager<>();
    
    /**
     * Tree nodes used for the secondary tree holding the set of elements which are equal to each other.
     * Also in this tree, the elements are ordered by the index in the list. Nodes of both type are linked
     * together both ways to allow quickly moving between the trees.
     */
    private static class ElementIndexTreeNode<E> extends AbstractAvlTreeNode<ElementIndexTreeNode<E>>
            implements MutableAvlTreeNode<ElementIndexTreeNode<E>> {
        
        private final TreeListNode<E> listNode;
        
        protected ElementIndexTreeNode(TreeListNode<E> listNode) {
            this.listNode = listNode;
        }
        
        public TreeListNode<E> getTreeListNode() {
            return listNode;
        }
        
        public void resetChildren() {
            left = null;
            right = null;
            height = 1;
        }
    }
    
    /** 
     * The primary tree storing the elements in index order. Is null if the list is empty.
     */
    @CheckForNull
    private TreeListNode<E> indexTree;
    
    /**
     * Maps each element to a tree of index nodes of the given element. This tree has exactly one node if
     * there are no duplicates. The invariant is that there are no empty trees in the map, i.e. if
     * the last instance of an element is removed from the list, the tree is removed from the map.
     * <p>
     * This tree is used to speed up the {@link #indexOf(Object)} calculation for elements even in
     * the presence of duplicate elements in the list. Updating the tree when add, removing or changing
     * elements of the list takes O(log d * log n) time where d is the (maximal) number of duplicates for 
     * an element and n is the total number of elements in the TreeList.
     * <p>
     * Note that the tree consists of {@link NodeMapTreeNode} which use the index of the corresponding
     * {@link TreeListNode} as key. This means that the key can change when elements are inserted or
     * removed. However, the *order* of the keys in this map does not change and therefore this is no
     * problem for the tree consistency.
     */
    private final HashMap<E, ElementIndexTreeNode<E>> elementIndexTreeNodeMap = new HashMap<>();
    
    private int size = 0;
    
    public TreeList() {
        indexTree = null;
    }
    
    public TreeList(E element) {
        indexTree = new TreeListNode<E>(element);
        elementIndexTreeNodeMap.put(element, indexTree.getElementIndexTreeNode());
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean contains(E element) {
        return elementIndexTreeNodeMap.containsKey(element);
    }
    
    @Override
    public E findEqualOrNull(E element) {
        return elementIndexTreeNodeMap.findEqualKeyOrNull(element);
    }
    
    /**
     * Adds the given {@code element} to the end of the list.
     */
    public void add(E element) {
        PreConditions.paramNotNull(element);
        TreeListNode<E> node = new TreeListNode<E>(element);
        indexTree = (TreeListNode<E>)getIndexTreeManager().insertLast(indexTree, node);
        addToElementIndexTree(element, node);
        size++;
    }
    
    /**
     * Adds the given {@code element} at the given {@code index}.
     * 
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size}
     */
    public void add(E element, int index) {
        PreConditions.paramNotNull(element);
        TreeListNode<E> node = new TreeListNode<E>(element);
        if (index == size) {
            add(element);
        } else {
            setIndexTree(getIndexTreeManager().insert(indexTree, node, index));
            addToElementIndexTree(element, node);
            size++;
        }
    }
    
    private void addToElementIndexTree(E element, TreeListNode<E> node) {
        ElementIndexTreeNode<E> elementIndexTree = elementIndexTreeNodeMap.get(element);
        if (elementIndexTree == null) {
            elementIndexTreeNodeMap.put(element, node.getElementIndexTreeNode());
        } else {
            elementIndexTree = getElementIndexTreeManager().insert(elementIndexTree, node.getElementIndexTreeNode());
            elementIndexTreeNodeMap.put(element, elementIndexTree);
        }
    }
    
    private void setIndexTree(@CheckForNull TreeListNode<E> newRoot) {
        indexTree = (TreeListNode<E>)newRoot;
        if (newRoot != null) {
            indexTree.setParent(null);
        }
    }
    
    /**
     * Adds all {@code elements} to the end of the list.
     */
    public void addAll(Iterable<? extends E> elements) {
        for (E element: elements) {
            add(element);
        }
    }
    
    @SuppressWarnings("unchecked")
    private ElementIndexTreeManager<E> getElementIndexTreeManager() {
        return (ElementIndexTreeManager<E>)ELEMENT_INDEX_TREE_MANAGER;
    }
    
    @SuppressWarnings("unchecked")
    private AvlIndexTreeManager<E> getIndexTreeManager() {
        return (AvlIndexTreeManager<E>)TREE_MANAGER;
    }
    
    /**
     * Removes the element at the given {@code index}.
     * 
     * @return the element previously at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size}
     */
    public E removeIndex(int index) {
        // TODO merge into single tree operation
        TreeListNode<E> node = getTreeNode(index);
        setIndexTree(getIndexTreeManager().remove(indexTree, index));
        
        // Update element index tree.
        E element = node.getElement();
        ElementIndexTreeNode<E> elmentIndexTree = checkNotNull(elementIndexTreeNodeMap.get(element));
        elmentIndexTree = getElementIndexTreeManager().remove(elmentIndexTree, node.getElementIndexTreeNode());
        if (elmentIndexTree == null) {
            elementIndexTreeNodeMap.remove(element);
        } else {
            elementIndexTreeNodeMap.put(element, elmentIndexTree);
        }
        size--;
        return element;
    }
    
    /**
     * Removes the first occurrence of the given {@code element}.
     * @return true if the element was successfully removed or false if it was not found
     */
    public boolean remove(E element) {
        ElementIndexTreeNode<E> elementIndexTree = elementIndexTreeNodeMap.get(element);
        // Empty tree is never in the map, so if the returned tree is non-null, the element
        // is present at least once.
        if (elementIndexTree == null) {
            return false;
        }
        // TODO create a removeMin method in tree-manager
        ElementIndexTreeNode<E> min = checkNotNull(TreeUtil.getMinNode(elementIndexTree));
        removeNode(min.getTreeListNode());
        return true;
    }
    
    private void removeNode(TreeListNode<E> node) {
        E element = node.getElement();
        ElementIndexTreeNode<E> treeHashNode = ((TreeListNode<E>)node).getElementIndexTreeNode();
        removeFromElementIndexTree(elementIndexTreeNodeMap.get(element), treeHashNode, element);
        // TODO could just get the path from the node to root through parents
        setIndexTree(getIndexTreeManager().remove(indexTree, treeHashNode.getTreeListNode().getIndex()));
        size--;
    }
    
    private void removeFromElementIndexTree(ElementIndexTreeNode<E> elementIndexTree, ElementIndexTreeNode<E> node, E element) {
        elementIndexTree = getElementIndexTreeManager().remove(elementIndexTree, node);
        if (elementIndexTree == null) {
            elementIndexTreeNodeMap.remove(element);
        } else {
            elementIndexTreeNodeMap.put(element, elementIndexTree);
        }
    }
    
    /**
     * Removes all {@code elements} from the list.
     * 
     * @return the actual number of elements removed.
     */
    public int removeAll(Iterable<? extends E> elements) {
        int removed = 0;
        for (E element: elements) {
            if (remove(element)) {
                removed++;
            }
        }
        return removed;
    }
    
    private TreeListNode<E> getTreeNode(int index) {
        return (TreeListNode<E>)getIndexTreeManager().get(indexTree, index);
    }
    
    @Override
    public E get(int index) {
        return getTreeNode(index).getElement();
    }
    
    /**
     * Sets the element at {@code index} to the given {@code element}.
     * 
     * @return the element previously at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size}
     */
    public E set(int index, E element) {
        TreeListNode<E> treeListNode = getTreeNode(index);
        E oldElement = treeListNode.getElement();
        treeListNode.setElement(element);
        
        // Update element index trees.
        ElementIndexTreeNode<E> elementIndexTreeNode = treeListNode.getElementIndexTreeNode();
        ElementIndexTreeNode<E> oldElementIndexTree = elementIndexTreeNodeMap.get(oldElement);
        removeFromElementIndexTree(oldElementIndexTree, elementIndexTreeNode, oldElement);
        
        elementIndexTreeNode.resetChildren();
        addToElementIndexTree(element, treeListNode);
        return oldElement;
    }
    
    /**
     * Replaces all occurrences of {@code oldElement} by {@code newElement}.
     * @return the number of replacements that were done
     */
    public int replaceAll(E oldElement, E newElement) {
        ElementIndexTreeNode<E> oldElementIndexTree = elementIndexTreeNodeMap.remove(oldElement);
        if (oldElementIndexTree == null) {
            // No 'oldElements' contained. We are done.
            return 0;
        }
        
        int replaced = 0;
        ElementIndexTreeNode<E> newElementIndexTree = elementIndexTreeNodeMap.get(newElement);
        // Use post-order iteration because we are destroying the tree by calling 
        // 'resetChildren()' on its nodes.
        Iterator<ElementIndexTreeNode<E>> iterator = TreeUtil.iterateNodesPostOrder(oldElementIndexTree);
        while (iterator.hasNext()) {
            replaced++;
            ElementIndexTreeNode<E> current = iterator.next();
            current.getTreeListNode().setElement(newElement);
            if (newElementIndexTree != null) {
                current.resetChildren();
                newElementIndexTree = getElementIndexTreeManager().insert(newElementIndexTree, current);
            }
        }
        
        if (newElementIndexTree == null) {
            newElementIndexTree = oldElementIndexTree;
        }
        elementIndexTreeNodeMap.put(newElement, newElementIndexTree);
        return replaced;
    }
    
    /**
     * Returns the first index of the given element or {@code -1} if the element is not contained in the list.
     */
    public int indexOf(E element) {
        ElementIndexTreeNode<E> elementIndexTree = elementIndexTreeNodeMap.get(element);
        if (elementIndexTree == null) return -1;
        // rely on the invariant: empty tree is never in map
        return checkNotNull(TreeUtil.getMinNode(elementIndexTree)).getTreeListNode().getIndex();
    }
    
    /**
     * Returns the last index of the given element or {@code -1} if the element is not contained in the list.
     */
    public int lastIndexOf(E element) {
        ElementIndexTreeNode<E> elementIndexTree = elementIndexTreeNodeMap.get(element);
        if (elementIndexTree == null) return -1;
        // rely on the invariant: empty tree is never in map
        return checkNotNull(TreeUtil.getMaxNode(elementIndexTree)).getTreeListNode().getIndex();
    }
    
    // TODO add method that gives iterator over indices of duplicate for a given element 
    
    /**
     * Removes all elements.
     */
    public void clear() {
        indexTree = null;
        elementIndexTreeNodeMap.clear();
        size = 0;
    }
    
    @Override
    public E getFirstOrNull() {
        TreeListNode<E> min = TreeUtil.getMinNode(indexTree);
        return (min == null) ? null : min.getElement();
    }
    
    @Override
    public E getLastOrNull() {
        TreeListNode<E> min = TreeUtil.getMaxNode(indexTree);
        return (min == null) ? null : min.getElement();
    }
    
    @Override
    public Iterator<E> iterator() {
        if (indexTree == null) {
            return CollectionUtil.emptyIterator();
        }
        
        return new TreeIterator<E, TreeListNode<E>>(indexTree, indexTree.getHeight()) {
            @Override
            protected E getElement(TreeListNode<E> node) {
                return node.getElement();
            }
        };
    }
    
    public Iterator<E> modifyingIterator() {
        if (indexTree == null) {
            return CollectionUtil.emptyIterator();
        }
        
        return new TreeIterator<E, TreeListNode<E>>(indexTree, indexTree.getHeight()) {
            @CheckForNull
            TreeListNode<E> current = null;
            
            @Override
            protected E getElement(TreeListNode<E> node) {
                current = node;
                return node.getElement();
            }
            
            @Override
            public void remove() {
                if (current == null) {
                    throw new IllegalStateException();
                }
                removeNode(current);
                current = null;
            }
        };
    }
    
    @Override
    public Iterator<E> reverseIterator() {
        if (indexTree == null) {
            return CollectionUtil.emptyIterator();
        }
        
        return new ReverseTreeIterator<E, TreeListNode<E>>(indexTree, indexTree.getHeight()) {
            @Override
            protected E getElement(TreeListNode<E> node) {
                return node.getElement();
            }
        };
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>();
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, TreeList<E>> {
        
        @Nullable
        private TreeList<E> treeList = new TreeList<>();
        
        @Override
        public Builder<E> add(E element) {
            super.add(element);
            return this;
        }
        
        @Override
        protected void _add(E element) {
            treeList.add(element);
        }
        
        @Override
        public Builder<E> addAll(Iterable<? extends E> elements) {
            super.addAll(elements);
            return this;
        }
        
        public Builder<E> remove(E element) {
            checkUsable();
            treeList.remove(element);
            return this;
        }
        
        @Override
        protected TreeList<E> _createCollection() {
            TreeList<E> collection = treeList;
            treeList = null;
            return collection;
        }
        
        @Override
        protected void _reset() {
            treeList = new TreeList<>();
        }
        
    }
    
}
