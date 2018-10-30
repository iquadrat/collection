package bench.org.povworld.collection.mutable;

import org.jbenchx.annotations.Bench;
import org.jbenchx.annotations.ForEachInt;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.List;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.mutable.HashSet;

public class ShortListVsSet {
    
    private ArrayList<String> list;
    
    private HashSet<String> set;
    
    private List<String> test;
    
    public ShortListVsSet(@ForEachInt({0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}) int size) {
        this.list = new ArrayList<>(size);
        this.set = new HashSet<>(size);
        
        List<String> fruits = ImmutableCollections.listOf("apple", "orange", "peach", "coconut", "strawberry", "kiwi", "banana", "blueberry",
                "ananas", "pomegrade");
        for (int i = 0; i < size; ++i) {
            String s = fruits.get(i);
            list.add(s);
            set.add(s);
        }
        test = ImmutableCollections.listOf("foo", "bar", "bogus", "apple", "banana", "ananas");
    }
    
    @Bench
    public Object buildArrayList() {
        ArrayList<String> l = new ArrayList<>(list.size());
        for(String s: list) {
            l.push(s);
        }
        return l;
    }
    
    @Bench
    public Object buildHashSet() {
        HashSet<String> l = new HashSet<>(list.size());
        for(String s: list) {
            l.add(s);
        }
        return l;
    }
    
//    @Bench
//    public int containsInArrayList() {
//        int count = 0;
//        for (String s: test) {
//            count += CollectionUtil.contains(list, s) ? 1 : 0;
//        }
//        return count;
//    }
//    
//    @Bench
//    public int containsHashSet() {
//        int count = 0;
//        for (String s: test) {
//            count += set.contains(s) ? 1 : 0;
//        }
//        return count;
//    }
//    
}
