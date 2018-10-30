package test.org.povworld.collection.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Iterator;

import javax.annotation.CheckForNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.povworld.collection.CollectionUtil;
import org.povworld.collection.immutable.ImmutableCollections;
import org.povworld.collection.mutable.ArrayList;
import org.povworld.collection.tree.TreeNode;
import org.povworld.collection.tree.TreeUtil;

import test.org.povworld.collection.TestUtil;

/**
 * Unit tests for {@link TreeUtil}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TreeUtilTest {
    
    @CheckForNull
    private final TestNode leaf = null;
    
    private final TestNode single;
    
    private final TestNode tree;
    
    private interface TestNode extends TreeNode<TestNode> {
    }
    
    private static class TestNodeImpl implements TestNode { // TODO create base class for TreeNode
        
        private final TestNode left;
        
        private final TestNode right;
        
        public TestNodeImpl(TestNode left, TestNode right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public TestNode getLeft() {
            return left;
        }
        
        @Override
        public TestNode getRight() {
            return right;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            if (left == null) {
                sb.append("*");
            } else {
                sb.append(left.toString());
            }
            if (right == null) {
                sb.append("*");
            } else {
                sb.append(right.toString());
            }
            sb.append(")");
            return sb.toString();
        }
        
    }
    
    public TreeUtilTest() {
        CollectionUtil.getDefaultComparator(Integer.class);
        single = node(leaf, leaf);
        tree = node(node(leaf, leaf), node(node(leaf, leaf), leaf));
    }
    
    private static TestNode node(TestNode left, TestNode right) {
        return new TestNodeImpl(left, right);
    }
    
    @Test
    public void getMaxNodeOfLeaf() {
        assertNull(TreeUtil.getMaxNode(leaf));
    }
    
    @Test
    public void getMaxNode() {
        assertEquals(single, TreeUtil.getMaxNode(single));
        assertEquals(tree.getRight(), TreeUtil.getMaxNode(tree));
    }
    
    @Test
    public void getMinNodeOfLeaf() {
        assertNull(TreeUtil.getMinNode(leaf));
    }
    
    @Test
    public void getMinNode() {
        assertEquals(single, TreeUtil.getMinNode(single));
        assertEquals(tree.getLeft(), TreeUtil.getMinNode(tree));
    }
    
    @Test
    public void iterateNodes() {
        ArrayList<TestNode> actual = TestUtil.verifyIterable(new Iterable<TestNode>() {
            @Override
            public Iterator<TestNode> iterator() {
                return TreeUtil.iterateNodes(tree);
            }
        }, ArrayList.<TestNode>newBuilder());
        assertEquals(ImmutableCollections.listOf(tree.getLeft(), tree, tree.getRight().getLeft(), tree.getRight()), actual);
    }
    
    @Test
    public void iterateNodesPostOrder() {
        ArrayList<TestNode> actual = TestUtil.verifyIterable(new Iterable<TestNode>() {
            @Override
            public Iterator<TestNode> iterator() {
                return TreeUtil.iterateNodesPostOrder(tree);
            }
        }, ArrayList.<TestNode>newBuilder());
        assertEquals(ImmutableCollections.listOf(tree.getLeft(), tree.getRight().getLeft(), tree.getRight(), tree), actual);
    }
    
}
