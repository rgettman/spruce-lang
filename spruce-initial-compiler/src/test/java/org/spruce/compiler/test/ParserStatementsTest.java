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
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing()
    {
        Parser parser = new Parser(new Scanner("{}"));
        ASTBlock node = parser.parseBlock();
        checkEmpty(node, OPEN_BRACE);
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements()
    {
        Parser parser = new Parser(new Scanner("{Integer a := 1;\nInteger b := 2;\nreturn a + b;}"));
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements()
    {
        Parser parser = new Parser(new Scanner("final String stmt := \"Statement one!\";\nconst Integer stmt2Nbr := 2;\ni++;}"));
        ASTBlockStatements node = parser.parseBlockStatements();
        checkList(node, null, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration()
    {
        Parser parser = new Parser(new Scanner("final Integer i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration()
    {
        Parser parser = new Parser(new Scanner("Integer i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment()
    {
        Parser parser = new Parser(new Scanner("i := 1;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation()
    {
        Parser parser = new Parser(new Scanner("i(j);"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE()
    {
        Parser parser = new Parser(new Scanner("i.new J();"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn()
    {
        Parser parser = new Parser(new Scanner("return true;"));
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
    }

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
        checkList(node, null, ASTVariableModifier.class, 2);
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
        checkSimple(node, ASTVariableDeclarator.class, COMMA);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList()
    {
        Parser parser = new Parser(new Scanner("x := 1, y := x"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested()
    {
        Parser parser = new Parser(new Scanner("a := 1, b := a + 1, c := 2 * b"));
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 3);
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
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock()
    {
        Parser parser = new Parser(new Scanner("{x := x + 1;}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBlock.class);
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
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement()
    {
        Parser parser = new Parser(new Scanner("if (success) { return true; }"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTIfStatement.class);
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement()
    {
        Parser parser = new Parser(new Scanner("while (shouldContinue) doWork();"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTWhileStatement.class);
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement()
    {
        Parser parser = new Parser(new Scanner("do { work(); } while (shouldContinue);"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTDoStatement.class);
    }

    /**
     * Tests statement of synchronized statement.
     */
    @Test
    public void testStatementOfSynchronizedStatement()
    {
        Parser parser = new Parser(new Scanner("synchronized (myLock) {\n    myLock.wait();\n}"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSynchronizedStatement.class);
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement()
    {
        Parser parser = new Parser(new Scanner("for (;;) doWork();"));
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTForStatement.class);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple()
    {
        Parser parser = new Parser(new Scanner("if (success) { return true; }"));
        ASTIfStatement node = parser.parseIfStatement();

        assertEquals(IF, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpressionNoIncrDecr.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit()
    {
        Parser parser = new Parser(new Scanner("if {String line := br.readLine()} (line != null) out.println(line);"));
        ASTIfStatement node = parser.parseIfStatement();

        assertEquals(IF, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse()
    {
        Parser parser = new Parser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else {\n    out.println(\"Test FAILED!\");\n}"));
        ASTIfStatement node = parser.parseIfStatement();

        assertEquals(IF, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested()
    {
        Parser parser = new Parser(new Scanner("if (result) {\n    out.println(\"Test passed.\");\n} else if (DEBUG) {\n    out.println(\"Test failed in debug mode!\");\n} else {\n    out.println(\"Test FAILED!\");\n}"));
        ASTIfStatement node = parser.parseIfStatement();

        assertEquals(IF, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpressionNoIncrDecr.class, ASTStatement.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        ASTStatement nested = (ASTStatement) children.get(2);
        assertNull(nested.getOperation());
        children = nested.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTIfStatement);

        ASTIfStatement nestedIf = (ASTIfStatement) child;
        assertEquals(IF, nestedIf.getOperation());
        children = nestedIf.getChildren();
        assertEquals(3, children.size());
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple()
    {
        Parser parser = new Parser(new Scanner("while (shouldContinue) { doWork(); }"));
        ASTWhileStatement node = parser.parseWhileStatement();

        assertEquals(WHILE, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpressionNoIncrDecr.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit()
    {
        Parser parser = new Parser(new Scanner("while {String line := br.readLine()} (line != null) out.println(line);"));
        ASTWhileStatement node = parser.parseWhileStatement();

        assertEquals(WHILE, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement()
    {
        Parser parser = new Parser(new Scanner("do { work(); } while (shouldContinue);"));
        ASTDoStatement node = parser.parseDoStatement();

        assertEquals(DO, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTStatement.class, ASTExpressionNoIncrDecr.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests synchronized statement.
     */
    @Test
    public void tesSynchronizedStatement()
    {
        Parser parser = new Parser(new Scanner("synchronized (lock) { lock.notifyAll(); }"));
        ASTSynchronizedStatement node = parser.parseSynchronizedStatement();

        assertEquals(SYNCHRONIZED, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(2, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTExpressionNoIncrDecr.class, ASTBlock.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3()
    {
        Parser parser = new Parser(new Scanner("for (Int i := 0; i < 10; i++) {\n    out.println(i);\n}"));
        ASTForStatement node = parser.parseForStatement();

        assertEquals(FOR, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTBasicForStatement);

        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) child;
        assertEquals(SEMICOLON, basicForStmt.getOperation());
        children = basicForStmt.getChildren();
        assertEquals(4, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTInit.class, ASTExpressionNoIncrDecr.class, ASTStatementExpressionList.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of infinite loop.
     */
    @Test
    public void testForStatementOfBasicForStatementInfiniteLoop()
    {
        Parser parser = new Parser(new Scanner("for (;;) {\n    out.println(\"Hello world!\");\n}"));
        ASTForStatement node = parser.parseForStatement();

        assertEquals(FOR, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTBasicForStatement);

        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) child;
        assertEquals(SEMICOLON, basicForStmt.getOperation());
        children = basicForStmt.getChildren();
        assertEquals(1, children.size());
        child = children.get(0);
        assertTrue(child instanceof ASTStatement);

        node.collapseThenPrint();
    }



    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement()
    {
        Parser parser = new Parser(new Scanner("for (Int i : array) {\n    sum += i;\n}"));
        ASTForStatement node = parser.parseForStatement();

        assertEquals(FOR, node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTEnhancedForStatement);

        ASTEnhancedForStatement enhForStmt = (ASTEnhancedForStatement) child;
        assertEquals(COLON, enhForStmt.getOperation());
        children = enhForStmt.getChildren();
        assertEquals(3, children.size());
        List<Class<?>> expectedClasses = Arrays.asList(ASTLocalVariableDeclaration.class, ASTExpressionNoIncrDecr.class, ASTStatement.class);
        compareClasses(expectedClasses, children);

        node.collapseThenPrint();
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
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration()
    {
        Parser parser = new Parser(new Scanner("Int i := 0, j := 0"));
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTLocalVariableDeclaration.class);
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression()
    {
        Parser parser = new Parser(new Scanner("i := 0"));
        ASTInit node = parser.parseInit();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStatementExpressionList);

        ASTStatementExpressionList list = (ASTStatementExpressionList) child;
        checkSimple(list, ASTStatementExpression.class, COMMA);
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList()
    {
        Parser parser = new Parser(new Scanner("i := 0, j := 0, k := 1"));
        ASTInit node = parser.parseInit();

        assertNull(node.getOperation());
        List<ASTNode> children = node.getChildren();
        assertEquals(1, children.size());
        ASTNode child = children.get(0);
        assertTrue(child instanceof ASTStatementExpressionList);

        ASTStatementExpressionList list = (ASTStatementExpressionList) child;
        checkList(list, COMMA, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression()
    {
        Parser parser = new Parser(new Scanner("i := 0"));
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkSimple(node, ASTStatementExpression.class, COMMA);
    }

    /**
     * Tests statement expression lists of nested statement expression lists
     * (here, just multiple statement expressions).
     */
    @Test
    public void testStatementExpressionListNested()
    {
        Parser parser = new Parser(new Scanner("i := 0, j := 0, k := 1"));
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkList(node, COMMA, ASTStatementExpression.class, 3);
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
