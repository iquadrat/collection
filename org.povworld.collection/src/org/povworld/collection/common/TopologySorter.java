package org.povworld.collection.common;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.EntryIterator;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashMap;
import org.povworld.collection.mutable.LinkedOrderedCollection;

/**
 * Class to topologically sort structures.
 * <p>
 * Sorts a collection of elements of type {@code T} in such a way
 * that for any element 'a' that is a parent of element 'b', 'a' comes
 * before 'b' in the result.
 * <p>
 * If the topology contains cycles, the result is undefined. 
 * 
 * @param <T>
 *            Element type.
 */
public class TopologySorter<T, C extends Collection<T>> {
    
    public interface Topology<T> {
        /**
         * Retrieves the (direct) parents of the given child element.
         */
        public Iterable<T> getParents(T child);
    }
    
    public static <T, C extends Collection<T>> C sort(Topology<T> topology, Iterable<T> roots,
            CollectionBuilder<T, C> collectionBuilder) {
        return new TopologySorter<T, C>(topology, collectionBuilder).sort(roots);
    }
    
    private final class Entry {
        
        ArrayList<T> list = new ArrayList<T>();
        
        int parentCount;
        
    }
    
    private final HashMap<T, Entry> dependencies = new HashMap<T, Entry>();
    
    private final LinkedOrderedCollection<T> freeNodes = new LinkedOrderedCollection<T>();
    
    private final Topology<T> topology;
    
    private final CollectionBuilder<T, C> resultBuilder;
    
    private TopologySorter(Topology<T> topology, CollectionBuilder<T, C> resultBuilder) {
        this.topology = topology;
        this.resultBuilder = resultBuilder;
    }
    
    public C sort(Iterable<T> roots) {
        for (T root: roots) {
            process(root);
        }
        
        while (!dependencies.isEmpty()) {
            
            while (freeNodes.isEmpty()) {
                resolveCycle();
            }
            
            T node = freeNodes.removeFirst();
            Entry entry = ObjectUtil.checkNotNull(dependencies.remove(node));
            resultBuilder.add(node);
            for (T dependee: entry.list) {
                if (--get(dependee).parentCount == 0) {
                    freeNodes.insertBack(dependee);
                }
            }
            
        }
        return resultBuilder.build();
    }
    
    private void resolveCycle() {
        EntryIterator<T, Entry> dependencyIterator = dependencies.entryIterator();
        while (dependencyIterator.next()) {
            if (--dependencyIterator.getCurrentValue().parentCount == 0) {
                freeNodes.insertBack(dependencyIterator.getCurrentKey());
            }
        }
    }
    
    private void process(T node) {
        if (dependencies.containsKey(node))
            return;
        
        Entry entry = new Entry();
        dependencies.put(node, entry);
        
        int parentCount = processParents(node);
        
        if (parentCount == 0) {
            freeNodes.insertBack(node);
        }
        entry.parentCount = parentCount;
    }
    
    private int processParents(T node) {
        int parentCount = 0;
        for (T parent: topology.getParents(node)) {
            if (parent.equals(node))
                continue;
            parentCount++;
            process(parent);
            get(parent).list.push(node);
        }
        return parentCount;
    }
    
    private Entry get(T parent) {
        Entry result = dependencies.get(parent);
        if (result == null) {
            // Parent dependency has already been removed. This only happens
            // when there are cycles.
            return new Entry();
        }
        return result;
    }
    
}
