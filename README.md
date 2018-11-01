# Java Collection Library

This library provides alternative collection interfaces and implementations other than the ones shipped with the
Java Collections Framework in the `java.util` package. The library follows a different set of principles,
which are listed below.

#### Table of Contents
[Principles](#principles)

[Requirements](#requirements)

[Getting Started](#getting-started)

## Principles
### Strong Typing: Interfaces as a Contract
The collection library provides fine-granular interfaces for the collection classes. Interfaces are separated in read-only
and modifyable parts which makes it possible to take a collection with its read-only interface as a method argument signaling
that the method is not going to modify the argument's contents. For each read-only interface there is also a immutable variant
which goes a step further in guaranteeing that the collection content never changes. This guarantees thread-safety and
allows for easy sharing of collection instances.

Classes implement all the methods of their interfaces (no throwing of `UnsupportedOperationException`). 
The only exception to this is `Iterator` -- we use `java.util.Iterable` to take advantage of the for-each loop --
which does not implement the `remove()` operation if returned by a read-only interface (some collections implement a `modifyingIterator()` method which allows removal during iteration).

### Support Efficient Operations Only
Each collection provides only those operations which it can implement efficiently. This generally means in `O(log n)`
where `n` is the number of elements in the collection (or `O(m * log(n + m))` for operations that take `m` elements
as argument). This helps writing programs that scale well by forcing to choose the collection that supports the
needed operations efficiently.

### Reasonable Memory-CPU Compromises
The implementations provide reasonable compromises between memory and CPU consumption. It is probably not the fastest
nor the most memory efficient out there but up to a certain level both goals are well aligned and the implementation
are quite efficient in both dimensions.

### Disallow `null` elements
The collections generally cannot contain `null` elements. `null` is used in some places to signal the absence of
an element. Allowing `null`as element whould make these conditions ambiguous.

## Requirements
The library has been developed with Java 8 which is the minimum version needed. Due to the backwards-compability,
it should also work with higher versions of Java -- this has not been tested though. If you run into issues please
report them.

The only dependency is the JSR305 annotations which are mainly used to document where `null` is allowed or returned.

Unit tests are written using [JUnit 5](https://junit.org), [Mockito](https://site.mockito.org/) and 
[Truth](https://github.com/google/truth).

## Getting Started

### The Hierarchy

<p align="center">
  <img src="/doc/images/basic-interfaces.gif" width="75%" title="Interface Hierarchy">
</p>

`Collection` is the base interface for all the single-dimension collections. Basically, it just allows to query for the
size and to iterate its elements.

Collections are divided into those which have a well-defined order of the elements (`OrderedCollection`) and those
that do not (`UnOrderedCollection`). Note that these interfaces define exactly how `equals` must be implemented.
An ordered collection can never be equal to an unordered one and vice versa. When writing APIs this property is usally
essential (especially for testing and mocking), therefore a sub-type of `OrderedCollection` or `UnOrderedCollection`
should be preferred over using `Collection`.

The interfaces `OrderedSet`, `Sequence`, `List`, `Set` and `Bag` are the main interfaces to choose from. They provide
different sets of operations, so you want to prefer one over the other based on your needs. If all you
need to do is collecting elements that you will later iterate over and you do not care about duplicates, the
`ArrayList` is your friend. If you want to eliminate duplicates, it is most likely the `HashSet`. For more advanced
uses see the following table.

| Name                              | Ordered | Duplicates | Containment Check | Natural Order  | Get by Index | Insert at Front |
|-----------------------------------|:-------:|:----------:|:-----------------:|:--------------:|:------------:|:---------------:|
| [ArrayList](org.povworld.collection/src/org/povworld/collection/mutable/ArrayList.java)   | Yes     | Yes        | No                | No             | Yes          | No              |
| [HashBag](org.povworld.collection/src/org/povworld/collection/mutable/HashBag.java)        | No      | Yes        | Yes               | No             | No           | No              |
| [HashList](org.povworld.collection/src/org/povworld/collection/mutable/HashList.java)     | Yes     | Yes        | Yes               | No             | Yes          | No              |
| [HashSet](org.povworld.collection/src/org/povworld/collection/mutable/HashSet.java)              | No      | No         | Yes               | No             | No           | No              |
| [IndexedHashSet](org.povworld.collection/src/org/povworld/collection/mutable/IndexedHashSet.java)   | Yes     | No         | Yes               | No             | Yes          | No              |
| [TreeList](org.povworld.collection/src/org/povworld/collection/mutable/TreeList.java)               | Yes     | Yes        | Yes               | No             | Yes          | Yes             |
| [TreeSequence](org.povworld.collection/src/org/povworld/collection/mutable/TreeSequence.java)                      | Yes     | Yes        | Yes               | Yes            | No           | No              |
| [TreeSet](org.povworld.collection/src/org/povworld/collection/mutable/TreeSet.java)                 | Yes     | No         | Yes               | Yes            | No           | No              |
| [LinkedSequence](org.povworld.collection/src/org/povworld/collection/mutable/LinkedSequence.java)   | Yes     | Yes        | No                | No             | No           | Yes             |
| [IntrusiveLinkedSequence](org.povworld.collection/src/org/povworld/collection/mutable/IntrusiveLinkedSequence.java)    | Yes     | Yes        | No                | No             | No           | Yes             |

'Natural Order' means that the elements are always kept ordered according to a given ordering relation.

The `TreeList` has the additional advantage that is supports insertion (and removal) at an arbitrary index, 
not just at front and back like the `LinkedSequence`. The `IntrusiveLinkedSequence` allows to 
customize the link objects that compose its doubly linked list and removal by pointer to the link.

Not listed above is the [`ConcurrentIntrusiveLinkedSequence`](org.povworld.collection/src/org/povworld/collection/mutable/ConcurrentIntrusiveLinkedSequence)
which has the same properties as `IntrusiveLinkedSequence`
but supports concurrent operations. All the other classes above are not thread-safe, you need to properly guard them
against concurrent access if multiple threads read or modify them.

### Modifying Collections

One thing that you might find odd at first when used to work with the Java Collections Framework is that
interfaces like `List` or `Set` do not provide any modification methods to add, remove elements etc.
This is on purpose. To be able to modify a collection you need to have a reference to the concrete type.
So, instead of 

<pre><code>
public class NameRepository {
   
     private final <b>Set</b>&lt;String&gt; usedNames = new HashSet<>();

     public void registerName(String name) {
       if (!usedNames.add(name)) {  <b>// Ooops, no 'add' on Set...</b>
         throw new IllegalStateException("Already registered");
       }
     }
}
</pre></code>

write

<pre><code>
public class NameRepository<Foo> {
   
     private final <b>HashSet</b>&lt;String&gt; usedNames = new HashSet<>();

     public void registerName(String name) {
       if (!usedNames.add(name)) { <b>// ... but there is on HashSet!</b>
         throw new IllegalStateException("Already registered");
       }
     }
}
</pre></code>

This allows you to limit the code that has write-access to the collection as strictly as possible, while
still allowing to pass a collection around:

```java
public class NameRepository<Foo> {

     //...
     
     List<String> getAllNamesAlphabeticallySorted() {
       // Passes 'usedNames' as read-only interface to the 'sort' method.
       return CollectionUtil.sort(usedNames);
     }

}
```
In this example `CollectionUtil.sort` takes a `Collection` argument indicating that it does not need
to modify its contents (though it would be principally possible by casting it). Instead, it creates 
and returns a new `ArrayList` to hold the sorted elements.

### Builders

Another way to create collection instances is through their builders. A builder can be passed around
if elements need to be collected from different places. Most collections have a static `newBuilder()` method
that returns a `CollectionBuilder` for the collection.

An example collecting all permutations of a string in a recursive implementation:
```java
class Permutations {
    public static ImmutableSet<String> permutations(String s) {
        ImmutableHashSet.Builder<String> permutations = ImmutableHashSet.newBuilder();
        permutations("", s, permutations);
        return permutations.build();
    }
    
    private static void permutations(String prefix, String s, CollectionBuilder<String, ?> builder) {
        if (s.length() == 1) {
            builder.add(prefix + s);
        } else {
            for (int i = 0; i < s.length(); ++i) {
                permutations(prefix + s.charAt(i), s.substring(0, i) + s.substring(i + 1), builder);
            }
        }
    }
}
```

Builders are the only way to create instances of any implementation of `ImmutableCollection`. Immutable collections
are guaranteed to never change after creation. You can also use the static methods of `ImmutableCollections`
like `listOf(...)`, `setOf(...)` etc which create immutable collections indirectly by using the builders.

### Maps

Maps are associative containers represented by the `Map` interface. The two mutable main implementations are
`HashMap` and `TreeMap` which use hashing respectively a balanced tree to store the entries.

Implementations of `MultiMap` and `ListMultiMap` can be used when multiple values have to be associated
with the same key. `MultiMap` stores the values in a set (so, no order and no duplicates) while `ListMultiMap`
stores the values in a list.

Note that for efficiency reasons, `Map<K,V>` does not implement`Iterable<Entry<K,V>>`. This would require to
either permanently store `Entry` objects or create them on-the-fly during iteration. To iterate all entries
in a map, use the `EntryIterator<K,V>` returned by the `entryIterator()` method.

### Persistent Datastructures

A speciality of the library are the persistent collections -- collections which are immutable but it is possible
to efficiently create new collections based on an existing one with one element added, removed or changed! To
distinguish these operations from the ones of the mutable collections, they are called `with` and `without` instead
of `add` or `remove` for example.

```java
public class SnapshotableNameRepository<Foo> {
    
    private PersistentSet<String> usedNames = PersistentHashSet.empty();
    
    public void registerName(String name) {
        PersistentSet<String> updatedNames = usedNames.with(name);
        if (usedNames == updatedNames) { // 'with' returns the same object if there was no change
            throw new IllegalStateException("Already registered");
        }
        usedNames = updatedNames; 
    }
    
    public ImmutableSet<String> snapshot() {
        return usedNames; // No need to copy 'usedNames' as they are immutable!
    }
}
```

Persistent implementations are significantly slower that their mutable counterparts. But all their operations
are still `O(log n)` so they are much faster than creating copies when their size is large.

### Interacting with Java Collections Framework

To interact with classes from `java.util`, there are adapters in `org.povworld.collection` that translate 
between the different interfaces:
* `JavaAdapters` wraps collections of this library to `java.util.List`, `java.util.Set` or `java.util.Collection`.
  Note that the wrappers are read-only.
* `ListAdapter` and `SetAdapter` wrap `java.util.List` and `java.util.Set` into `List` and `Set` interfaces of
  this library.

