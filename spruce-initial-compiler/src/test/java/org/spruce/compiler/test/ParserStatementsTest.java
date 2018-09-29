package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.Collections;
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
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement()
    {
        Parser parser = new Parser(new Scanner("Integer[] values := {1, 2, 3};"));
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
    }
    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration()
    {
        Parser parser = new Parser(new Scanner("Boolean result := true, done := false"));
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkBinary(node, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers()
    {
        Parser parser = new Parser(new Scanner("final const Boolean result := true, done := false"));
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTVariableModifierList.class, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier()
    {
        Parser parser = new Parser(new Scanner("const"));
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkSimple(node, ASTVariableModifier.class);
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers()
    {
        Parser parser = new Parser(new Scanner("final const"));
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkBinaryLeftAssociative(node, Collections.singletonList(null), ASTVariableModifierList.class, ASTVariableModifier.class);
    }

    /**
     * Tests variable modifier of "const".
     */
    @Test
    public void testVariableModifierOfConst()
    {
        Parser parser = new Parser(new Scanner("const"));
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, CONST);
    }

    /**
     * Tests variable modifier of "final".
     */
    @Test
    public void testVariableModifierOfFinal()
    {
        Parser parser = new Parser(new Scanner("final"));
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, FINAL);
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator()
    {
        Parser parser = new Parser(new Scanner("a := b"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkSimple(node, ASTVariableDeclarator.class);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList()
    {
        Parser parser = new Parser(new Scanner("x := 1, y := x"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA), ASTVariableDeclaratorList.class, ASTVariableDeclarator.class);
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested()
    {
        Parser parser = new Parser(new Scanner("a := 1, b := a + 1, c := 2 * b"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkBinaryLeftAssociative(node, Arrays.asList(COMMA, COMMA), ASTVariableDeclaratorList.class, ASTVariableDeclarator.class);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier()
    {
        Parser parser = new Parser(new Scanner("varName"));
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer()
    {
        Parser parser = new Parser(new Scanner("count := 2"));
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkBinary(node, ASSIGNMENT, ASTIdentifier.class, ASTVariableInitializer.class);
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType()
    {
        Parser parser = new Parser(new Scanner("spruce.lang.String[][])"));
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto()
    {
        Parser parser = new Parser(new Scanner("auto"));
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkEmpty(node, AUTO);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement()
    {
        Parser parser = new Parser(new Scanner("x := x + 1;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement()
    {
        Parser parser = new Parser(new Scanner("return true;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement()
    {
        Parser parser = new Parser(new Scanner("throw new CompileException(\"Error message\");"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement()
    {
        Parser parser = new Parser(new Scanner("break;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement()
    {
        Parser parser = new Parser(new Scanner("continue;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement()
    {
        Parser parser = new Parser(new Scanner("fallthrough;"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement()
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
    public void testReturnStatementOfExpression()
    {
        Parser parser = new Parser(new Scanner("return x.y + 2;"));
        ASTReturnStatement node = parser.parseReturnStatement();
        checkSimple(node, ASTExpression.class, RETURN);
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression()
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
    public void testAssertStatementOfExpression()
    {
        Parser parser = new Parser(new Scanner("assert result = true;"));
        ASTAssertStatement node = parser.parseAssertStatement();
        checkSimple(node, ASTExpression.class, ASSERT);
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions()
    {
        Parser parser = new Parser(new Scanner("assert result = true : \"Assertion failed!\";"));
        ASTAssertStatement node = parser.parseAssertStatement();
        checkBinary(node, ASSERT, ASTExpression.class, ASTExpression.class);
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression()
    {
        Parser parser = new Parser(new Scanner("x++;"));
        ASTExpressionStatement node = parser.parseExpressionStatement();

        assertEquals(SEMICOLON, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStatementExpression);

        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment()
    {
        Parser parser = new Parser(new Scanner("x := 0"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTAssignment);

        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression()
    {
        Parser parser = new Parser(new Scanner("x.y++"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPostfixExpression);

        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of prefix expression.
     */
    @Test
    public void testStatementExpressionOfPrefixExpression()
    {
        Parser parser = new Parser(new Scanner("--x.y"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTPrefixExpression);

        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation()
    {
        Parser parser = new Parser(new Scanner("x.y(2)"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTMethodInvocation);

        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressioOfnClassInstanceCreationExpression()
    {
        Parser parser = new Parser(new Scanner("new SideEffect()"));
        ASTStatementExpression node = parser.parseStatementExpression();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());

        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTClassInstanceCreationExpression);

        node.collapseThenPrint();
    }
}
