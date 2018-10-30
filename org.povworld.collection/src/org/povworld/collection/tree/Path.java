package org.povworld.collection.tree;

import java.util.Arrays;

import javax.annotation.CheckForNull;

import org.povworld.collection.common.ArrayUtil;

/**
 * A path represents a sequence of nodes in a tree. For each vertex, it
 * is specified if it takes the 'left' or the 'right' child.
 * 
 * @param <N> node type
 */
// TODO implement Iterable<N>?
public final class Path<N> {

    private static final Path<?> EMPTY = Path.newBuilder(null, 0).build();
    
    private final int length;
    
    private final N[] nodes;
    
    private final boolean[] left;
    
    private Path(int length, N[] nodes, boolean[] left) {
        this.length = length;
        this.nodes = nodes;
        this.left = left;
    }
    
    @SuppressWarnings("unchecked")
    public static <N> Path<N> empty() {
        return (Path<N>)EMPTY;
    }
    
    public int length() {
        return length;
    }
    
    @CheckForNull
    public N getNode(int index) {
        if (index > length) throw new IndexOutOfBoundsException();
        return nodes[index];
    }
    
    public boolean isLeft(int index) {
        if (index >= length) throw new IndexOutOfBoundsException();
        return left[index];
    }
    
    public boolean endsInLeaf() {
        return nodes[length] == null;
    }
    
    @CheckForNull
    public N getEnd() {
        return nodes[length];
    }
    
    public N getStart() {
        return nodes[0];
    }
    
    public static <N extends TreeNode<N>> Builder<N> newBuilder(@CheckForNull N start, int estimatedLength) {
        return new Builder<>(start, estimatedLength);
    }
    
    public static class Builder<N> {
        /** Number of vertices. */
        private int length;
        
        /** Node array of size 'length+1'. */
        private N[] nodes;
        
        /** Node array of size 'length'. */
        private boolean[] left;
        
        public Builder(@CheckForNull N start, int estimatedLength) {
            this.nodes = ArrayUtil.unsafeCastedNewArray(estimatedLength + 1);
            this.left = new boolean[estimatedLength];
            this.length = 0;
            nodes[0] = start;
        }

        public int length() {
            return length;
        }
        
        public void append(boolean left, @CheckForNull N node) {
            if (length == this.left.length) {
                // Resize array
                int newSize = length * 2;
                this.left = Arrays.copyOf(this.left, newSize);
                this.nodes = Arrays.copyOf(this.nodes, newSize + 1);
            }
            this.left[length] = left;
            this.nodes[length + 1] = node;
            this.length++;
        }
        
        public Path<N> build() {
            return new Path<>(length, nodes, left);
        }

        public void limitLength(int length) {
            while(this.length > length) {
                this.nodes[this.length] = null;
                this.length--;
            }
        }
        
    }
}
