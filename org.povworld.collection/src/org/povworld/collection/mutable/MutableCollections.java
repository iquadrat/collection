package org.povworld.collection.mutable;

import static org.povworld.collection.CollectionUtil.wrap;

import org.povworld.collection.Collection;
import org.povworld.collection.Comparator;
import org.povworld.collection.ListMultiMap;
import org.povworld.collection.MultiMap;

/**
 * This class contains static methods to create mutable collections.
 */
public class MutableCollections {
    
    private MutableCollections() {}
    
    // empty initialization
    
    // TODO implement OrderedHashSet  
    public static <E> IndexedHashSet<E> orderedSetOf() {
        return new IndexedHashSet<>();
    }
    
    public static <E> IndexedHashSet<E> indexedSetOf() {
        return new IndexedHashSet<>();
    }
    
    public static <E> HashSet<E> setOf() {
        return new HashSet<>();
    }
    
    public static <E> ArrayList<E> listOf() {
        return new ArrayList<>();
    }
    
    public static <E> TreeList<E> treeListOf() {
        return new TreeList<>();
    }
    
    public static <E extends Comparable<E>> TreeSet<E> treeSetOf(Class<E> comparable) {
        return TreeSet.create(comparable);
    }
    
    public static <E extends Comparable<E>> TreeSet<E> treeSetOf(Comparator<E> comparator) {
        return new TreeSet<>(comparator);
    }
    
    public static <K, V> ListMultiMap<K, V> listMultiMapOf() {
        return new HashListMultiMap<K, V>(0);
    }
    
    public static <K, V> MultiMap<K, V> multiMapOf() {
        return new HashMultiMap<K, V>();
    }
    
    // single element initialization
    
    public static <E> IndexedHashSet<E> orderedSetOf(E element) {
        return indexedSetOf(element);
    }
    
    public static <E> IndexedHashSet<E> indexedSetOf(E element) {
        return IndexedHashSet.<E>newBuilder().add(element).build();
    }
    
    public static <E> HashSet<E> setOf(E element) {
        return HashSet.<E>newBuilder(1).add(element).build();
    }
    
    public static <E> ArrayList<E> listOf(E element) {
        return ArrayList.copyOf(wrap(element));
    }
    
    public static <E> TreeList<E> treeListOf(E element) {
        return new TreeList<>(element);
    }
    
    public static <E extends Comparable<E>> TreeSet<E> treeSetOf(Class<E> class_, E element) {
        return TreeSet.<E>newBuilder(class_).add(element).build();
    }
    
    public static <K, V> ListMultiMap<K, V> listMultiMapOf(K key, Collection<? extends V> values) {
        HashListMultiMap<K, V> map = new HashListMultiMap<K, V>(1);
        map.putAll(key, values);
        return map;
    }
    
    public static <K, V> ListMultiMap<K, V> listMultiMapOf(K key, V value) {
        HashListMultiMap<K, V> map = new HashListMultiMap<K, V>(1);
        map.putAtEnd(key, value);
        return map;
    }
    
    public static <K, V> MultiMap<K, V> multiMapOf(K key, Collection<? extends V> values) {
        HashMultiMap<K, V> map = new HashMultiMap<K, V>();
        map.putAll(key, values);
        return map;
    }
    
    public static <K, V> MultiMap<K, V> multiMapOf(K key, V value) {
        HashMultiMap<K, V> map = new HashMultiMap<K, V>();
        map.put(key, value);
        return map;
    }
    
    // array initialization
    
    @SafeVarargs
    public static <E> IndexedHashSet<E> orderedSetOf(E... elements) {
        return asOrderedSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> HashSet<E> setOf(E... elements) {
        return asSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> IndexedHashSet<E> indexedSetOf(E... elements) {
        return asIndexedSet(wrap(elements));
    }
    
    @SafeVarargs
    public static <E> ArrayList<E> listOf(E... elements) {
        return ArrayList.of(elements);
    }
    
    @SafeVarargs
    public static <E> TreeList<E> treeListOf(E... elements) {
        return asTreeList(wrap(elements));
    }
    
    @SafeVarargs
    public static <E extends Comparable<E>> TreeSet<E> treeSetOf(Class<E> class_, E... elements) {
        return asTreeSet(class_, wrap(elements));
    }
    
    // iterable initialization
    
    public static <E> IndexedHashSet<E> asOrderedSet(Iterable<? extends E> elements) {
        return asIndexedSet(elements);
    }
    
    public static <E> IndexedHashSet<E> asIndexedSet(Iterable<? extends E> elements) {
        return IndexedHashSet.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> HashSet<E> asSet(Iterable<? extends E> elements) {
        return HashSet.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> ArrayList<E> asList(Iterable<? extends E> elements) {
        return ArrayList.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E> TreeList<E> asTreeList(Iterable<? extends E> elements) {
        return TreeList.<E>newBuilder().addAll(elements).build();
    }
    
    public static <E extends Comparable<E>> TreeSet<E> asTreeSet(Class<E> class_, Iterable<? extends E> elements) {
        return TreeSet.newBuilder(class_).addAll(elements).build();
    }
    
}
