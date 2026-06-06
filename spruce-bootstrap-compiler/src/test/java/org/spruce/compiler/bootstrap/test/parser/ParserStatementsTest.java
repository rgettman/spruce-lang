package org.spruce.compiler.bootstrap.test.parser;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.*;
import org.spruce.compiler.bootstrap.ast.statements.*;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.BaseMessageProducer;
import org.spruce.compiler.bootstrap.parser.ExpressionsParser;
import org.spruce.compiler.bootstrap.parser.Parser;
import org.spruce.compiler.bootstrap.parser.StatementsParser;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.bootstrap.scanner.TokenType.*;
import static org.spruce.compiler.bootstrap.test.parser.ParserTestUtility.*;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.test.util.TestUtility;

import static org.spruce.compiler.bootstrap.ast.ASTListNode.Type.*;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest {

    /**
     * Test block of block.
     */
    @Test
    public void testBlocksNested() {
        StatementsParser parser = getStatementsParser("""
            {
                {
                    Int a = 1;
                }
            }
            """);
        ASTBlock node = parser.parseBlock();
        ensureNoErrors(node, parser);
        ASTBlockStatements blockStmts = node.getBlockStmts();
        checkList(blockStmts, BLOCK_STMTS, ASTBlockStatement.class, 1);

        ASTBlock innerBlock = TestUtility.ensureIsa(blockStmts.getChildren().get(0), ASTBlock.class);
        ASTBlockStatements innerBlockStmts = innerBlock.getBlockStmts();
        checkList(innerBlockStmts, BLOCK_STMTS, ASTBlockStatement.class, 1);
    }

    /**
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing() {
        StatementsParser parser = getStatementsParser("{}");
        ASTBlock node = parser.parseBlock();
        ensureNoErrors(node, parser);
        ASTBlockStatements blockStmts = node.getBlockStmts();
        checkList(blockStmts, BLOCK_STMTS, ASTBlockStatement.class, 0);
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements() {
        StatementsParser parser = getStatementsParser("""
            {
                Integer a = 1;
                Integer b = 2;
                return a + b;
            }
            """);
        ASTBlock node = parser.parseBlock();
        ensureNoErrors(node, parser);
        ASTBlockStatements blockStmts = TestUtility.ensureIsa(node.getBlockStmts(), ASTBlockStatements.class);
        checkList(blockStmts, BLOCK_STMTS, ASTBlockStatement.class, 3);
    }

    /**
     * Tests bad block of no open brace.
     */
    @Test
    public void testBlockBadNoOpenBrace() {
        StatementsParser parser = getStatementsParser("Int i = 0;");
        ASTBlock node = parser.parseBlock();
        expectError(node, parser, 2);
    }

    /**
     * Tests bad block of no close brace.
     */
    @Test
    public void testBlockBadNoCloseBrace() {
        StatementsParser parser = getStatementsParser("""
                {
                    Int i = 0;
                """);
        ASTBlock node = parser.parseBlock();
        expectError(node, parser);
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements() {
        StatementsParser parser = getStatementsParser("""
            String stmt = "Statement one!";
            Integer stmt2Nbr = 2;
            i = i + 1;}
            """);
        ASTBlockStatements node = parser.parseBlockStatements();
        ensureNoErrors(node, parser);
        checkList(node, BLOCK_STMTS, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTLocalVariableDeclarationStatement.class, node);
    }

    /**
     * Tests block statement of constructor invocation with self.
     */
    @Test
    public void testBlockStatementOfConstructorInvocationSelf() {
        StatementsParser parser = getStatementsParser("self(2);");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTConstructorInvocation constrInvocation = TestUtility.ensureIsa(node, ASTConstructorInvocation.class);
        assertEquals(SELF, constrInvocation.getConstructorKeyword().getKeyword());
    }

    /**
     * Tests block statement of constructor invocation with super.
     */
    @Test
    public void testBlockStatementOfConstructorInvocationSuper() {
        StatementsParser parser = getStatementsParser("super(3);");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTConstructorInvocation constrInvocation = TestUtility.ensureIsa(node, ASTConstructorInvocation.class);
        assertEquals(SUPER, constrInvocation.getConstructorKeyword().getKeyword());
    }

    /**
     * Tests bad block statement of constructor invocation, no open parenthesis.
     */
    @Test
    public void testBlockStatementOfBadConstructorInvocationNoOpenParen() {
        StatementsParser parser = getStatementsParser("self 2);");
        ASTBlockStatement node = parser.parseBlockStatement();
        expectError(node, parser, 4);
    }

    /**
     * Tests bad block statement of constructor invocation, no close parenthesis.
     */
    @Test
    public void testBlockStatementOfBadConstructorInvocationNoCloseParen() {
        StatementsParser parser = getStatementsParser("self(2 ;");
        ASTBlockStatement node = parser.parseBlockStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad block statement of constructor invocation, no semicolon.
     */
    @Test
    public void testBlockStatementOfBadConstructorInvocationNoSemicolon() {
        StatementsParser parser = getStatementsParser("self(2) return");
        ASTBlockStatement node = parser.parseBlockStatement();
        expectError(node, parser);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment() {
        StatementsParser parser = getStatementsParser("i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTExpressionStatement exprStmt = TestUtility.ensureIsa(node, ASTExpressionStatement.class);
        ASTAssignment assignment = TestUtility.ensureIsa(exprStmt.getStmtExpr(), ASTAssignment.class);
        assertEquals(EQUAL, assignment.getOperator());
        assertNotNull(assignment.getLeftHandSide());
        assertNotNull(assignment.getExpr());
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("i(j);");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTExpressionStatement exprStmt = TestUtility.ensureIsa(node, ASTExpressionStatement.class);
        assertInstanceOf(ASTMethodInvocation.class, exprStmt.getStmtExpr());
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE() {
        StatementsParser parser = getStatementsParser("i.new J();");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTExpressionStatement exprStmt = TestUtility.ensureIsa(node, ASTExpressionStatement.class);
        assertInstanceOf(ASTClassInstanceCreationExpression.class, exprStmt.getStmtExpr());
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn() {
        StatementsParser parser = getStatementsParser("return 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        ensureNoErrors(node, parser);
        ASTReturnStatement returnStmt = TestUtility.ensureIsa(node, ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        assertInstanceOf(ASTPrimary.class, returnStmt.getExpr().get());
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement() {
        StatementsParser parser = getStatementsParser("Integer value = 3;");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement(dt);
        ensureNoErrors(node, parser);
        assertNotNull(node.getLocalVarDecl());
    }

    /**
     * Tests bad local variable declaration statement of no semicolon.
     */
    @Test
    public void testLocalVariableDeclarationStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("Integer i = 3 return;");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement(dt);
        expectError(node, parser);
    }

    /**
     * Tests bad local variable declaration statement of bad assignment.
     */
    @Test
    public void testLocalVariableDeclarationStatementBadAssignment() {
        StatementsParser parser = getStatementsParser("Integer value := 3;");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement(dt);
        expectError(node, parser);
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        ensureNoErrors(node, parser);
        ASTLocalVariableType lvt = node.getLocalVarType();
        assertTrue(lvt.getDataType().isPresent());
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers() {
        StatementsParser parser = getStatementsParser("Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        ensureNoErrors(node, parser);
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator() {
        StatementsParser parser = getStatementsParser("a = b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList() {
        StatementsParser parser = getStatementsParser("x = 1, y = x");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests many variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListMany() {
        StatementsParser parser = getStatementsParser("a = 1, b = a + 1, c = 2 + b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        ensureNoErrors(node, parser);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 3);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier() {
        StatementsParser parser = getStatementsParser("varName");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        ensureNoErrors(node, parser);
        ASTIdentifier varName = node.getVarName();
        assertEquals("varName", varName.getValue());
        assertFalse(node.getVarInitializer().isPresent());
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer() {
        StatementsParser parser = getStatementsParser("count = 2");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        ensureNoErrors(node, parser);
        ASTIdentifier varName = node.getVarName();
        assertEquals("count", varName.getValue());
        assertTrue(node.getVarInitializer().isPresent());
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType() {
        StatementsParser parser = getStatementsParser("spruce.lang.String[][])");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        ensureNoErrors(node, parser);
        assertTrue(node.getDataType().isPresent());
        assertFalse(node.getAutoKeyword().isPresent());
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock() {
        StatementsParser parser = getStatementsParser("{x = x + 1;}");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBlock.class, node);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement() {
        StatementsParser parser = getStatementsParser("x = x + 1;");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        ASTExpressionStatement exprStmt = TestUtility.ensureIsa(node, ASTExpressionStatement.class);
        ASTAssignment assignment = TestUtility.ensureIsa(exprStmt.getStmtExpr(), ASTAssignment.class);
        assertEquals(EQUAL, assignment.getOperator());
        assertNotNull(assignment.getLeftHandSide());
        assertNotNull(assignment.getExpr());
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTReturnStatement.class, node);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBreakStatement.class, node);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTContinueStatement.class, node);
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTIfStatement.class, node);
    }

    /**
     * Tests statement of while statement without braces.
     */
    @Test
    public void testStatementOfWhileStatementNoBlock() {
        StatementsParser parser = getStatementsParser("while (shouldContinue) doWork();");
        ASTStatement node = parser.parseStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement() {
        StatementsParser parser = getStatementsParser("""
            while (shouldContinue) {
                doWork();
            }
            """);
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTWhileStatement.class, node);
    }

    /**
     * Tests statement of for statement, no block.
     */
    @Test
    public void testStatementOfForStatementNoBlock() {
        StatementsParser parser = getStatementsParser("for (;;) doWork();");
        ASTStatement node = parser.parseStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement() {
        StatementsParser parser = getStatementsParser("""
                for (;;) {
                    doWork();
                }
                """);
        ASTStatement node = parser.parseStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTBasicForStatement.class, node);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple() {
        StatementsParser parser = getStatementsParser("if success { return true; }");
        ASTIfStatement node = parser.parseIfStatement();
        ensureNoErrors(node, parser);
        assertFalse(node.getInit().isPresent());
        assertNotNull(node.getCondExpr());
        assertNotNull(node.getIfBlock());
        assertFalse(node.getElseBlock().isPresent());
        assertFalse(node.getElseIf().isPresent());
    }

    /**
     * Tests if statement with init no block.
     */
    @Test
    public void testIfStatementOfInitNoBlock() {
        StatementsParser parser = getStatementsParser("""
                if {String line = br.readLine()} (line != null) stdout.println(line);
                """);
        ASTIfStatement node = parser.parseIfStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                if {String line = br.readLine()} line != null {
                    stdout.println(line);
                }
                """);
        ASTIfStatement node = parser.parseIfStatement();
        ensureNoErrors(node, parser);
        assertTrue(node.getInit().isPresent());
        assertNotNull(node.getCondExpr());
        assertNotNull(node.getIfBlock());
        assertFalse(node.getElseBlock().isPresent());
        assertFalse(node.getElseIf().isPresent());
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse() {
        StatementsParser parser = getStatementsParser("""
            if result {
                stdout.println("Test passed.");
            } else {
                stdout.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        ensureNoErrors(node, parser);
        assertFalse(node.getInit().isPresent());
        assertNotNull(node.getCondExpr());
        assertNotNull(node.getIfBlock());
        assertTrue(node.getElseBlock().isPresent());
        assertFalse(node.getElseIf().isPresent());
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested() {
        StatementsParser parser = getStatementsParser("""
            if result {
                stdout.println("Test passed.");
            } else if DEBUG {
                stdout.println("Test failed in debug mode!");
            } else {
                stdout.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        ensureNoErrors(node, parser);
        assertFalse(node.getInit().isPresent());
        assertNotNull(node.getCondExpr());
        assertNotNull(node.getIfBlock());
        assertFalse(node.getElseBlock().isPresent());
        assertTrue(node.getElseIf().isPresent());

        ASTIfStatement nestedIf = node.getElseIf().get();
        assertFalse(nestedIf.getInit().isPresent());
        assertNotNull(node.getCondExpr());
        assertNotNull(node.getIfBlock());
        assertTrue(nestedIf.getElseBlock().isPresent());
        assertFalse(nestedIf.getElseIf().isPresent());
    }

    /**
     * Test bad if statement of no close brace after init.
     */
    @Test
    public void testIfStatementBadInitNoCloseBrace() {
        StatementsParser parser = getStatementsParser("""
            if {String line = br.readLine() line != null {
                stdout.println("Test passed.");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        expectError(node, parser);
    }

    /**
     * Test bad if statement of bad else clause.
     */
    @Test
    public void testIfStatementBadElse() {
        StatementsParser parser = getStatementsParser("""
            if result {
                stdout.println("Test passed.");
            }
            else stdout.println("Test failed.");
            """);
        ASTIfStatement node = parser.parseIfStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple() {
        StatementsParser parser = getStatementsParser("while shouldContinue { doWork(); }");
        ASTWhileStatement node = parser.parseWhileStatement();
        ensureNoErrors(node, parser);
        assertFalse(node.getInit().isPresent());
        assertNotNull(node.getValueExpr());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests while statement with init without block.
     */
    @Test
    public void testWhileStatementOfInitNoBlock() {
        StatementsParser parser = getStatementsParser("""
                while {String line = br.readLine()} line != null stdout.println(line);
                """);
        ASTWhileStatement node = parser.parseWhileStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                while {String line = br.readLine()} line != null {
                    stdout.println(line);
                }
                """);
        ASTWhileStatement node = parser.parseWhileStatement();
        ensureNoErrors(node, parser);
        assertTrue(node.getInit().isPresent());
        ASTInit init = node.getInit().get();
        assertInstanceOf(ASTLocalVariableDeclaration.class, init);
        assertNotNull(node.getValueExpr());
        assertNotNull(node.getBlock());
    }

    /**
     * Test bad while statement of no close brace after init.
     */
    @Test
    public void testWhileStatementBadInitNoCloseBrace() {
        StatementsParser parser = getStatementsParser("""
            while {String line = br.readLine() line != null {
                stdout.println("Test passed.");
            }
            """);
        ASTWhileStatement node = parser.parseWhileStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad for statement of no open parenthesis.
     */
    @Test
    public void testForStatementNoOpenParen() {
        StatementsParser parser = getStatementsParser("""
                for Int i = 0; i < 10; i = i + 1) {
                    stdout.println(i);
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad for statement of no semicolon or 'in'.
     */
    @Test
    public void testForStatementNoSemicolonOrIn() {
        StatementsParser parser = getStatementsParser("""
                for (Int i = 0) {
                    stdout.println(i);
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3() {
        StatementsParser parser = getStatementsParser("""
                for (Int i = 0; i < 10; i = i + 1) {
                    stdout.println(i);
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        ensureNoErrors(node, parser);
        ASTBasicForStatement basicForStmt = TestUtility.ensureIsa(node, ASTBasicForStatement.class);
        assertTrue(basicForStmt.getInit().isPresent());
        ASTInit init = basicForStmt.getInit().get();
        assertInstanceOf(ASTLocalVariableDeclaration.class, init);
        assertTrue(basicForStmt.getValueExpr().isPresent());
        ASTStatementExpressionList stmtExprList = basicForStmt.getStmtExprList();
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 1);
        assertNotNull(basicForStmt.getBlock());
    }

    /**
     * Tests for statement of basic for statement of infinite loop.
     */
    @Test
    public void testForStatementOfBasicForStatementInfiniteLoop() {
        StatementsParser parser = getStatementsParser("""
        for (;;) {
            stdout.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        ensureNoErrors(node, parser);
        ASTBasicForStatement basicForStmt = TestUtility.ensureIsa(node, ASTBasicForStatement.class);
        assertFalse(basicForStmt.getInit().isPresent());
        assertFalse(basicForStmt.getValueExpr().isPresent());
        ASTStatementExpressionList stmtExprList = basicForStmt.getStmtExprList();
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 0);
    }

    /**
     * Tests bad basic for statement of no first semicolon.
     */
    @Test
    public void testBasicForStatementBadNoFirstSemicolon() {
        StatementsParser parser = getStatementsParser("""
        for (Integer i = 0) {
            stdout.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests bad basic for statement of no second semicolon.
     */
    @Test
    public void testBasicForStatementBadNoSecondSemicolon() {
        StatementsParser parser = getStatementsParser("""
        for (Integer i = 0; i < length) {
            stdout.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad basic for statement of no close parenthesis.
     */
    @Test
    public void testBasicForStatementBadNoCloseParen() {
        StatementsParser parser = getStatementsParser("""
        for (Integer i = 0; i < length; i = i + 1 {
            stdout.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser);
    }

    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement() {
        StatementsParser parser = getStatementsParser("""
                for (Int i in array) {
                    sum = sum + i;
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        ensureNoErrors(node, parser);
        ASTEnhancedForStatement enhForStmt = TestUtility.ensureIsa(node, ASTEnhancedForStatement.class);
        assertNotNull(enhForStmt.getLocalVarDecl());
        assertInstanceOf(ASTPrimary.class, enhForStmt.getValueExpr());
        assertNotNull(enhForStmt.getBlock());
    }

    /**
     * Tests bad enhanced for statement of no close parenthesis.
     */
    @Test
    public void testEnhancedForStatementNoCloseParen() {
        StatementsParser parser = getStatementsParser("""
                for (Int i in array {
                    sum = sum + i;
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad enhanced for statement of bad init of statement expression list.
     */
    @Test
    public void testEnhancedForStatementBadInitOfStatementExprList() {
        StatementsParser parser = getStatementsParser("""
                for (i = 0, j = 0 in array {
                    sum = sum + i;
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        expectError(node, parser, 2);
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement() {
        StatementsParser parser = getStatementsParser("return;");
        ASTReturnStatement node = parser.parseReturnStatement();
        ensureNoErrors(node, parser);
        assertFalse(node.getExpr().isPresent());
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression() {
        StatementsParser parser = getStatementsParser("return x.y + 2;");
        ASTReturnStatement node = parser.parseReturnStatement();
        ensureNoErrors(node, parser);
        assertTrue(node.getExpr().isPresent());
    }

    /**
     * Tests bad return statement of no semicolon.
     */
    @Test
    public void testReturnStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("return}");
        ASTReturnStatement node = parser.parseReturnStatement();
        expectError(node, parser);
    }

    /**
     * Tests bad return statement with expression of no semicolon.
     */
    @Test
    public void testReturnStatementWithExpressionNoSemicolon() {
        StatementsParser parser = getStatementsParser("return false}");
        ASTReturnStatement node = parser.parseReturnStatement();
        expectError(node, parser);
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTBreakStatement node = parser.parseBreakStatement();
        ensureNoErrors(node, parser);
        assertEquals(BREAK, node.getBreakKeyword().getKeyword());
    }

    /**
     * Tests bad break statement of no semicolon.
     */
    @Test
    public void testBreakStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("break}");
        ASTBreakStatement node = parser.parseBreakStatement();
        expectError(node, parser);
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTContinueStatement node = parser.parseContinueStatement();
        ensureNoErrors(node, parser);
        assertEquals(CONTINUE, node.getContinueKeyword().getKeyword());
    }

    /**
     * Tests bad continue statement of no semicolon.
     */
    @Test
    public void testContinueStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("continue}");
        ASTContinueStatement node = parser.parseContinueStatement();
        expectError(node, parser);
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression() {
        StatementsParser parser = getStatementsParser("x();");
        ASTExpressionStatement node = parser.parseExpressionStatement();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodInvocation.class, node.getStmtExpr());
    }

    /**
     * Tests bad expression statement of no semicolon.
     */
    @Test
    public void testExpressionStatementOfNoSemicolon() {
        StatementsParser parser = getStatementsParser("x = x + 1 return");
        ASTExpressionStatement node = parser.parseExpressionStatement();
        expectError(node, parser);
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Int i = 0, j = 0");
        ASTInit node = parser.parseInit();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTLocalVariableDeclaration.class, node);
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTInit node = parser.parseInit();
        ensureNoErrors(node, parser);
        ASTStatementExpressionList stmtExprList = TestUtility.ensureIsa(node, ASTStatementExpressionList.class);
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 1);
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTInit node = parser.parseInit();
        ensureNoErrors(node, parser);
        ASTStatementExpressionList stmtExprList = TestUtility.ensureIsa(node, ASTStatementExpressionList.class);
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        ensureNoErrors(node, parser);
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 1);
    }

    /**
     * Tests statement expression lists of multiple statement expressions.
     */
    @Test
    public void testStatementExpressionListMultiple() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        ensureNoErrors(node, parser);
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment() {
        StatementsParser parser = getStatementsParser("x = 0");
        ASTStatementExpression node = parser.parseStatementExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTAssignment.class, node);
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("x.y(2)");
        ASTStatementExpression node = parser.parseStatementExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTMethodInvocation.class, node);
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression() {
        StatementsParser parser = getStatementsParser("new SideEffect()");
        ASTStatementExpression node = parser.parseStatementExpression();
        ensureNoErrors(node, parser);
        assertInstanceOf(ASTClassInstanceCreationExpression.class, node);
    }

    /**
     * Tests bad statement expression of bad left hand side of literal.
     */
    @Test
    public void testStatementExpressionBadLhsOfLiteral() {
        StatementsParser parser = getStatementsParser("1 = 1");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad statement expression of bad left hand side of class literal.
     */
    @Test
    public void testStatementExpressionBadLhsOfClassLiteral() {
        StatementsParser parser = getStatementsParser("Bad.class = 1");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad statement expression of bad left hand side of self.
     */
    @Test
    public void testStatementExpressionBadLhsOfSelf() {
        StatementsParser parser = getStatementsParser("self = other");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad statement expression of bad left hand side of typename dot self.
     */
    @Test
    public void testStatementExpressionBadLhsOfTypenameSelf() {
        StatementsParser parser = getStatementsParser("Enclosing.self = other");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad statement expression of bad left hand side of parenthesized expression.
     */
    @Test
    public void testStatementExpressionBadLhsOfParenthesizedExpression() {
        StatementsParser parser = getStatementsParser("(a, b) = (1, 2)");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser, 3);
    }

    /**
     * Tests bad statement expression of bad left hand side of instance creation expression.
     */
    @Test
    public void testStatementExpressionBadLhsOfArrayCreationExpression() {
        StatementsParser parser = getStatementsParser("Integer.class = 3");
        ASTStatementExpression node = parser.parseStatementExpression();
        expectError(node, parser);
    }

    /**
     * Tests bad assignment of bad assignment operator.
     */
    @Test
    public void testAssignmentBadOperator() {
        StatementsParser parser = getStatementsParser("a + 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        expectError(node, parser);
    }

    /**
     * Helper method to get a <code>StatementsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>StatementsParser</code> that will parse the given code.
     */
    private static StatementsParser getStatementsParser(String code) {
        return new Parser(new Scanner(code), new BaseMessageProducer()).getStatementsParser();
    }
}
