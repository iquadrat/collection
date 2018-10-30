package bench.org.povworld.collection;

import java.util.Random;

import org.povworld.collection.common.PreConditions;
import org.povworld.collection.mutable.HashSet;

public class StringProducer implements ElementProducer<String> {
    
    private final Random random = new Random(42);
    
    private final HashSet<String> produced = new HashSet<>();
    
    private final int minLength;
    
    private final int varLength;
    
    public StringProducer(int minLength, int maxLength) {
        PreConditions.paramCheck(maxLength, "maxLength must be >4", maxLength > 4);
        PreConditions.conditionCheck("minLength must be smaller than maxLength", minLength < maxLength);
        this.minLength = minLength;
        this.varLength = maxLength - minLength + 1;
    }
    
    @Override
    public String produce() {
        for (;;) {
            String candidate = produceCandidate();
            if (produced.add(candidate)) {
                return candidate;
            }
        }
    }
    
    private String produceCandidate() {
        int length = random.nextInt(varLength) + minLength;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            int base = random.nextBoolean() ? 'a' : 'A';
            base += random.nextInt('Z' - 'A');
            sb.append((char)base);
        }
        return sb.toString();
    }
    
    public static ElementProducer<String> createDefaultElementProducer() {
        return new StringProducer(1, 10);
    }
    
    public static ElementProducer<String> createDefaultKeyProducer() {
        return new StringProducer(3, 10);
    }
    
    public static ElementProducer<String> createDefaultValueProducer() {
        return new StringProducer(3, 8);
    }
}
