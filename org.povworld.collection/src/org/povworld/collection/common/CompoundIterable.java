package org.povworld.collection.common;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Unrolls a nested iterable. The outer iterable may not contain {@code null} values!
 *  
 * @author micha
 *
 * @param <T> value type
 */
// TODO create CompoundSequence
@NotThreadSafe
public class CompoundIterable<T> implements Iterable<T> {
    
    protected final Iterable<? extends Iterable<? extends T>> iterables;
    
    public static <T> CompoundIterable<T> create(Iterable<? extends Iterable<? extends T>> iterables) {
        return new CompoundIterable<T>(iterables);
    }
    
    @SafeVarargs
    public static <T> CompoundIterable<T> create(Iterable<? extends T>... values) {
        return new CompoundIterable<T>(Arrays.asList(values));
    }
    
    public CompoundIterable(Iterable<? extends Iterable<? extends T>> iterables) {
        this.iterables = iterables;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new CompoundIterator();
    }
    
    private class CompoundIterator implements Iterator<T> {
        
        private final Iterator<? extends Iterable<? extends T>> valueIter;
        
        /**
         * Invariant: currentIter == null || currentIter.hasNext() == true
         */
        @CheckForNull
        private Iterator<? extends T> currentIter;
        
        public CompoundIterator() {
            valueIter = iterables.iterator();
            findNextIter();
        }
        
        @Override
        public boolean hasNext() {
            return currentIter != null;
        }
        
        private void findNextIter() {
            do {
                if (!valueIter.hasNext()) {
                    currentIter = null;
                    return;
                }
                currentIter = valueIter.next().iterator();
            } while (!currentIter.hasNext());
        }
        
        @Override
        public T next() {
            if (currentIter == null) throw new NoSuchElementException();
            T result = currentIter.next();
            if (!currentIter.hasNext()) findNextIter();
            return result;
        }
    }
    
}
