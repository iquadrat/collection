package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.Identificator;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.MutableCollections;
import org.povworld.collection.mutable.TreeSequence;

import test.org.povworld.collection.AbstractContainerTest;

/**
 * Unit tests for {@link TreeSequence}.
 */
public class TreeSequenceTest extends AbstractContainerTest<TreeSequence<String>> {
    
    public TreeSequenceTest() {
        super(TreeSequence.newBuilder(String.class));
    }
    
    @Override
    protected Identificator<? super String> getIdentificator() {
        return CollectionUtil.getDefaultIdentificator(String.class);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        ArrayList<String> list = MutableCollections.asList(elements);
        list.sort(CollectionUtil.getDefaultComparator(String.class));
        return list;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
    @Test
    public void getCount() {
        TreeSequence<String> sequence = TreeSequence.create(String.class);
        sequence.add("1");
        sequence.add("2");
        sequence.add("3");
        sequence.add("2");
        sequence.add("3");
        sequence.add("3");
        
        for(int i=0; i<100; ++i) {
            sequence.add("100");
        }
        
        assertEquals(106, sequence.size());
        
        assertEquals(1, sequence.getCount("1"));
        assertEquals(2, sequence.getCount("2"));
        assertEquals(3, sequence.getCount("3"));
        assertEquals(100, sequence.getCount("100"));
    }
}
