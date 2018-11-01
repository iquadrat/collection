package org.povworld.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;

import org.povworld.collection.common.ArrayIterator;
import org.povworld.collection.common.ArrayUtil;
import org.povworld.collection.common.CompoundIterable;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.common.PreConditions;
import org.povworld.collection.common.ReverseList;
import org.povworld.collection.common.ShuffledIterable;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.ArrayList.Builder;
import org.povworld.collection.mutable.HashBag;
import org.povworld.collection.mutable.HashSet;
import org.povworld.collection.mutable.IndexedHashSet;
import org.povworld.collection.mutable.MutableCollections;

/**
 * Helper methods that operate on generic {@link Collection}s.
 */
public final class CollectionUtil {
    
    private CollectionUtil() {
        // utility class
    }
    
    /**
     * Returns an {@link Identificator} that uses {@link Object#equals(Object)} to compare
     * objects and {@link Object#hashCode()} for calculating hash codes.
     */
    public static Identificator<Object> getObjectIdentificator() {
        return OBJECT_IDENTIFICATOR;
    }
    
    /**
     * Returns the default identificator for the given type. If the type implements
     * {@link java.util.Comparator}, comparison will be done using
     * {@link java.util.Comparator#compare(Object, Object)}. Otherwise,
     * {@link Object#equals(Object)} is used.
     */
    public static <E> Identificator<? super E> getDefaultIdentificator(Class<E> type) {
        if (Comparable.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            Identificator<E> result = getDefaultComparator(type.asSubclass(Comparable.class));
            return result;
        }
        return getObjectIdentificator();
    }
    
    /**
     * Returns a {@link Comparator} that uses {@link java.util.Comparator#compare(Object, Object)}
     * to compare objects.
     */
    public static <C extends Comparable<C>> Comparator<C> getDefaultComparator(Class<C> comparable) {
        return new DefaultComparator<>(comparable);
    }
    
    /**
     * Returns true if the given {@code iterable} is empty.
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        return !iterable.iterator().hasNext();
    }
    
    /**
     * Returns the number of elements in the iteration of the given {@code iterable}.
     * Runtime is linear in the returned value.
     */
    public static int sizeOf(Iterable<?> iterable) {
        return remainingElementCount(iterable.iterator());
    }
    
    /**
     * Counts the number of elements that are left in the given {@code iterator}.
     */
    public static int remainingElementCount(Iterator<?> iterator) {
        int size = 0;
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        return size;
    }
    
    /**
     * @return true if the given {@code iterator} has at least {@code n} elements remaining
     */
    public static boolean hasMinRemainingElements(Iterator<?> iterator, int n) {
        int size = 0;
        while (iterator.hasNext() && size < n) {
            iterator.next();
            size++;
        }
        return size == n;
    }
    
