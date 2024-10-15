package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.scanner.TokenType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility methods for other parser tests.  No test entry points.
 */
public class ParserTestUtility {
    /**
     * Helper method to test a list of child nodes to see if they are of the
     * expected types.
     * @param expectedClasses A <code>List</code> of expected <code>Class</code>es.
     * @param children A <code>List</code> of child <code>ASTNode</code>s.
     */
    static void compareClasses(List<Class<?>> expectedClasses, List<? extends ASTNode> children) {
        assertEquals(expectedClasses.size(), children.size());
        for (int i = 0; i < children.size(); i++) {
            ASTNode child = children.get(i);
            assertEquals(expectedClasses.get(i), child.getClass(), "Mismatch on child " + i);
        }
    }

    /**
     * Helper method to test that a parent node has no children but has the
     * expected operation.
     * @param node The parent node to check.
     * @param operation The expected operation for the parent.
     */
    static void checkEmpty(ASTParentNode node, TokenType operation) {
        assertEquals(operation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(0, children.size());
    }

    /**
     * Helper method to test the node itself.
     *                  child
     * @param node The node to check.
     * @param childClass The class that the node should be.
     */
    static void checkIs(ASTNode node, Class<? extends ASTNode> childClass) {
        assertTrue(childClass.isInstance(node));
    }

    /**
     * Fails the test if the given node is <em>not</em> an instance of the
     * given class.  If it is, this method casts the given node to the given
     * class.
     * @param node An <code>ASTNode</code>.
     * @param nodeClass The node should be an instance of this <code>Class</code>.
     * @return The node, down-casted to an instance of the given class.
     * @param <T> The type to cast to.
     */
    static <T extends ASTNode> T ensureIsa(ASTNode node, Class<T> nodeClass) {
        assertInstanceOf(nodeClass, node);
        return nodeClass.cast(node);
    }

    /**
     * Helper method to test the simple node relationship:
     *                  parent
     *                    |
     *                  child
     * The parent node is expected to have no operation and exactly one child.
     * @param node The parent node to check.
     * @param childClass The class of the child node.
     */
    static void checkSimple(ASTParentNode node, Class<? extends ASTNode> childClass) {
        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(childClass.isInstance(child));
    }

    /**
     * Helper method to test the simple node relationship:
     *              parent(operation)
     *                    |
     *                  child
     * The parent node is expected to have the given operation and exactly one child.
     * @param node The parent node to check.
     * @param childClass The class of the child node.
     * @param operation The expected operation for the parent.
     */
    static void checkSimple(ASTParentNode node, Class<? extends ASTNode> childClass, TokenType operation) {
        assertEquals(operation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(childClass.isInstance(child));
    }

    /**
     * Helper method to test the simple node relationship:
     *                     unary
     *                       |
     *                     child
     * @param node The <code>UnaryNode</code> to check.
     * @param childClass The expected class of the child node.
     */
    static void checkUnary(ASTUnaryNode node, Class<? extends ASTNode> childClass) {
        assertNull(node.getOperation());
        ASTNode child = node.getFirst();
        assertTrue(childClass.isInstance(child));
    }

    /**
     * Helper method to test the simple node relationship:
     *                  unary(operation)
     *                       |
     *                     child
     * @param node The <code>UnaryNode</code> to check.
     * @param operation The expected operation for the unary node.
     * @param childClass The expected class of the child node.
     */
    static void checkUnary(ASTUnaryNode node, TokenType operation, Class<? extends ASTNode> childClass) {
        assertEquals(operation, node.getOperation());
        ASTNode child = node.getFirst();
        assertTrue(childClass.isInstance(child));
    }

    /**
     * Helper method to test the unary node relationship:
     *                  parent(operation)
     *                    |
     *                  parent
     *                    |
     *                  child1
     * The parent node is expected to have the given operation and exactly one child.
     * That child is of the expected parent node type with exactly one child, the
     * child type.
     * @param node The parent node to check.
     * @param childClass The class of the child node.
     */
    static <U extends ASTUnaryNode, C extends ASTNode> void checkUnary(ASTUnaryNode node, TokenType expectedOperation,
                           Class<U> nodeClass, Class<C> childClass) {
        assertEquals(expectedOperation, node.getOperation());
        U child = ensureIsa(node.getFirst(), nodeClass);

        assertNull(child.getOperation());
        assertInstanceOf(childClass, child.getFirst());
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent(operation)
     *                   /            \
     *            parent(operation)  child
     *                 /     \
     *               ...   child
     *               /
     *            child
     * The parent nodes are expected to have the given operations, and every child
     * is expected to have no operation.
     * @param node The parent node to check.
     * @param expectedOperations The operations that the parent nodes are
     *      expected to have.  The size of this list also defines how tall the
     *      tree is expected to be.
     * @param nodeClass The class of the parent node.
     * @param childClass The class of the child node.
     */
    static void checkBinaryLeftAssociative(ASTParentNode node, List<TokenType> expectedOperations,
                                           Class<? extends ASTParentNode> nodeClass, Class<? extends ASTNode> childClass) {
        ASTParentNode child = node;
        //List<ASTNode> children = Arrays.asList(node.getFirst(), node.getSecond());

        for (TokenType operation : expectedOperations) {
            assertEquals(operation, child.getOperation());
            ASTBinaryNode binary = ensureIsa(child, ASTBinaryNode.class);
            List<ASTNode> children = Arrays.asList(binary.getFirst(), binary.getSecond());
            List<Class<?>> expectedClasses = Arrays.asList(nodeClass, childClass);
            compareClasses(expectedClasses, children);
            child = (ASTParentNode) binary.getFirst();
        }

        assertNull(child.getOperation());
        assertInstanceOf(childClass, child);
    }

    /**
     * Helper method to test the multi-level binary node by implementing a
     * postorder traversal to check classes of leaf nodes and operations of
     * binary (non-leaf) nodes.
     *                          binary(op3)
     *                           /    \
     *                  binary(op2)   leaf(value4)
     *                  /        \
     *        leaf(value1)      binary(op1)
     *                          /       \
     *              leaf(value2)        leaf(value3)
     * <em>value1 value2 value3 op1 op2 value4 op3</em>
     * @param node An <code>ASTBinaryNode</code>.
     * @param expected The postorder nodes that are expected, consisting of either
     *                 <code>Class&lt;?&gt;</code> objects of leaf nodes or
     *                 <code>TokenType</code>s representing binary node operations.
     */
    static void checkBinaryPostorder(ASTBinaryNode node, Object... expected) {
        checkBinaryPostorder(node, 0, expected);
    }

    /**
     * Helper method to implement binary node postorder traversal check.
     * @param node An <code>ASTBinaryNode</code>.
     * @param index The current index of postorder nodes to check.
     * @param expected The postorder nodes that are expected, consisting of either
     *                 <code>Class&lt;?&gt;</code> objects of leaf nodes or
     *                 <code>TokenType</code>s representing binary node operations.
     * @return The current index after checking <code>node</code>.
     */
    private static int checkBinaryPostorder(ASTBinaryNode node, int index, Object... expected) {
        ASTNode left = node.getFirst();
        ASTNode right = node.getSecond();

        if (left instanceof ASTBinaryNode parent) {
            index = checkBinaryPostorder(parent, index, expected);
        }
        else if (expected[index] instanceof Class<?> leaf) {
            assertEquals(leaf, left.getClass());
            index++;
        }
        else {
            fail("Expected a " + expected[index] + ", got a " + left.getClass() + "!");
        }

        if (right instanceof ASTBinaryNode parent) {
            index = checkBinaryPostorder(parent, index, expected);
        }
        else if (expected[index] instanceof Class<?> leaf) {
            assertEquals(leaf, right.getClass());
            index++;
        }
        else {
            fail("Expected a " + expected[index] + ", got a " + right.getClass() + "!");
        }

        if (expected[index] instanceof TokenType operation) {
            assertEquals(operation, node.getOperation());
        } else {
            fail("Expected " + expected[index] + ", got a " + node.getOperation());
        }
        return index + 1;
    }

    /**
     * Helper method to test the binary node relationship:<code>
     *                 binary(operation)
     *                   /        \
     *               child1     child2
     * </code>
     * The binary node is expected to have the given operation, and the
     * children are expected to be of the given classes.
     * @param node The <code>BinaryNode</code> to check.
     * @param operation The expected operation for the unary node.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTBinaryNode node, TokenType operation,
                            Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass) {
        assertEquals(operation, node.getOperation());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, Arrays.asList(node.getFirst(), node.getSecond()));
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent
     *                 /    \
     *             child1  child2
     * The binary node is expected to have no operation, and the
     * children are expected to be of the given classes.
     * @param node The <code>ASTBinaryNode</code> to check.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTBinaryNode node, Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass) {
        assertNull(node.getOperation());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, Arrays.asList(node.getFirst(), node.getSecond()));
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent(operation)
     *                   /        \
     *               child1     child2
     * The parent node is expected to have the given operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param expectedOperation The operation that the parent node is expected
     *      to have.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTParentNode node, TokenType expectedOperation,
                            Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass) {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, children);
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent
     *                 /    \
     *             child1  child2
     * The parent node is expected to have no operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTParentNode node, Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass) {
        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, children);
    }

    /**
     * Helper method to test the list node relationship:
     *                 parent(operation)
     *                  /    |    \    \
     *              child  child  ...  child
     * The parent node is expected to have the given operation, and all
     * children are expected to be of the given child class.
     * @param node The parent node to check.
     * @param expectedOperation The operation that the parent node is expected
     *      to have.
     * @param expectedSize The expected size of the list of child nodes.
     * @param childClass The class of the all children.
     */
    static void checkList(ASTParentNode node, TokenType expectedOperation, Class<? extends ASTNode> childClass, int expectedSize) {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(expectedSize, children.size());
        for (ASTNode child : children) {
            assertTrue(childClass.isInstance(child));
        }
    }

    /**
     * Helper method to test the list node relationship:
     *                   listNode(type)
     *                  /    |    \    \
     *              child  child  ...  child
     * The parent node is expected to have the given operation, and all
     * children are expected to be of the given child class.
     * @param node The parent node to check.
     * @param type The list type that the list node is expected
     *      to have.
     * @param expectedSize The expected size of the list of child nodes.
     * @param childClass The class of the all children.
     */
    static void checkList(ASTListNode node, ASTListNode.Type type, Class<? extends ASTNode> childClass, int expectedSize) {
        assertEquals(type, node.getType());
        List<ASTNode> children = node.getChildren();
        assertEquals(expectedSize, children.size());
        for (ASTNode child : children) {
            assertInstanceOf(childClass, child);
        }
    }

    /**
     * Helper method to test the trinary node relationship:
     *               parent(operation)
     *                /    |     \
     *           child1  child2  child3
     * The parent node is expected to have the given operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param expectedOperation The operation that the parent node is expected
     *      to have.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     * @param thirdClass The class of the third child node.
     */
    static void checkTrinary(ASTParentNode node, TokenType expectedOperation,
        Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass, Class<? extends ASTNode> thirdClass) {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass, thirdClass);
        compareClasses(expectedClasses, children);
    }

    /**
     * Helper method to test the n-ary node relationship:
     *                 parent(operation)
     *                /    |     |     \
     *           child1 child2 child3  childN
     * The parent node is expected to have the given operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param expectedOperation The operation that the parent node is expected
     *      to have.
     * @param classes The classes of each child.
     */
    static void checkNary(ASTParentNode node, TokenType expectedOperation, Class<?>... classes) {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(classes.length, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(classes);
        compareClasses(expectedClasses, children);
    }

}
