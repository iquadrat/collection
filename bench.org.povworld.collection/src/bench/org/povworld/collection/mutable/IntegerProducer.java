package bench.org.povworld.collection.mutable;

import java.util.Random;

import org.povworld.collection.mutable.HashSet;

import bench.org.povworld.collection.ElementProducer;

public class IntegerProducer implements ElementProducer<Integer> {
    
    private final Random random = new Random(42);
    
    private final HashSet<Integer> produced = new HashSet<>();
    
    @Override
    public Integer produce() {
        Integer candidate;
        do {
            candidate = random.nextInt();
        } while (!produced.add(candidate));
        return candidate;
    }
    
}
