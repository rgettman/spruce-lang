package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.ast.*;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest
{
    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementExpressionStatement()
    {
        Parser parser = new Parser(new Scanner("x := x + 1;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementReturnStatement()
    {
        Parser parser = new Parser(new Scanner("return true;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementThrowStatement()
    {
        Parser parser = new Parser(new Scanner("throw new CompileException(\"Error message\");"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementBreakStatement()
    {
        Parser parser = new Parser(new Scanner("break;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementContinueStatement()
    {
        Parser parser = new Parser(new Scanner("continue;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementFallthroughStatement()
    {
        Parser parser = new Parser(new Scanner("fallthrough;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementAssertStatement()
    {
        Parser parser = new Parser(new Scanner("assert status = true;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTAssertStatement.class);
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement()
    {
        Parser parser = new Parser(new Scanner("return;"));
        ASTReturnStatement node = parser.parseReturnStatement();
        checkEmpty(node, RETURN);
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementExpression()
    {
        Parser parser = new Parser(new Scanner("return x.y + 2;"));
        ASTReturnStatement node = parser.parseReturnStatement();
        checkSimple(node, ASTExpression.class, RETURN);
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementExpression()
    {
        Parser parser = new Parser(new Scanner("throw new Exception();"));
        ASTThrowStatement node = parser.parseThrowStatement();
        checkSimple(node, ASTExpression.class, THROW);
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement()
    {
        Parser parser = new Parser(new Scanner("break;"));
        ASTBreakStatement node = parser.parseBreakStatement();
        checkEmpty(node, BREAK);
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement()
    {
        Parser parser = new Parser(new Scanner("continue;"));
        ASTContinueStatement node = parser.parseContinueStatement();
        checkEmpty(node, CONTINUE);
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement()
    {
        Parser parser = new Parser(new Scanner("fallthrough;"));
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        checkEmpty(node, FALLTHROUGH);
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementExpression()
    {
        Parser parser = new Parser(new Scanner("assert result = true;"));
        ASTAssertStatement node = parser.parseAssertStatement();
        checkSimple(node, ASTExpression.class, ASSERT);
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementTwoExpression()
    {
        Parser parser = new Parser(new Scanner("assert result = true : \"Assertion failed!\";"));
        ASTAssertStatement node = parser.parseAssertStatement();

        assertEquals(ASSERT, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpression.class, ASTExpression.class);
        compareClasses(expectedClasses, children);

        node.collapse();
        node.print();
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementStatementExpression()
    {
        Parser parser = new Parser(new Scanner("x++;"));
        ASTExpressionStatement node = parser.parseExpressionStatement();

        assertEquals(SEMICOLON, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStatementExpression);

        node.collapse();
        node.print();
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionAssignment()
    {
        Parser parser = new Parser(new Scanner("x := 0"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTAssignment);

        node.collapse();
        node.print();
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionPostfixExpression()
    {
        Parser parser = new Parser(new Scanner("x.y++"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPostfixExpression);

        node.collapse();
        node.print();
    }

    /**
     * Tests statement expression of prefix expression.
     */
    @Test
    public void testStatementExpressionPrefixExpression()
    {
        Parser parser = new Parser(new Scanner("--x.y"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPrefixExpression);

        node.collapse();
        node.print();
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionMethodInvocation()
    {
        Parser parser = new Parser(new Scanner("x.y(2)"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTMethodInvocation);

        node.collapse();
        node.print();
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionClassInstanceCreationExpression()
    {
        Parser parser = new Parser(new Scanner("new SideEffect()"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassInstanceCreationExpression);

        node.collapse();
        node.print();
    }
}
