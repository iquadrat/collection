package org.povworld.collection.mutable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.Set;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.AbstractUnOrderedCollection;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.Assert;
import org.povworld.collection.common.PreConditions;

/**
 * Implementation of {@link Set} that uses the element's hash value to find them.
 *
 * @param <E> the element type
 */
@NotThreadSafe
public class HashSet<E> extends AbstractUnOrderedCollection<E> implements Set<E> {
    
    private Identificator<? super E> identificator;
    
    private E table[];
    
    private byte[] hashPrefix;
    
    private int size = 0;
    
    private int mask;
    
    public HashSet() {
        this(CollectionUtil.getObjectIdentificator());
    }
    
    public HashSet(int expectedSize) {
        this(CollectionUtil.getObjectIdentificator(), expectedSize);
    }
    
    public HashSet(Identificator<? super E> identificator) {
        this(identificator, 1);
    }
    
    public HashSet(Identificator<? super E> identificator, int expectedSize) {
        this.identificator = identificator;
        init(getCapacityFor(expectedSize));
    }
    
    public static <E> HashSet<E> copyOf(Collection<? extends E> collection) {
        HashSet<E> result = new HashSet<>(collection.size());
        result.addAll(collection);
        return result;
    }
    
    @Override
    public Identificator<? super E> getIdentificator() {
        return identificator;
    }
    
    private void init(int capacity) {
        table = ArrayUtil.unsafeCastedNewArray(capacity);
        hashPrefix = new byte[capacity];
        mask = capacity - 1;
    }
    
    private static int getCapacityFor(int size) {
        int capacity = 1;
        while (capacity < size * 2) {
            capacity *= 2;
        }
        return capacity;
    }
    
    private boolean isEmptyTableIndex(int index) {
        return hashPrefix[index] == 0;
    }
    
    private int findElementByHash(E element, int hash) {
        int index = hash & mask;
        while (!isEmptyTableIndex(index)) {
            if (readPrefix(index) == prefix(hash) && ((table[index] == element) || identificator.equals(table[index], element))) {
                return index;
            }
            if (!isCollisionBitSet(index)) {
                break;
            }
            index = increment(index);
        }
        return -1;
    }
    
    private static byte prefix(int hash) {
        return (byte)(hash >>> 26);
    }
    
    private byte readPrefix(int index) {
        return (byte)(((hashPrefix[index] & 0xFF) >> 2));
    }
    
    /**
     * NOTE: Clears collision bit for the given index.
     */
    private void storePrefix(int index, int hash) {
        hashPrefix[index] = (byte)((prefix(hash) << 2) | 2);
    }
    
    private int increment(int index) {
        return (index + 1) & mask;
    }
    
    public boolean add(E element) {
        PreConditions.paramNotNull(element);
        int hash = identificator.hashCode(element);
        if (findElementByHash(element, hash) != -1) {
            return false;
        }
        
        ensureCapacityFor(size + 1);
        internalInsert(element, hash); // TODO double iteration, we did this already in findElementByHash
        size++;
        return true;
    }
    
    public int addAll(Iterable<? extends E> elements) {
        int added = 0;
        for (E element: elements) {
            if (add(element)) added++;
        }
        return added;
    }
    
    @Override
    public boolean contains(E element) {
        return findElementIndex(element) != -1;
    }
    
    @Override
    public E findEqualOrNull(E element) {
        int index = findElementIndex(element);
        return index == -1 ? null : table[index];
    }
    
    private int findElementIndex(E element) {
        int hash = identificator.hashCode(element);
        return findElementByHash(element, hash);
    }
    
    /**
     * Finds an element in the set that is equal to the given {@code element} (according to the set's identificator). 
     * If successful, returns the found element, otherwise returns {@code null}.
     */
    @CheckForNull
    public E getCurrentOrNull(E element) {
        int index = findElementIndex(element);
        if (index == -1) return null;
        return table[index];
    }
    
    /**
     * Removes the given {@code element} from the set if present, i.e., it tries to find
     * an element in the set which is equals to the given element (according to the set's identificator) and
     * if successful, the found element is removed and returned. 
     * 
     * @return the removed element or {@code null} if no matching element was found
     */
    @CheckForNull
    public E removeAndReturnRemoved(E element) {
        int index = findElementIndex(element);
        if (index == -1) return null;
        E current = table[index];
        removeIndex(index);
        return current;
    }
    
    /**
     * Removes the given {@code element} from the set if present.
     * @return true iff the element has been present
     */
    public boolean remove(E element) {
        int index = findElementIndex(element);
        if (index == -1) return false;
        removeIndex(index);
        return true;
    }
    
