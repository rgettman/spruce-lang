package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.scanner.TokenType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility methods for other parser tests.  No test entry points.
 */
public class ParserTestUtility
{
    /**
     * Helper method to test a list of child nodes to see if they are of the
     * expected types.
     * @param expectedClasses A <code>List</code> of expected <code>Class</code>es.
     * @param children A <code>List</code> of child <code>ASTNode</code>s.
     */
    static void compareClasses(List<Class<?>> expectedClasses, List<ASTNode> children)
    {
        assertEquals(expectedClasses.size(), children.size());
        for (int i = 0; i < children.size(); i++)
        {
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
    static void checkEmpty(ASTParentNode node, TokenType operation)
    {
        assertEquals(operation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(0, children.size());

        node.collapseThenPrint();
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
    static void checkSimple(ASTParentNode node, Class<? extends ASTNode> childClass)
    {
        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(childClass.isInstance(child));

        node.collapseThenPrint();
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
    static void checkSimple(ASTParentNode node, Class<? extends ASTNode> childClass, TokenType operation)
    {
        assertEquals(operation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(childClass.isInstance(child));

        node.collapseThenPrint();
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
    static void checkUnary(ASTParentNode node, TokenType expectedOperation,
                           Class<? extends ASTParentNode> nodeClass, Class<? extends ASTNode> childClass)
    {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        assertTrue(nodeClass.isInstance(children.get(0)));

        ASTParentNode child = (ASTParentNode) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(1, children.size());
        assertTrue(childClass.isInstance(children.get(0)));

        node.collapseThenPrint();
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
     *
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
                                           Class<? extends ASTParentNode> nodeClass, Class<? extends ASTNode> childClass)
    {
        ASTParentNode child = node;
        List<ASTNode> children = node.getChildren();

        for (TokenType operation : expectedOperations)
        {
            assertEquals(operation, child.getOperation());
            children = child.getChildren();
            assertEquals(2, children.size());
            List<Class<?>> expectedClasses = Arrays.asList(nodeClass, childClass);
            compareClasses(expectedClasses, children);
            child = (ASTParentNode) children.get(0);
        }

        child = (ASTParentNode) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(1, children.size());
        assertTrue(childClass.isInstance(children.get(0)));

        node.collapseThenPrint();
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent(operation)
     *                   /            \
     *            parent(operation)  child(value)
     *                 /     \
     *               ...   child(value)
     *               /
     *            child(value)
     *
     * The parent nodes are expected to have the given operations, and every child
     * is expected to have no operation and a value.
     * @param node The parent node to check.
     * @param expectedOperations The operations that the parent nodes are
     *      expected to have.  The size of this list also defines how tall the
     *      tree is expected to be.
     * @param expectedValues The values that the child nodes are expected to
     *      have.  The size of this list must be exactly one longer than the
     *      size of <code>expectedOperations</code>.
     * @param nodeClass The class of the parent node.
     * @param childClass The class of the child node.
     */
    static void checkBinaryLeftAssociative(ASTParentNode node, List<TokenType> expectedOperations, List<?> expectedValues,
                                           Class<? extends ASTParentNode> nodeClass, Class<? extends ASTValueNode> childClass)
    {
        ASTParentNode child = node;
        List<ASTNode> children = node.getChildren();

        for (int i = 0; i < expectedOperations.size(); i++)
        {
            TokenType operation = expectedOperations.get(i);
            assertEquals(operation, child.getOperation());
            children = child.getChildren();
            assertEquals(2, children.size());
            List<Class<?>> expectedClasses = Arrays.asList(nodeClass, childClass);
            compareClasses(expectedClasses, children);
            ASTValueNode valueChild = (ASTValueNode) children.get(1);
            assertEquals(expectedValues.get(i), valueChild.getValue());
            child = (ASTParentNode) children.get(0);
        }

        child = (ASTParentNode) children.get(0);
        assertNull(child.getOperation());
        children = child.getChildren();
        assertEquals(1, children.size());
        assertTrue(childClass.isInstance(children.get(0)));
        ASTValueNode valueChild = (ASTValueNode) children.get(0);
        assertEquals(expectedValues.get(expectedOperations.size()), valueChild.getValue());

        node.collapseThenPrint();
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent(operation)
     *                   /        \
     *               child1     child2
     *
     * The parent node is expected to have the given operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param expectedOperation The operation that the parent node is expected
     *      to have.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTParentNode node, TokenType expectedOperation,
                            Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass)
    {
        assertEquals(expectedOperation, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Helper method to test the binary node relationship:
     *                 parent
     *                 /    \
     *             child1  child2
     *
     * The parent node is expected to have no operation, and the
     * children are expected to be of the given classes.
     * @param node The parent node to check.
     * @param firstClass The class of the first child node.
     * @param secondClass The class of the second child node.
     */
    static void checkBinary(ASTParentNode node, Class<? extends ASTNode> firstClass, Class<? extends ASTNode> secondClass)
    {
        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(firstClass, secondClass);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Helper method to test the binary node relationship:
     *                AssignmentExpression
     *                         |
     *                Assignment(operator)
     *                    /         \
     *             LeftHandSide  AssignmentExpression
     * @param node The <code>AssignmentExpression</code> to check.
     * @param expectedOperation The operation that the assignment expression
     *      node is expected to have.
     */
    static void checkAssignmentExpressionLeftAssociative(ASTAssignmentExpression node, TokenType expectedOperation)
    {
        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ASTAssignment);

        ASTAssignment assignment = (ASTAssignment) children.get(0);
        assertEquals(expectedOperation, assignment.getOperation());
        children = assignment.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTLeftHandSide.class, ASTAssignmentExpression.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }
}
