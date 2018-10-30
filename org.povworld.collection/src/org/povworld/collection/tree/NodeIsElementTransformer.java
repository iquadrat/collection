package org.povworld.collection.tree;

public class NodeIsElementTransformer<N> implements NodeElementTransformer<N, N> {

    private NodeIsElementTransformer() {
    }
    
    @Override
    public N getElement(N node) {
        return node;
    }

    @Override
    public N createNode(N element) {
        return element;
    }
    
    public static <N> NodeElementTransformer<N, N> getInstance() {
        return new NodeIsElementTransformer<N>();
    }
    
}
