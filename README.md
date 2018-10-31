# Java Collection Library

This library provides alternative collection interfaces and implementations to the ones shipped with the
Java Collections Framework in the `java.util` package. The library follows a different set of principles,
which are listed below.

#### Table of Contents
[Principles](#principles)

[Requiremenets](#requirements)

[Getting Started](#getting-started)

## Principles
### Strong Typing: Interfaces are a Contract
The collection library provides fine-granular interfaces for the collection classes. Interfaces are separated in read-only
and modifyable parts which makes it possible to take a collection with its read-only interface as a param signaling
that the method is not going to modify its contents. For each read-only interface there is also a immutable variant
which goes a step further in guaranteeing that the collection content never changes. This guarantees thread-safety and
allows for easy sharing of collection instances.

Implementations implement all the methods of their interfaces (no throwing of `UnsupportedOperationException`). 
The only exception to this is `Iterator` -- we use the `java.util.Iterable` to take advantage of the for-each loop --
which does not implement `remove` if taken from the read-only interface (some collections implement a `modifyingIterator`
method which allows removal during iteration).

### Only Implement Efficient Operations
Each collection provides only those operations that it can implement efficiently. This generally means in `O(log n)`
where `n` is the number of elements in the collection or `O(m * log(n + m))` for operations that take `m` elements
as parameter. This helps writing programs that scale well by forcing to choose the collection that supports the
needed operations efficiently.

### Reasonable Memory-Computation Compromises
The implementations proivde reasonable compromises between memory and CPU consumption. It is probably not fastest
nor the most memory efficent out there but up to a certain level both goals are well aligned and the implementation
are quite efficient in both dimensions.

## Requiremenets
The library has been developed with Java 8 which is the minimum version needed. Due to the backwards-compability,
it should also work with higher versions of Java -- this has not been tested though. If you run into issues please
report them.

The only dependency is the JSR305 annotations which are mainly used to document where `null` is allowed or returned.

Unit tests are written using [JUnit 5](https://junit.org), [Mockito](https://site.mockito.org/) and 
[Truth](https://github.com/google/truth).

## Getting Started

### The Hierarchy
![Interfce Hierarchy](/doc/images/basic-interfaces.gif)

`Collection` is the base interface for all the single-dimension collections. Basically, it just allows to query for the
size and iterate its elements.

Collections are divided into those which have a well-defined order of the elements (`OrderedCollection`) and those
that do not (`UnOrderedCollection`). Note that these interfaces define exactly how `equals` must be implemented.
An ordered collection can never be equal to an unordered one and vice versa. When writing APIs this property is usally
essential (especially for testing and mocking), therefore a sub-type of `OrderedCollection` or `UnOrderedCollection`
should be preferred over using `Collection`.

The interfaces `OrderedSet`, `Sequence`, `List`, `Set` and `Bag` are the main interfaces to choose from. They provide
different set of efficient operations, so you want to prefer one over the other based on your needs. If all you
need to do is to collect elements that you will later iterate over and you do not care about duplicates the
`ArrayList` is your candidate. If you want to eliminate duplicates, it is most of the `HashSet`. For more advanced
uses see the table below.

| Name                              | Ordered | Duplicates | Containment Check | Natural Order  | Get by Index | Insert at Front |
|-----------------------------------|:-------:|:----------:|:-----------------:|:--------------:|:------------:|:---------------:|
| ArrayList                         | Yes     | Yes        | No                | No             | Yes          | No              |
| HashBag                           | No      | Yes        | Yes               | No             | No           | No              |
| HashList                          | No      | Yes        | Yes               | No             | Yes          | No              |
| HashSet                           | No      | No         | Yes               | No             | No           | No              |
| IndexedHashSet                    | Yes     | No         | Yes               | No             | Yes          | No              |
| TreeList                          | Yes     | Yes        | Yes               | No             | Yes          | Yes             |
| TreeSequence                      | Yes     | Yes        | Yes               | Yes            | No           | No              |
| TreeSet                           | Yes     | No         | Yes               | Yes            | No           | No              |
| LinkedSequence                    | Yes     | Yes        | No                | No             | No           | Yes             |
| IntrusiveLinkedSequence           | Yes     | Yes        | No                | No             | No           | Yes             |

The `TreeList` has the additional advantage that is supports insertion (and removal) at an arbitrary index, not just at front and back
like the `LinkedSequence`. The (`Concurrent`)`IntrusiveLinkedSequence` allows to customize the link objects that compose its doubly linked list
and removal by pointer to the link.

Not listed above is the `ConcurrentIntrusiveLinkedSequence` which has the same properties as `IntrusiveLinkedSequence`
but supports concurrent operations. All the other classes above are not thread-safe, you need to properly guard them
against concurrent access if multiple threads read or modify them.

### Building and Changing Collections

TODO

### Maps

TODO

### Persistent Datastructures

TODO

### Interacting with Java Collections Framework

TODO

