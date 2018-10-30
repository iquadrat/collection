package bench.org.povworld.collection.persistent;

import bench.org.povworld.collection.StringProducer;

public abstract class PersistentMapBenchString extends PersistentMapBench<String> {
    
    public PersistentMapBenchString(int keyCount) {
        super(keyCount, StringProducer.createDefaultKeyProducer(), StringProducer.createDefaultValueProducer());
    }
    
}
