package org.spruce.compiler.test;

import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.ast.expressions.ASTBinaryExpression;
import org.spruce.compiler.ast.expressions.ASTUnaryExpression;
import org.spruce.compiler.ast.expressions.ASTValueExpression;
import org.spruce.compiler.message.CompilerMessage;
import org.spruce.compiler.parser.BasicParser;
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
    static void compareClasses(List<Class<?>> expectedClasses, List<? extends Node> children) {
        assertEquals(expectedClasses.size(), children.size());
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            assertEquals(expectedClasses.get(i), child.getClass(), "Mismatch on child " + i);
        }
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
    static <T extends Node> T ensureIsa(Node node, Class<T> nodeClass) {
        assertInstanceOf(nodeClass, node);
        return nodeClass.cast(node);
    }

    /**
     * Prints the node.  Prints any compiler messages.  Ensures that there are
     * no compiler messages representing an error.
     * @param node A <code>Node</code>.
     * @param parser A <code>BasicParser</code>.
     */
    static void ensureNoErrors(Node node, BasicParser parser) {
        System.out.println(node);
        long errorCount = generalCheckForError(parser);
        if (errorCount != 0) {
            fail("Error message(s) found!");
        }
    }

    /**
     * Prints the node.  Prints any compiler messages.  Ensures that there is
     * exactly one compiler message representing an error.
     * @param node A <code>Node</code>.
     * @param parser A <code>BasicParser</code>.
     */
    static void expectError(Node node, BasicParser parser) {
        expectError(node, parser, 1);
    }

    /**
     * Prints the node.  Prints any compiler messages.  Ensures that there is
     * exactly the specified number of compiler messages representing an error.
     * @param node A <code>Node</code>.
     * @param parser A <code>BasicParser</code>.
     */
    static void expectError(Node node, BasicParser parser, int count) {
        System.out.println(node);
        long errorCount = generalCheckForError(parser);
        if (errorCount != count) {
            fail("Expected " + count + " message(s), got " + errorCount + "!");
        }
    }

    private static long generalCheckForError(BasicParser parser) {
        List<CompilerMessage> msgs = parser.getCompilerMessages();
        for (CompilerMessage msg : msgs) {
            System.out.println(msg);
        }
        return msgs.stream()
                .filter(cm -> cm.getLevel() == CompilerMessage.Level.ERROR)
                .count();
    }

    /**
     * Helper method to test the unary expression relationship:
     *               unaryExpr(operation)
     *                    |
     *                  child1
     * The unary expression is expected to have the given operation.
     * That child is of the expected child class node type .
     * @param node The unary expression node to check.
     * @param childClass The class of the child node.
     */
    static void checkUnaryExpr(ASTUnaryExpression node, TokenType expectedOperation, Class<? extends ASTNode> childClass) {
        assertEquals(expectedOperation, node.getOperation());
        assertInstanceOf(childClass, node.getFirst());
    }

    /**
     * Helper method to test the unary node relationship:
     *               unaryExpr(operation)
     *                    |
     *                 unaryExpr
     *                    |
     *                  child1
     * The unary expression node is expected to have the given operation and exactly one child.
     * That child is of the expected parent node type with exactly one child, the
     * child type.
     * @param node The unary expression node to check.
     * @param childClass The class of the child node.
     */
    static <U extends ASTUnaryExpression> void checkUnaryExpr(ASTUnaryExpression node, TokenType expectedOperation,
                                                        Class<U> parentNodeClass, Class<? extends ASTNode> childClass) {
        assertEquals(expectedOperation, node.getOperation());
        U unaryNode = ensureIsa(node.getFirst(), parentNodeClass);
        assertInstanceOf(childClass, unaryNode.getFirst());
    }

    /**
     * Helper method to test the multi-level binary expression by implementing a
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
     * @param node An <code>ASTBinaryExpression</code>.
     * @param expected The postorder nodes that are expected, consisting of either
     *                 <code>Class&lt;?&gt;</code> objects of leaf nodes or
     *                 <code>TokenType</code>s representing binary expression operations.
     */
    static void checkBinaryPostorder(ASTBinaryExpression node, Object... expected) {
        checkBinaryPostorder(node, 0, expected);
    }

    /**
     * Helper method to implement binary expression postorder traversal check.
     * @param node An <code>ASTBinaryExpression</code>.
     * @param index The current index of postorder nodes to check.
     * @param expected The postorder nodes that are expected, consisting of either
     *                 <code>Class&lt;?&gt;</code> objects of leaf nodes or
     *                 <code>TokenType</code>s representing binary node operations.
     * @return The current index after checking <code>node</code>.
     */
    private static int checkBinaryPostorder(ASTBinaryExpression node, int index, Object... expected) {
        ASTValueExpression left = node.getFirst();
        ASTValueExpression right = node.getSecond();

        if (left instanceof ASTBinaryExpression parent) {
            index = checkBinaryPostorder(parent, index, expected);
        }
        else if (expected[index] instanceof Class<?> leaf) {
            assertEquals(leaf, left.getClass());
            index++;
        }
        else {
            fail("Expected a " + expected[index] + ", got a " + left.getClass() + "!");
        }

        if (right instanceof ASTBinaryExpression parent) {
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
     * Helper method to test the list node relationship:
     *                   listNode(type)
     *                  /    |    \    \
     *             child1  child2  ...  child
     * The parent node is expected to have the given operation, and all
     * children are expected to be of the given child class.
     * @param node The parent node to check.
     * @param type The list type that the list node is expected
     *      to have.
     * @param expectedSize The expected size of the list of child nodes.
     * @param childClass The class of the all children.
     */
    static <T extends Node> void checkList(ASTListNode<T> node, ASTListNode.Type type, Class<T> childClass, int expectedSize) {
        assertEquals(type, node.getType());
        List<T> children = node.getTypedChildren();
        assertEquals(expectedSize, children.size());
        for (T child : children) {
            assertInstanceOf(childClass, child);
        }
    }
}
