package org.povworld.collection.persistent;

import static org.povworld.collection.CollectionUtil.wrap;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.mutable.HashBag;

public class PersistentCollections {
    
    private PersistentCollections() {}
    
    // PersistentList
    
    public static <E> PersistentList<E> listOf() {
        return PersistentArrayList.empty();
    }
    
    public static <E> PersistentList<E> listOf(E element) {
        // TODO create singleton collection
        return PersistentArrayList.<E>empty().with(element);
    }
    
    @SafeVarargs
    public static <E> PersistentList<E> listOf(E... elements) {
        return asList(wrap(elements));
    }
    
    public static <E> PersistentList<E> asList(Iterable<E> elements) {
        return PersistentArrayList.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> PersistentList<E> asList(Collection<E> elements) {
        return PersistentArrayList.copyOf(elements);
    }
    
    // PersistentSet
    
    public static <E> PersistentSet<E> setOf() {
        return PersistentHashSet.<E>empty();
    }
    
    public static <E> PersistentSet<E> setOf(E element) {
        // TODO create singleton collection
        return PersistentHashSet.<E>newBuilder().add(element).build();
    }
    
    @SafeVarargs
    public static <E> PersistentSet<E> setOf(E... elements) {
        return asSet(CollectionUtil.wrap(elements));
    }
    
    public static <E> PersistentSet<E> asSet(Iterable<E> elements) {
        return PersistentHashSet.<E>empty().withAll(elements);
    }
    
    public static <E> PersistentSet<E> asSet(Collection<E> collection) {
        return PersistentHashSet.copyOf(collection);
    }
    
    // PersistentOrderedSet
    
    public static <E> PersistentOrderedSet<E> orderedSetOf() {
        return indexedSetOf();
    }
    
    public static <E> PersistentOrderedSet<E> orderedSetOf(E element) {
        return indexedSetOf(element);
    }
    
    @SafeVarargs
    public static <E> PersistentOrderedSet<E> orderedSetOf(E... elements) {
        return indexedSetOf(elements);
    }
    
    public static <E> PersistentOrderedSet<E> asOrderedSet(Iterable<E> elements) {
        return asIndexedSet(elements);
    }
    
    public static <E> PersistentOrderedSet<E> asOrderedSet(Collection<E> elements) {
        return asIndexedSet(elements);
    }
    
    // PersistentIndexedSet
    
    public static <E> PersistentIndexedSet<E> indexedSetOf() {
        return PersistentIndexedSetImpl.<E>empty();
    }
    
    public static <E> PersistentIndexedSet<E> indexedSetOf(E element) {
        // TODO create singleton collection
        return PersistentIndexedSetImpl.<E>newBuilder().add(element).build();
    }
    
    @SafeVarargs
    public static <E> PersistentIndexedSet<E> indexedSetOf(E... elements) {
        return asIndexedSet(wrap(elements));
    }
    
    public static <E> PersistentIndexedSet<E> asIndexedSet(Iterable<E> elements) {
        return PersistentIndexedSetImpl.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> PersistentIndexedSet<E> asIndexedSet(Collection<E> collection) {
        return asIndexedSet((Iterable<E>)collection);
    }
    
    // PersistentTreeSet
    
    public static <E extends Comparable<E>> PersistentOrderedSet<E> treeSetOf(Class<E> class_) {
        return PersistentTreeSet.empty(CollectionUtil.getDefaultComparator(class_));
    }
    
    public static <E extends Comparable<E>> PersistentOrderedSet<E> treeSetOf(Class<E> class_, E element) {
        // TODO create singleton collection
        return PersistentTreeSet.newBuilder(CollectionUtil.getDefaultComparator(class_)).add(element).build();
    }
    
    @SafeVarargs
    public static <E extends Comparable<E>> PersistentOrderedSet<E> treeSetOf(Class<E> class_, E... elements) {
        return asTreeSet(class_, wrap(elements));
    }
    
    public static <E extends Comparable<E>> PersistentOrderedSet<E> asTreeSet(Class<E> class_, Iterable<E> elements) {
        return PersistentTreeSet.newBuilder(CollectionUtil.getDefaultComparator(class_)).addAll(elements).build();
    }
    
    public static <E extends Comparable<E>> PersistentOrderedSet<E> asTreeSet(Class<E> class_, Collection<E> collection) {
        return asTreeSet(class_, (Iterable<E>)collection);
    }
    
    // PersistentMap
    
    public static <K, V> PersistentMap<K, V> mapOf() {
        return hashMapOf();
    }
    
    public static <K, V> PersistentHashMap<K, V> hashMapOf() {
        return PersistentHashMap.empty();
    }
    
    public static <K extends Comparable<K>, V> PersistentMap<K, V> treeMapOf(Class<K> keyClass) {
        return PersistentTreeMap.empty(keyClass);
    }
    
    // PersistentMultiMap  
    
    public static <K, V> PersistentListMultiMap<K, V> listMultiMapOf() {
        return PersistentListMultiMapImpl.<K, V>empty();
    }
    
    public static <K, V> PersistentMultiMap<K, V> multiMapOf() {
        return PersistentMultiMapImpl.<K, V>empty();
    }
    
    // Misc utility functions
    
    /**
     * Removes given {@code elements} from the given {@code list}. Handles multiple occurrence of the same 
     * element in {@code elements}: If an element 'e' is contained n times, the first 'n' occurrences of 'e'
     * in {@code list} are removed if present.
     * 
     * @return the resulting list
     */
    public static <E> PersistentList<E> removeAll(PersistentList<E> list, Collection<? extends E> elements) {
        HashBag<E> hashBag = new HashBag<E>(elements);
        int index = 0;
        PersistentList<E> result = list;
        for (E element: list) {
            if (hashBag.contains(element)) {
                result = result.without(index);
                hashBag.remove(element);
            } else {
                index++;
            }
        }
        return result;
    }
    
}