    /**
     * @return the first element in the iteration
     * @throws NoSuchElementException if the iterable is empty
     */
    public static <T> T firstElement(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) throw new NoSuchElementException();
        return iter.next();
    }
    
    /**
     * @return the first element of the given iterable or {@code null} if the iterable has no elements
     */
    @CheckForNull
    public static <T> T firstElementOrNull(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) return null;
        return iterator.next();
    }
    
    /**
     * @return the first element of the given array or {@code null} if the array is empty
     */
    @CheckForNull
    public static <T> T firstElementOrNull(T[] array) {
        if (array.length == 0) return null;
        return array[0];
    }
    
    /**
     * @return the last element of the given iterable
     * @throws NoSuchElementException if the given iterable is empty
     */
    @CheckForNull
    public static <T> T lastElementOrNull(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) return null;
        T result = iterator.next();
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }
    
    /**
     * @return the last element of the given array or {@code null} if the array is empty
     */
    @CheckForNull
    public static <T> T lastElementOrNull(T[] array) {
        if (array.length == 0) return null;
        return array[array.length - 1];
    }
    
    /**
     * @return the last element of the given iterable
     * @throws NoSuchElementException if the given iterable is empty
     */
    public static <T> T lastElement(Iterable<T> iterable) throws NoSuchElementException {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) throw new NoSuchElementException();
        T result = iterator.next();
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> Iterator<E> emptyIterator() {
        return (Iterator<E>)EMPTY_ITERATOR;
    }
    
    /**
     * Counts the number of occurrences of given {@code element} in the iterable.
     */
    // TODO pass in {@link Identificator}
    public static <E> int count(Iterable<E> iterable, E element) {
        int count = 0;
        for (E e: iterable) {
            if (element.equals(e)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * @return true iff both {@link Iterable}s iterate the equal sequence of elements
     */
    
    public static <E> boolean iteratesEqualSequence(Iterable<E> iterable1, Iterable<E> iterable2) {
        return iteratesEqualSequence(iterable1, iterable2, getObjectIdentificator());
    }
    
    public static <E> boolean iteratesEqualSequence(Iterable<E> iterable1, Iterable<E> iterable2, Identificator<? super E> identificator) {
        Iterator<E> iterator1 = iterable1.iterator();
        Iterator<E> iterator2 = iterable2.iterator();
        while (iterator1.hasNext()) {
            if (!iterator2.hasNext()) {
                return false;
            }
            E current1 = iterator1.next();
            E current2 = iterator2.next();
            if (current1 == null || current2 == null) {
                if (current1 != current2) {
                    return false;
                }
            } else {
                if (!identificator.equals(current1, current2)) {
                    return false;
                }
            }
        }
        if (iterator2.hasNext()) {
            return false;
        }
        return true;
    }
    
    /**
     * Creates a new list of all the elements for which the given {@code predicate} evaluates to true.
     */
    public static <T> ArrayList<T> filter(Predicate<? super T> predicate, Iterable<T> elements) {
        Builder<T> builder = ArrayList.<T>newBuilder();
        for (T element: elements) {
            if (!predicate.test(element)) continue;
            builder.add(element);
        }
        return builder.build();
    }
    
    /**
     * Finds the first index of the given element in the iteration
     * or returns -1 if the element is not contained in the iteration.
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using {@link Object#equals(Object)}</li>
     * </ul>
     */
    public static <E> int indexOf(Iterable<E> iterable, @CheckForNull E element) {
        return indexOf(iterable, element, getObjectIdentificator());
    }
    
    /**
     * Finds the first index of the given element in the iteration
     * or returns -1 if the element is not contained in the iteration.
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using the given {@code identificator}</li>
     * </ul>
     */
    public static <E> int indexOf(Iterable<E> iterable, @CheckForNull E element, Identificator<? super E> identificator) {
        int i = 0;
        for (E current: iterable) {
            if (element == null) {
                if (current == null) {
                    return i;
                }
            } else if (identificator.equals(element, current)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    /**
     * @see CollectionUtil#indexOf(Iterable, Object, Identificator)
     */
    public static <E> int indexOf(Container<E> container, E element) {
        PreConditions.paramNotNull(element);
        final Identificator<? super E> identificator = container.getIdentificator();
        int i = 0;
        for (E current: container) {
            if (identificator.equals(element, current)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    /**
     * Finds the first index of the given element in the array
     * or returns -1 if the element is not contained in the array.
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the length of the array!</li>
     * <li>The element is matched using {@link Object#equals(Object)}</li>
     * </ul>
     */
    public static <T> int indexOf(T[] array, @CheckForNull T element) {
        for (int i = 0; i < array.length; ++i) {
            if (ObjectUtil.objectEquals(element, array[i])) return i;
        }
        return -1;
    }
    
    /**
     * Tests if an element is contained in an {@code Iterable}.
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using {@link Object#equals(Object)}</li>
     * </ul>
     *
     * @return true if the given element is found
     */
    public static <E> boolean contains(Iterable<E> iterable, @CheckForNull E element) {
        for (E current: iterable) {
            if (ObjectUtil.objectEquals(element, current)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tests if an element is contained in an {@code Iterable}.
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using {@link Collection#getIdentificator()}</li>
     * </ul>
     *
     * @return true if the given element is found
     */
    public static <E> boolean contains(Collection<E> collection, E element) {
        final Identificator<? super E> identificator = collection.getIdentificator();
        for (E current: collection) {
            if (identificator.equals(element, current)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds the first element in the {@code Iterable} that is equal to the given {@code element} or
     * returns null;
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using {@link Object#equals(Object)}</li>
     * </ul>
     *
     * @return true if the given element is found
     */
    @CheckForNull
    public static <E> E firstEqualOrNull(Iterable<E> iterable, @CheckForNull E element) {
        for (E current: iterable) {
            if (ObjectUtil.objectEquals(element, current)) {
                return current;
            }
        }
        return null;
    }
    
    /**
     * Finds the first element in the {@code Collection} that is equal to the given {@code element} or
     * returns null;
     * <p>
     * NOTE:
     * <ul>
     * <li>Runs in O(n) where n is the number of elements in the iteration!</li>
     * <li>The element is matched using {@link Collection#getIdentificator()}</li>
     * </ul>
     *
     * @return true if the given element is found
     */
    @CheckForNull
    public static <E> E firstEqualOrNull(Collection<E> collection, E element) {
        final Identificator<? super E> identificator = collection.getIdentificator();
        for (E current: collection) {
            if (identificator.equals(element, current)) {
                return current;
            }
        }
        return null;
    }
    
    /**
     * Sorts the elements of the given collection using their natural ordering.
     * 
     * @return a new collection with the sorted elements
     * @see Comparable
     */
    @CheckReturnValue
    public static <E extends Comparable<E>, C extends OrderedCollection<E>> C sort(Collection<? super E> collection,
            CollectionBuilder<E, ? extends C> builder) {
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>)Comparable.class;
        return sort(collection, builder, new DefaultComparator<E>(clazz));
    }
    
    /**
     * Sorts the elements of the given collection using the given {@link Comparator}.
     * 
     * @return a new collection with the sorted elements
     */
    @CheckReturnValue
    public static <E, C extends OrderedCollection<E>> C sort(Collection<? super E> collection, CollectionBuilder<E, C> builder,
            java.util.Comparator<? super E> comparator) {
        E[] array = ArrayUtil.unsafeCast(toObjectArray(collection));
        Arrays.sort(array, comparator);
        return builder.addAll(wrap(array)).build();
    }
    
    /**
     * Sorts the elements of the given collection using the given {@link Comparator}.
     * 
     * @return an instance of ArrayList with the sorted elements
     */
    @CheckReturnValue
    public static <E> ArrayList<E> sort(Collection<? super E> collection, java.util.Comparator<? super E> comparator) {
        return sort(collection, ArrayList.<E>newBuilder(), comparator);
    }
    
    /**
     * Sorts the elements of the given collection using their natural ordering.
     * 
     * @return an instance of ArrayList with the sorted elements
     * @see Comparable
     */
    @CheckReturnValue
    public static <E extends Comparable<E>> ArrayList<E> sort(Collection<? super E> collection) {
        @SuppressWarnings("unchecked")
        Class<E> clazz = (Class<E>)Comparable.class;
        return sort(collection, new DefaultComparator<E>(clazz));
    }
    
    /**
     * @return the first n elements of the given collection, or the collection itself, if there are less than n elements
     *         in the collection
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public static <T> Collection<T> firstN(Collection<T> collection, int n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.valueOf(n));
        }
        if (n == 0) {
            return ImmutableCollections.listOf();
        }
        if (collection.size() <= n) {
            return collection;
        }
        ArrayList<T> result = new ArrayList<T>(n);
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; i < n; ++i) {
            result.push(iterator.next());
        }
        return result;
    }
    
    /**
     * Builds a collection which results from removing the first occurrence of {@code element} from the {@code iterable}.
     * Uses the {@code collectionBuilder} to build the returned collection.
     */
    public static <E, C extends Collection<E>> C remove(Iterable<E> iterable, E element, CollectionBuilder<E, C> collectionBuilder) {
        boolean removed = false;
        for (E e: iterable) {
            if (removed || !element.equals(e)) {
                collectionBuilder.add(element);
            }
        }
        return collectionBuilder.build();
    }
    
    /**
     * Builds a collection which results from removing all occurrences of {@code element} from the {@code iterable}.
     * Uses the {@code collectionBuilder} to build the returned collection.
     */
    public static <E, C extends Collection<E>> C removeAll(Iterable<E> iterable, E element, CollectionBuilder<E, C> collectionBuilder) {
        for (E e: iterable) {
            if (!element.equals(e)) {
                collectionBuilder.add(element);
            }
        }
        return collectionBuilder.build();
    }
    
    /**
     * Builds a collection which results from subtracting the elements of {@code iterable2} from the elements of {@code iterable1}.
     * Uses the {@code collectionBuilder} to build the returned collection.
     */
    public static <E, C extends Collection<E>> C subtract(Iterable<E> iterable1, Iterable<E> iterable2, CollectionBuilder<E, C> collectionBuilder) {
        HashBag<E> elementsToRemove = HashBag.<E>newBuilder().addAll(iterable2).build();
        for (E element: iterable1) {
            if (elementsToRemove.remove(element) == -1) {
                collectionBuilder.add(element);
            }
        }
        return collectionBuilder.build();
    }
    
    /**
     * Intersects two instances of {@link Set}.
     * <p>
     * The elements in the returned set have no defined order.
     *
     * @return a set containing those elements that are contained in both of the given sets
     */
    public static <E> Set<E> intersect(Set<E> set1, Set<E> set2) {
        return intersect(set1, set2, HashSet.<E>newBuilder());
    }
    
    /**
     * Intersects two instances of {@link Set}.
     * <p>
     * The elements in the returned set have no defined order.
     *
     * @return a set containing those elements that are contained in both of the given sets
     */
    public static <E, CO extends Set<E>, B extends CollectionBuilder<E, CO>> CO intersect(
            Set<E> set1, Set<E> set2, B builder) {
        return _intersect(set1, set2, builder);
    }
    
    /**
     * Intersects two {@link OrderedSet}.
     * <p>
     * The returned {@link OrderedSet} contains exactly those elements which are contained in both of the given sets.
     * The relative order of the elements is maintained if it is consistent in both given sets. I.e.,
     * if for all elements {@code e1} and {@code e2} that are present in both sets
     *    {@code indexOf(set1, e1) <= indexOf(set1, e2)}
     * if and only if
     *    {@code indexOf(set2, e1) <= indexOf(set2, e2)}
     * then {@code e1} will be added to the resulting set before {@code e2}. Otherwise, if the order is not
     * consistent in the given sets, the result order is undefined.
     */
    public static <E> OrderedSet<E> intersectOrdered(OrderedSet<E> set1, OrderedSet<E> set2) {
        return _intersect(set1, set2, IndexedHashSet.<E>newBuilder());
    }
    
    /**
     * Intersects two instances of {@link Container}.
     * <p>
     * The returned collection contains exactly those elements which are contained in both of the given collections.
     * The relative order of the elements is maintained if it is consistent in both given collections. I.e.,
     * if for all elements {@code e1} and {@code e2} that are present in both collections
     *    {@code indexOf(collection1, e1) <= indexOf(collection1, e2)}
     * if and only if
     *    {@code indexOf(collection2, e1) <= indexOf(collection2, e2)}
     * then {@code e1} will be added to the resulting set before {@code e2}. Otherwise, if the order is not
     * consistent in the given sets, the result order is undefined.
     *
     * @throws IllegalArgumentException if the given sets do not have the same {@link Identificator}
     */
    private static <E, C extends Container<E>, B extends CollectionBuilder<E, C>> C _intersect(
            Container<E> collection1, Container<E> collection2, B collectionBuilder) {
        
        if (!collection1.getIdentificator().equals(collection2.getIdentificator())) {
            throw new IllegalArgumentException("Given sets have different identificators!");
        }
        
        Container<E> iterate, probe;
        if (collection1.size() < collection2.size()) {
            iterate = collection1;
            probe = collection2;
        } else {
            iterate = collection2;
            probe = collection1;
        }
        
        for (E current: iterate) {
            if (!probe.contains(current)) continue;
            collectionBuilder.add(current);
        }
        return collectionBuilder.build();
    }
    
    @SafeVarargs
    public static <E> Iterable<E> join(Iterable<E>... iterables) {
        return new CompoundIterable<E>(wrap(iterables));
    }
    
    public static String toSetString(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (Object element: iterable) {
            sb.append(element.toString());
            sb.append(", ");
        }
        if (sb.length() == 1) {
            return "{}";
        }
        sb.setCharAt(sb.length() - 2, '}');
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
    
    public static String toListString(Iterable<?> iterable) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Object element: iterable) {
            sb.append(element.toString());
            sb.append(", ");
        }
        if (sb.length() == 1) {
            return "[]";
        }
        sb.setCharAt(sb.length() - 2, ']');
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
    
    public static <E> List<E> reverse(List<E> list) {
        return new ReverseList<>(list);
    }
    
    public static <E> Iterable<E> shuffle(IndexedCollection<E> collection) {
        return shuffle(collection, new Random());
    }
    
    public static <E> Iterable<E> shuffle(IndexedCollection<E> collection, Random random) {
        return new ShuffledIterable<E>(collection, random);
    }
    
    public static <E> E[] toArray(Collection<E> collection, E[] array) {
        int size = collection.size();
        if (array.length != size) {
            array = ArrayUtil.newArray(array, size);
        }
        fillArray(collection, array);
        return array;
    }
    
    public static Object[] toObjectArray(Collection<?> collection) {
        int size = collection.size();
        Object[] result = new Object[size];
        fillArray(collection, result);
        return result;
    }
    
    private static <E> void fillArray(Collection<? extends E> collection, E[] array) {
        int size = array.length;
        int i = 0;
        for (E element: collection) {
            if (i == size) {
                throw new ConcurrentModificationException();
            }
            array[i] = element;
            i++;
        }
        if (size != i) {
            throw new ConcurrentModificationException();
        }
    }
    
    /**
     * Wraps the given elements in a {@code Collection} of unspecified subtype.
     * <p>
     * If you need a specific type, use the methods in either 
     * {@link ImmutableCollections} or {@link MutableCollections}.
     */
    @SafeVarargs
    public static <E> Collection<E> wrap(final E... elements) {
        PreConditions.paramNotNull(elements);
        return new Collection<E>() {
            
            @Override
            public Iterator<E> iterator() {
                return new ArrayIterator<>(elements);
            }
            
            @Override
            public int size() {
                return elements.length;
            }
            
            @Override
            @CheckForNull
            public E getFirstOrNull() {
                return isEmpty() ? null : elements[0];
            }
        };
    }
    
    private static class ObjectIdentificator implements Identificator<Object> {
        @Override
        public boolean isIdentifiable(Object object) {
            return true;
        }
        
        @Override
        public boolean equals(Object object1, Object object2) {
            // TODO is it faster to skip == here?
            return /*(object1 == object2) || */object1.equals(object2);
        }
        
        @Override
        public int hashCode(Object object) {
            return ObjectUtil.strengthenedHashcode(object.hashCode());
        }
        
        @Override
        public boolean equals(@CheckForNull Object object) {
            if (object == null) {
                return false;
            }
            return object.getClass() == this.getClass();
        }
        
        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }
    
    /**
     * Compares element using {@link Comparable#compareTo(Object)}.
     *
     * @param <T> Type to compare: a subclass of {@link Comparable}.
     */
    private static class DefaultComparator<T extends Comparable<T>> implements Comparator<T> {
        
        private final Class<T> class_;
        
        public DefaultComparator(Class<T> class_) {
            this.class_ = class_;
        }
        
        @Override
        public int compare(T o1, T o2) {
            return o1.compareTo(o2);
        }
        
        @Override
        public boolean isIdentifiable(Object object) {
            return class_.isInstance(object);
        }
        
        @Override
        public boolean equals(T object1, T object2) {
            // TODO What is the performance difference over using equals?
            return compare(object1, object2) == 0;
        }
        
        @Override
        public int hashCode(T object) {
            return object.hashCode();
        }
        
        // Methods inherited from java.lang.Object
        
        @Override
        public boolean equals(@CheckForNull Object object) {
            DefaultComparator<?> other = ObjectUtil.castOrNull(object, DefaultComparator.class);
            if (other == null) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return class_.hashCode() + 399722917;
        }
        
    }
    
    private static final Iterator<Object> EMPTY_ITERATOR = Collections.emptyIterator();
    
    private static final Identificator<Object> OBJECT_IDENTIFICATOR = new ObjectIdentificator();
    
}
