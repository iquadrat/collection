package bench.org.povworld.collection;

import java.util.Random;

import org.povworld.collection.mutable.HashSet;

public class IntegerProducer implements ElementProducer<Integer> {
    
    private final Random random = new Random(42);
    
    private final HashSet<Integer> produced = new HashSet<>();
    
    @Override
    public Integer produce() {
        while (true) {
            Integer candidate = Integer.valueOf(random.nextInt());
            if (produced.add(candidate)) {
                return candidate;
            }
        }
    }
    
}