    /**
     * Removes all elements of the given {@code iterable}.
     * @return number of elements removed
     */
    public int removeAll(Iterable<? extends E> iterable) {
        int removed = 0;
        for (E element: iterable) {
            if (remove(element)) {
                removed++;
            }
        }
        return removed;
    }
    
    private void removeIndex(int index) {
        boolean isCollision = isCollisionBitSet(index);
        table[index] = null;
        hashPrefix[index] = 0;
        if (isCollision) {
            plugHole(index);
        }
        size--;
        checkEmptySpace();
    }
    
    /**
     * Removes all elements from the set.
     */
    public void clear() {
        if (size == 0) {
            return;
        }
        init(1);
        size = 0;
    }
    
    private void plugHole(int holeIdx) {
        int index = increment(holeIdx);
        while (!isEmptyTableIndex(index)) {
            E elementToReinsert = table[index];
            boolean collision = isCollisionBitSet(index);
            table[index] = null;
            hashPrefix[index] = 0;
            internalInsert(elementToReinsert, identificator.hashCode(elementToReinsert));
            if (!collision) break;
            index = increment(index);
        }
    }
    
    private void internalInsert(E element, int hash) {
        int index = hash & mask;
        while (!isEmptyTableIndex(index)) {
            setCollisionBit(index);
            index = increment(index);
        }
        table[index] = element;
        storePrefix(index, hash);
    }
    
    private void setCollisionBit(int index) {
        hashPrefix[index] |= 1;
    }
    
    private boolean isCollisionBitSet(int index) {
        return (hashPrefix[index] & 1) != 0;
    }
    
    private void ensureCapacityFor(int size) {
        if (table.length >= size * 2) {
            return;
        }
        buildHashTable(table, getCapacityFor(size));
    }
    
    private void checkEmptySpace() {
        if (table.length <= size * 5 || table.length < 5) return;
        
        int newCapacity = table.length;
        while ((newCapacity > size * 5) && (newCapacity > 1)) {
            newCapacity /= 2;
        }
        
        buildHashTable(table, newCapacity);
    }
    
    private void buildHashTable(E[] elements, int tableSize) {
        Assert.assertTrue((tableSize & (tableSize - 1)) == 0, "Table size must be a power of two!");
        init(tableSize);
        for (E element: elements) {
            if (element == null) continue;
            internalInsert(element, identificator.hashCode(element));
        }
    }
    
    @Override
    public Iterator<E> iterator() {
        return new HashSetIterator();
    }
    
    public Iterator<E> modifyingIterator() {
        return new HashSetIterator() {
            @Override
            public void remove() {
                if (current == -1) {
                    throw new IllegalStateException();
                }
                HashSet.this.remove(iteratingTable[current]);
                current = -1;
            }
        };
    }
    
    private class HashSetIterator implements Iterator<E> {
        
        protected final E[] iteratingTable;
        
        protected int next = -1;
        
        protected int current = -1;
        
        HashSetIterator() {
            iteratingTable = table;
            findNextElement();
        }
        
        @Override
        public boolean hasNext() {
            return next < iteratingTable.length;
        }
        
        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            E result = iteratingTable[next];
            if (result == null) {
                throw new ConcurrentModificationException();
            }
            current = next;
            findNextElement();
            return result;
        }
        
        private void findNextElement() {
            do {
                next++;
                if (next >= iteratingTable.length) break;
            } while (iteratingTable[next] == null);
        }
        
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public E getFirstOrNull() {
        if (isEmpty()) return null;
        int i = 0;
        // As the set is non-empty there must be at least one non-null index.
        while (isEmptyTableIndex(i)) {
            i++;
        }
        return table[i];
    }
    
    public static <E> Builder<E> newBuilder() {
        return new Builder<>(CollectionUtil.getObjectIdentificator(), 0);
    }
    
    public static <E> Builder<E> newBuilder(Identificator<? super E> identificator) {
        return new Builder<>(identificator, 0);
    }
    
    public static <E> Builder<E> newBuilder(int expectedSize) {
        return new Builder<>(CollectionUtil.getObjectIdentificator(), expectedSize);
    }
    
    public static <E> Builder<E> newBuilder(Identificator<? super E> identificator, int expectedSize) {
        return new Builder<>(identificator, expectedSize);
    }
    
    @NotThreadSafe
    public static final class Builder<E> extends AbstractCollectionBuilder<E, HashSet<E>> {
        
        @Nullable
        private HashSet<E> set = new HashSet<>();
        
        public Builder(Identificator<? super E> identificator, int expectedSize) {
            set = new HashSet<>(identificator);
        }
        
        @Override
        protected void _add(E element) {
            set.add(element);
        }
        
        @Override
        protected HashSet<E> _createCollection() {
            HashSet<E> result = set;
            set = null;
            return result;
        }
        
        @Override
        protected void _reset() {
            set = new HashSet<>();
        }
        
    }
    
}
