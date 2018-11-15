package test.org.povworld.collection;

import org.povworld.collection.Collection;
import org.povworld.collection.CollectionBuilder;

public abstract class AbstractStringCollectionTest<C extends Collection<String>> extends AbstractCollectionTest<String, C> {
    
    public AbstractStringCollectionTest(
            CollectionBuilder<String, ? extends C> builder) {
        super(builder, createManyStrings());
    }
    
    private static String[] createManyStrings() {
        String[] array = new String[1004];
        array[0] = "foobar";
        array[1] = "one";
        array[2] = "two";
        array[3] = "three";
        for (int i = 4; i < 1004; ++i) {
            array[i] = String.valueOf(i - 4);
        }
        return array;
    }
    
}
