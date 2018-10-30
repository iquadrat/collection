package test.org.povworld.collection;

import org.povworld.collection.CollectionBuilder;
import org.povworld.collection.Set;

public abstract class AbstractSetTest<C extends Set<String>> extends AbstractNoDuplicatesTest<C> {
    
    public AbstractSetTest(CollectionBuilder<String, ? extends C> builder) {
        super(builder);
    }
    
    @Override
    protected Iterable<String> expectedOrder(Iterable<String> elements) {
        return null;
    }
    
}
