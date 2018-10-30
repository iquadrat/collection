package test.org.povworld.collection.mutable;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;
import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.ObjectUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.IntrusiveLinkedSequence;
import org.povworld.collection.mutable.IntrusiveLinkedSequence.ElementLink;

public class IntrusiveLinkedCollectionTest
        extends
        AbstractIntrusiveLinkedSequenceTest<IntrusiveLinkedCollectionTest.StringLink, IntrusiveLinkedSequence<IntrusiveLinkedCollectionTest.StringLink>> {
    
    public static class StringLink extends IntrusiveLinkedSequence.AbstractLink<StringLink> {
        
        private final int data;
        
        private final String string;
        
        public StringLink(int data, String string) {
            this.data = data;
            this.string = string;
        }
        
        @Override
        public int hashCode() {
            return 31 * data + string.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            StringLink other = ObjectUtil.castOrNull(obj, StringLink.class);
            if (other == null)
                return false;
            
            return (data == other.data) && string.equals(other.string);
        }
        
        @Override
        public StringLink clone() throws CloneNotSupportedException {
            return (StringLink)super.clone();
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + data + "," + string + "]";
        }
        
    }
    
    public IntrusiveLinkedCollectionTest() throws CloneNotSupportedException {
        super(new TestBuilder(IntrusiveLinkedSequence.<StringLink>newBuilder()),
                createManyElements());
        
    }
    
    private static class TestBuilder extends AbstractCollectionBuilder<StringLink, IntrusiveLinkedSequence<StringLink>> implements
            IntrusiveLinkedCollectionBuilder<StringLink, IntrusiveLinkedSequence<StringLink>> {
        
        private final CollectionBuilder<StringLink, IntrusiveLinkedSequence<StringLink>> delegate;
        
        public TestBuilder(
                CollectionBuilder<StringLink, IntrusiveLinkedSequence<StringLink>> delegate) {
            this.delegate = delegate;
        }
        
        @Override
        protected void _add(StringLink element) {
            delegate.add(createDetachedLinkWithSameValue(element));
        }
        
        @Override
        protected IntrusiveLinkedSequence<StringLink> _createCollection() {
            return delegate.build();
        }
        
        @Override
        protected void _reset() {
            delegate.reset();
        }
        
        @Override
        public StringLink createDetachedLinkWithSameValue(StringLink link) {
            return new StringLink(link.data, link.string);
        }
    }
    
    private static StringLink[] createManyElements() {
        StringLink[] array = new StringLink[1004];
        for (int i = 0; i < array.length; ++i) {
            array[i] = new StringLink(i, String.valueOf(i));
        }
        return array;
    }
    
    @Override
    protected Iterator<StringLink> modifyingIterator(IntrusiveLinkedSequence<StringLink> collection) {
        return collection.modifyingIterator();
    }
    
    @Test
    public void moveToFront() {
        collection.moveToFront(link1);
        assertEquals(ImmutableCollections.listOf(link1, link2, link3), collection);
        
        collection.moveToFront(link2);
        assertEquals(ImmutableCollections.listOf(link2, link1, link3), collection);
        
        collection.moveToFront(link3);
        assertEquals(ImmutableCollections.listOf(link3, link2, link1), collection);
        assertEquals(3, collection.size());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void moveToFrontFailsForDetachedLink() {
        collection.remove(link3);
        collection.moveToFront(link3);
    }
    
    @Test
    public void reinsertLinksAfterRemove() {
        collection.remove(link1);
        collection.remove(link2);
        collection.remove(link3);
        
        collection.insertFront(link1);
        collection.insertFront(link2);
        collection.insertFront(link3);
        assertEquals(ImmutableCollections.listOf(link3, link2, link1), collection);
    }
    
    @Test
    public void setLinkElement() {
        ElementLink<Integer> link = new ElementLink<>(42);
        assertEquals(Integer.valueOf(42), link.getElement());
        
        link.setValue(11);
        assertEquals(Integer.valueOf(11), link.getElement());
    }
}
