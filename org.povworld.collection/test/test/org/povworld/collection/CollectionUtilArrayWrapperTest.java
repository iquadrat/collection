package test.org.povworld.collection;

import javax.annotation.CheckForNull;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.common.AbstractCollectionBuilder;
import org.povworld.collection.common.ArrayUtil;

public class CollectionUtilArrayWrapperTest extends AbstractStringCollectionTest<Collection<String>> {
    
    private static class Builder extends AbstractCollectionBuilder<String, Collection<String>> {
        
        private String[] elements = new String[0];
        
        @Override
        protected void _add(String element) {
            elements = ArrayUtil.appendArrayElement(elements, element);
        }
        
        @Override
        protected void _reset() {
            elements = new String[0];
        }
        
        @Override
        protected Collection<String> _createCollection() {
            return CollectionUtil.wrap(elements);
        }
        
    }
    
    public CollectionUtilArrayWrapperTest() {
        super(new Builder());
    }
    
    @Override
    @CheckForNull
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return elements;
    }
    
    @Override
    protected boolean allowsDuplicates() {
        return true;
    }
    
}
