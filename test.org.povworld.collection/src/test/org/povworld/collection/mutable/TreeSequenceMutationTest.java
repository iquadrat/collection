package test.org.povworld.collection.mutable;

import org.povworld.collection.Collection;
import org.povworld.collection.mutable.TreeSequence;

/**
 * Mutation tests for {@link TreeSequence}.
 */
public class TreeSequenceMutationTest extends AbstractMutableCollectionTest<TreeSequence<Integer>> {

    @Override
    protected TreeSequence<Integer> create() {
        return TreeSequence.create(Integer.class);
    }

    @Override
    protected boolean add(TreeSequence<Integer> collection, Integer element) {
        collection.add(element);
        return true;
    }

    @Override
    protected int addAll(TreeSequence<Integer> collection, Collection<Integer> elements) {
        collection.addAll(elements);
        return elements.size();
    }

    @Override
    protected boolean remove(TreeSequence<Integer> collection, Integer element) {
        return collection.remove(element);
    }

    @Override
    protected int removeAll(TreeSequence<Integer> collection, Collection<Integer> elements) {
        return collection.removeAll(elements);
    }

    @Override
    protected void clear(TreeSequence<Integer> collection) {
        collection.clear();
    }

    @Override
    protected boolean supportsRemove() {
        return true;
    }

    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
}
