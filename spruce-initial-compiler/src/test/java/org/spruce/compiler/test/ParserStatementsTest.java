package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.StatementsParser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

import static org.spruce.compiler.ast.ASTListNode.Type.*;

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
        System.out.println(node);
        ASTBlockStatements blockStmts = node.getBlockStmts();
        checkList(blockStmts, BLOCK_STMTS, ASTBlockStatement.class, 1);

        ASTBlock innerBlock = ensureIsa(blockStmts.getChildren().get(0), ASTBlock.class);
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
        System.out.println(node);
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
        System.out.println(node);
        ASTBlockStatements blockStmts = ensureIsa(node.getBlockStmts(), ASTBlockStatements.class);
        checkList(blockStmts, BLOCK_STMTS, ASTBlockStatement.class, 3);
    }

    /**
     * Tests bad block of no open brace.
     */
    @Test
    public void testBlockBadNoOpenBrace() {
        StatementsParser parser = getStatementsParser("int i = 0;");
        assertThrows(CompileException.class, parser::parseBlock, "Expected '{'.");
    }

    /**
     * Tests bad block of no close brace.
     */
    @Test
    public void testBlockBadNoCloseBrace() {
        StatementsParser parser = getStatementsParser("""
                {
                    int i = 0;
                """);
        assertThrows(CompileException.class, parser::parseBlock, "Expected '}'.");
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements() {
        StatementsParser parser = getStatementsParser("""
            String stmt = "Statement one!";
            Integer stmt2Nbr = 2;
            i++;}
            """);
        ASTBlockStatements node = parser.parseBlockStatements();
        System.out.println(node);
        checkList(node, BLOCK_STMTS, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration() {
        StatementsParser parser = getStatementsParser("mut Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        System.out.println(node);
        assertInstanceOf(ASTLocalVariableDeclarationStatement.class, node);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        System.out.println(node);
        assertInstanceOf(ASTLocalVariableDeclarationStatement.class, node);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment() {
        StatementsParser parser = getStatementsParser("i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        System.out.println(node);
        ASTExpressionStatement exprStmt = ensureIsa(node, ASTExpressionStatement.class);
        ASTAssignment assignment = ensureIsa(exprStmt.getStmtExpr(), ASTAssignment.class);
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
        System.out.println(node);
        ASTExpressionStatement exprStmt = ensureIsa(node, ASTExpressionStatement.class);
        assertInstanceOf(ASTMethodInvocation.class, exprStmt.getStmtExpr());
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE() {
        StatementsParser parser = getStatementsParser("i.new J();");
        ASTBlockStatement node = parser.parseBlockStatement();
        System.out.println(node);
        ASTExpressionStatement exprStmt = ensureIsa(node, ASTExpressionStatement.class);
        assertInstanceOf(ASTClassInstanceCreationExpression.class, exprStmt.getStmtExpr());
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTBlockStatement node = parser.parseBlockStatement();
        System.out.println(node);
        ASTReturnStatement returnStmt = ensureIsa(node, ASTReturnStatement.class);
        assertTrue(returnStmt.getExpr().isPresent());
        assertInstanceOf(ASTPrimary.class, returnStmt.getExpr().get());
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement() {
        StatementsParser parser = getStatementsParser("Integer[] values = {1, 2, 3};");
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        System.out.println(node);
        assertNotNull(node.getLocalVarDecl());
    }

    /**
     * Tests bad local variable declaration statement of no semicolon.
     */
    @Test
    public void testLocalVariableDeclarationStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("Integer[] values = {1, 2, 3} return;");
        assertThrows(CompileException.class, parser::parseLocalVariableDeclarationStatement, "Missing semicolon.");
    }

    /**
     * Tests bad local variable declaration statement of bad assignment.
     */
    @Test
    public void testLocalVariableDeclarationStatementBadAssignment() {
        StatementsParser parser = getStatementsParser("Integer[] values := {1, 2, 3};");
        assertThrows(CompileException.class, parser::parseLocalVariableDeclarationStatement,
                "Error: Use '=' for assignment, not ':='.");
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        System.out.println(node);
        checkList(node.getVarModifierList(), VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        ASTLocalVariableType lvt = node.getLocalVarType();
        assertTrue(lvt.getDataType().isPresent());
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers() {
        StatementsParser parser = getStatementsParser("mut Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        System.out.println(node);
        ASTVariableModifierList varModifierList = node.getVarModifierList();
        checkList(varModifierList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTKeywordNode modifier = ensureIsa(varModifierList.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(MUT, modifier.getKeyword());
        checkList(node.getVarDeclList(), VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var");
        ASTVariableModifierList node = parser.parseVariableModifierList();
        System.out.println(node);
        checkList(node, VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTKeywordNode modifier = ensureIsa(node.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(VAR, modifier.getKeyword());
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers() {
        StatementsParser parser = getStatementsParser("var mut");
        ASTVariableModifierList node = parser.parseVariableModifierList();
        System.out.println(node);
        checkList(node, VARIABLE_MODIFIERS, ASTKeywordNode.class, 2);
        ASTKeywordNode modifier1 = ensureIsa(node.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(VAR, modifier1.getKeyword());
        ASTKeywordNode modifier2 = ensureIsa(node.getChildren().get(1), ASTKeywordNode.class);
        assertEquals(MUT, modifier2.getKeyword());
    }

    /**
     * Tests variable modifier of "var".
     */
    @Test
    public void testVariableModifierOfVar() {
        StatementsParser parser = getStatementsParser("var");
        ASTKeywordNode node = parser.parseVariableModifier();
        System.out.println(node);
        assertEquals(VAR, node.getKeyword());
    }

    /**
     * Tests variable modifier of "mut".
     */
    @Test
    public void testVariableModifierOfMut() {
        StatementsParser parser = getStatementsParser("mut");
        ASTKeywordNode node = parser.parseVariableModifier();
        System.out.println(node);
        assertEquals(MUT, node.getKeyword());
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator() {
        StatementsParser parser = getStatementsParser("a = b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        System.out.println(node);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList() {
        StatementsParser parser = getStatementsParser("x = 1, y = x");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        System.out.println(node);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests many variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListMany() {
        StatementsParser parser = getStatementsParser("a = 1, b = a + 1, c = 2 * b");
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        System.out.println(node);
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 3);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier() {
        StatementsParser parser = getStatementsParser("varName");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        System.out.println(node);
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
        System.out.println(node);
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
        System.out.println(node);
        assertTrue(node.getDataType().isPresent());
        assertFalse(node.getAutoKeyword().isPresent());
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto() {
        StatementsParser parser = getStatementsParser("auto");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        System.out.println(node);
        assertFalse(node.getDataType().isPresent());
        assertTrue(node.getAutoKeyword().isPresent());
        assertEquals(AUTO, node.getAutoKeyword().get().getKeyword());
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock() {
        StatementsParser parser = getStatementsParser("{x = x + 1;}");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTBlock.class, node);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement() {
        StatementsParser parser = getStatementsParser("x = x + 1;");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        ASTExpressionStatement exprStmt = ensureIsa(node, ASTExpressionStatement.class);
        ASTAssignment assignment = ensureIsa(exprStmt.getStmtExpr(), ASTAssignment.class);
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
        System.out.println(node);
        assertInstanceOf(ASTReturnStatement.class, node);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement() {
        StatementsParser parser = getStatementsParser("throw new CompileException(\"Error message\");");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTThrowStatement.class, node);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTBreakStatement.class, node);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTContinueStatement.class, node);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTFallthroughStatement.class, node);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement() {
        StatementsParser parser = getStatementsParser("assert status == true;");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTAssertStatement.class, node);
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTIfStatement.class, node);
    }

    /**
     * Tests statement of while statement without braces.
     */
    @Test
    public void testStatementOfWhileStatementNoBlock() {
        StatementsParser parser = getStatementsParser("while (shouldContinue) doWork();");
        assertThrows(CompileException.class, parser::parseStatement, "Error: Expected '{'.");
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
        System.out.println(node);
        assertInstanceOf(ASTWhileStatement.class, node);
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement() {
        StatementsParser parser = getStatementsParser("""
            do {
                work();
            } while shouldContinue;
            """);
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTDoStatement.class, node);
    }

    /**
     * Tests statement of critical statement.
     */
    @Test
    public void testStatementOfCriticalStatement() {
        StatementsParser parser = getStatementsParser("""
            critical myLock {
                myLock.wait();
            }
        """);
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        ASTCriticalStatement criticalStatement = ensureIsa(node, ASTCriticalStatement.class);
        assertNotNull(criticalStatement.getValueExpr());
        assertNotNull(criticalStatement.getBlock());
    }

    /**
     * Tests statement of for statement, no block.
     */
    @Test
    public void testStatementOfForStatementNoBlock() {
        StatementsParser parser = getStatementsParser("for (;;) doWork();");
        assertThrows(CompileException.class, parser::parseStatement, "Expected '{'.");
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
        System.out.println(node);
        assertInstanceOf(ASTBasicForStatement.class, node);
    }

    /**
     * Tests statement of try statement.
     */
    @Test
    public void testStatementOfTryStatement() {
        StatementsParser parser = getStatementsParser("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTTryStatement.class, node);
    }

    /**
     * Tests statement of switch statement.
     */
    @Test
    public void testStatementOfSwitchStatement() {
        StatementsParser parser = getStatementsParser("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """);
        ASTStatement node = parser.parseStatement();
        System.out.println(node);
        assertInstanceOf(ASTSwitchStatement.class, node);
    }

    /**
     * Tests Switch Statement Rules.
     */
    @Test
    public void testSwitchStatementRules() {
        StatementsParser parser = getStatementsParser("""
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                """);
        ASTSwitchStatementRules node = parser.parseSwitchStatementRules();
        System.out.println(node);
        checkList(node, SWITCH_STMT_RULES, ASTSwitchStatementRule.class, 3);
    }

    /**
     * Tests switch statement rule of switch label and expression statement.
     */
    @Test
    public void testSwitchStatementRuleOfExprStmt() {
        StatementsParser parser = getStatementsParser("""
                case 1 -> out.println("One");
                """);
        ASTSwitchStatementRule node = parser.parseSwitchStatementRule();
        System.out.println(node);
        assertNotNull(node.getSwitchLabel());
        assertTrue(node.getExprStmt().isPresent());
        assertFalse(node.getBlock().isPresent());
        assertFalse(node.getThrowStmt().isPresent());
    }

    /**
     * Tests switch statement rule of switch label and block.
     */
    @Test
    public void testSwitchStatementRuleOfBlock() {
        StatementsParser parser = getStatementsParser("""
                case 1 -> {
                    out.println("One");
                }
                """);
        ASTSwitchStatementRule node = parser.parseSwitchStatementRule();
        System.out.println(node);
        assertNotNull(node.getSwitchLabel());
        assertFalse(node.getExprStmt().isPresent());
        assertTrue(node.getBlock().isPresent());
        assertFalse(node.getThrowStmt().isPresent());
    }

    /**
     * Tests switch statement rule of switch label and throw statement.
     */
    @Test
    public void testSwitchStatementRuleOfThrowStmt() {
        StatementsParser parser = getStatementsParser("""
                case 1 -> throw new TestException("Test");
                """);
        ASTSwitchStatementRule node = parser.parseSwitchStatementRule();
        System.out.println(node);
        assertNotNull(node.getSwitchLabel());
        assertFalse(node.getExprStmt().isPresent());
        assertFalse(node.getBlock().isPresent());
        assertTrue(node.getThrowStmt().isPresent());
    }

    /**
     * Tests bad switch statement of no arrow.
     */
    @Test
    public void testSwitchStatementRuleNoArrow() {
        StatementsParser parser = getStatementsParser("""
                case 1 throw new TestException("Test");
                """);
        assertThrows(CompileException.class, parser::parseSwitchStatementRule, "Expected arrow (->).");
    }

    /**
     * Tests switch statement.
     */
    @Test
    public void testSwitchStatement() {
        StatementsParser parser = getStatementsParser("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """);
        ASTSwitchStatement node = parser.parseSwitchStatement();
        System.out.println(node);
        assertNotNull(node.getValueExpr());
        ASTSwitchStatementRules switchStmtRules = ensureIsa(node.getSwitchStmtRules(), ASTSwitchStatementRules.class);
        checkList(switchStmtRules, SWITCH_STMT_RULES, ASTSwitchStatementRule.class, 3);
    }

    /**
     * Tests empty switch block.
     */
    @Test
    public void testSwitchBlockEmpty() {
        StatementsParser parser = getStatementsParser("{}");
        assertThrows(CompileException.class, parser::parseSwitchStatementBlock, "Error at code \"{}\".");
    }

    /**
     * Tests try statement of catch.
     */
    @Test
    public void testTryStatementOfCatch() {
        StatementsParser parser = getStatementsParser("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        System.out.println(node);
        assertFalse(node.getResourceSpec().isPresent());
        assertNotNull(node.getBlock());
        assertTrue(node.getCatches().isPresent());
        ASTCatches catches = node.getCatches().get();
        checkList(catches, CATCH_CLAUSES, ASTCatchClause.class, 1);
        assertFalse(node.getFinallyBlock().isPresent());
    }

    /**
     * Tests try statement of finally.
     */
    @Test
    public void testTryStatementOfFinally() {
        StatementsParser parser = getStatementsParser("""
                try {
                    br.readLine();
                } finally {
                    br.close();
                }
                """);
        ASTTryStatement node = parser.parseTryStatement();
        System.out.println(node);
        assertFalse(node.getResourceSpec().isPresent());
        assertNotNull(node.getBlock());
        assertFalse(node.getCatches().isPresent());
        assertTrue(node.getFinallyBlock().isPresent());
    }

    /**
     * Tests try statement of resource specification.
     */
    @Test
    public void testTryStatementOfResourceSpecification() {
        StatementsParser parser = getStatementsParser("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        System.out.println(node);
        assertTrue(node.getResourceSpec().isPresent());
        assertNotNull(node.getBlock());
        assertFalse(node.getCatches().isPresent());
        assertFalse(node.getFinallyBlock().isPresent());
    }

    /**
     * Tests try statement of all optionals.
     */
    @Test
    public void testTryStatementOfAll() {
        StatementsParser parser = getStatementsParser("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            } finally {
                br.close();
            }
            """);
        ASTTryStatement node = parser.parseTryStatement();
        System.out.println(node);
        assertTrue(node.getResourceSpec().isPresent());
        assertNotNull(node.getBlock());
        assertTrue(node.getCatches().isPresent());
        ASTCatches catches = node.getCatches().get();
        checkList(catches, CATCH_CLAUSES, ASTCatchClause.class, 1);
        assertTrue(node.getFinallyBlock().isPresent());
    }

    /**
     * Test bad try statement without a resource specification, a catches, or
     * a "finally".
     */
    @Test
    public void testTryStatementBad() {
        StatementsParser parser = getStatementsParser("""
            try {
                br.readLine();
            }
            """);
        assertThrows(CompileException.class, parser::parseTryStatement, "Expected 'catch' and/or 'finally' block.");
    }

    /**
     * Tests resource specification of resource list.
     */
    @Test
    public void testResourceSpecification() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr))");
        ASTResourceList node = parser.parseResourceSpecification();
        System.out.println(node);
        checkList(node, RESOURCES, ASTResource.class, 2);
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr);)");
        ASTResourceList node = parser.parseResourceSpecification();
        System.out.println(node);
        checkList(node, RESOURCES, ASTResource.class, 2);
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResourceList node = parser.parseResourceList();
        System.out.println(node);
        checkList(node, RESOURCES, ASTResource.class, 1);
    }

    /**
     * Tests resource list of multiple resources.
     */
    @Test
    public void testResourceListMultipleResources() {
        StatementsParser parser = getStatementsParser("fr; BufferedReader br = new BufferedReader(fr)");
        ASTResourceList node = parser.parseResourceList();
        System.out.println(node);
        checkList(node, RESOURCES, ASTResource.class, 2);
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResource node = parser.parseResource();
        System.out.println(node);
        assertInstanceOf(ASTResourceDeclaration.class, node);
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName() {
        StatementsParser parser = getStatementsParser("br");
        ASTResource node = parser.parseResource();
        System.out.println(node);
        ASTExpressionName exprName = ensureIsa(node, ASTExpressionName.class);
        checkList(exprName, EXPR_NAME_IDS, ASTIdentifier.class, 1);
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess() {
        StatementsParser parser = getStatementsParser("super.br");
        ASTResource node = parser.parseResource();
        System.out.println(node);
        assertInstanceOf(ASTFieldAccess.class, node);
    }

    /**
     * Test bad resource of primary not of field access or expression name.
     */
    @Test
    public void testResourceOfBadPrimary() {
        StatementsParser parser = getStatementsParser("Foo::bar");
        assertThrows(CompileException.class, parser::parseResource, "Expected resource declaration or variable.");
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        System.out.println(node);
        ASTVariableModifierList varModifierList = node.getVarModifierList();
        checkList(varModifierList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        assertInstanceOf(ASTLocalVariableType.class, node.getLocalVarType());
        ASTIdentifier resName = node.getResourceName();
        assertEquals("br", resName.getValue());
        ASTExpression expr = node.getExpression();
        assertInstanceOf(ASTPrimary.class, expr);
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        System.out.println(node);
        ASTVariableModifierList varModifierList = node.getVarModifierList();
        checkList(varModifierList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTKeywordNode modifierVar = ensureIsa(varModifierList.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(VAR, modifierVar.getKeyword());
        assertInstanceOf(ASTLocalVariableType.class, node.getLocalVarType());
        ASTIdentifier resName = node.getResourceName();
        assertEquals("br", resName.getValue());
        ASTExpression expr = node.getExpression();
        assertInstanceOf(ASTPrimary.class, expr);
    }

    /**
     * Tests bad resource declaration of no equals (variable modifier).
     */
    @Test
    public void testResourceDeclarationVarModifierNoEquals() {
        StatementsParser parser = getStatementsParser("var BufferedReader br;");
        assertThrows(CompileException.class, parser::parseResourceDeclaration, "Expected '='.");
    }

    /**
     * Tests bad resource declaration of no equals (no variable modifier).
     */
    @Test
    public void testResourceDeclarationNoEquals() {
        StatementsParser parser = getStatementsParser("BufferedReader br;");
        ASTDataType dt = parser.getTypesParser().parseDataType();
        assertThrows(CompileException.class, () -> parser.parseResourceDeclaration(dt), "Expected '='.");
    }

    /**
     * Test catches of catch clauses.
     */
    @Test
    public void testCatches() {
        StatementsParser parser = getStatementsParser("""
            catch (FileNotFoundException e) {
                err.println(e.getMessage());
            }
            catch (IOException e) {
                out.println(e.getMessage());
            }
            """);
        ASTCatches node = parser.parseCatches();
        System.out.println(node);
        checkList(node, CATCH_CLAUSES, ASTCatchClause.class, 2);
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause() {
        StatementsParser parser = getStatementsParser("catch (CompileException ce) { out.println(ce.getMessage()); }");
        ASTCatchClause node = parser.parseCatchClause();
        System.out.println(node);
        assertNotNull(node.getCatchFormalParam());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests bad catch clause of no open parenthesis.
     */
    @Test
    public void testCatchClauseNoOpenParen() {
        StatementsParser parser = getStatementsParser("catch CompileException ce) { out.println(ce.getMessage()); }");
        assertThrows(CompileException.class, parser::parseCatchClause, "Expected '('");
    }

    /**
     * Tests bad catch clause of no close parenthesis.
     */
    @Test
    public void testCatchClauseNoCloseParen() {
        StatementsParser parser = getStatementsParser("catch (CompileException ce { out.println(ce.getMessage()); }");
        assertThrows(CompileException.class, parser::parseCatchClause, "Expected ')'");
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType() {
        StatementsParser parser = getStatementsParser("Exception");
        ASTCatchType node = parser.parseCatchType();
        System.out.println(node);
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 1);
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter() {
        StatementsParser parser = getStatementsParser("Exception e");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        System.out.println(node);
        ASTVariableModifierList varModifierList = node.getVarModifierList();
        checkList(varModifierList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 0);
        ASTCatchType catchType = node.getCatchType();
        checkList(catchType, INTERSECTION_TYPES, ASTDataType.class, 1);
        ASTIdentifier varName = node.getVarName();
        assertEquals("e", varName.getValue());
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers() {
        StatementsParser parser = getStatementsParser("var CustomException ce");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        System.out.println(node);
        ASTVariableModifierList varModifierList = node.getVarModifierList();
        checkList(varModifierList, VARIABLE_MODIFIERS, ASTKeywordNode.class, 1);
        ASTKeywordNode modifierVar = ensureIsa(varModifierList.getChildren().get(0), ASTKeywordNode.class);
        assertEquals(VAR, modifierVar.getKeyword());
        ASTCatchType catchType = node.getCatchType();
        checkList(catchType, INTERSECTION_TYPES, ASTDataType.class, 1);
        ASTIdentifier varName = node.getVarName();
        assertEquals("ce", varName.getValue());
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType() {
        StatementsParser parser = getStatementsParser("IOException | SQLException");
        ASTCatchType node = parser.parseCatchType();
        System.out.println(node);
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests catch type of data types.
     */
    @Test
    public void testCatchTypeOfDataTypes() {
        StatementsParser parser = getStatementsParser("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException");
        ASTCatchType node = parser.parseCatchType();
        System.out.println(node);
        checkList(node, INTERSECTION_TYPES, ASTDataType.class, 3);
    }

    /**
     * Tests finally block.
     */
    @Test
    public void testFinally() {
        StatementsParser parser = getStatementsParser("""
                finally {
                    out.println("Always executed!");
                }
                """);
        ASTBlock node = parser.parseFinally();
        System.out.println(node);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple() {
        StatementsParser parser = getStatementsParser("if success { return true; }");
        ASTIfStatement node = parser.parseIfStatement();
        System.out.println(node);
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
                if {String line = br.readLine()} (line != null) out.println(line);
                """);
        assertThrows(CompileException.class, parser::parseIfStatement, "Expected '{'.");
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                if {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """);
        ASTIfStatement node = parser.parseIfStatement();
        System.out.println(node);
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
                out.println("Test passed.");
            } else {
                out.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        System.out.println(node);
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
                out.println("Test passed.");
            } else if DEBUG {
                out.println("Test failed in debug mode!");
            } else {
                out.println("Test FAILED!");
            }
            """);
        ASTIfStatement node = parser.parseIfStatement();
        System.out.println(node);
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
                out.println("Test passed.");
            }
            """);
        assertThrows(CompileException.class, parser::parseIfStatement, "Expected '}'.");
    }

    /**
     * Test bad if statement of bad else clause.
     */
    @Test
    public void testIfStatementBadElse() {
        StatementsParser parser = getStatementsParser("""
            if result {
                out.println("Test passed.");
            }
            else out.println("Test failed.");
            """);
        assertThrows(CompileException.class, parser::parseIfStatement, "Expected 'if' or a block.");
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple() {
        StatementsParser parser = getStatementsParser("while shouldContinue { doWork(); }");
        ASTWhileStatement node = parser.parseWhileStatement();
        System.out.println(node);
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
                while {String line = br.readLine()} line != null out.println(line);
                """);
        assertThrows(CompileException.class, parser::parseWhileStatement, "Expected '{'.");
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit() {
        StatementsParser parser = getStatementsParser("""
                while {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """);
        ASTWhileStatement node = parser.parseWhileStatement();
        System.out.println(node);
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
                out.println("Test passed.");
            }
            """);
        assertThrows(CompileException.class, parser::parseWhileStatement, "Expected '}'.");
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement() {
        StatementsParser parser = getStatementsParser("do { work(); } while shouldContinue;");
        ASTDoStatement node = parser.parseDoStatement();
        System.out.println(node);
        assertNotNull(node.getValueExpr());
        assertNotNull(node.getBlock());
    }

    /**
     * Tests bad do statement of no while.
     */
    @Test
    public void testDoStatementNoWhile() {
        StatementsParser parser = getStatementsParser("do { something(); };");
        assertThrows(CompileException.class, parser::parseDoStatement, "Expected while.");
    }

    /**
     * Tests bad do statement of no semicolon.
     */
    @Test
    public void testDoStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("""
                do { something(); }
                while (condition)
                return;
                """);
        assertThrows(CompileException.class, parser::parseDoStatement, "Expected semicolon.");
    }

    /**
     * Tests bad for statement of no open parenthesis.
     */
    @Test
    public void testForStatementNoOpenParen() {
        StatementsParser parser = getStatementsParser("""
                for Int i = 0; i < 10; i++) {
                    out.println(i);
                }
                """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected '('.");
    }

    /**
     * Tests bad for statement of no semicolon or colon.
     */
    @Test
    public void testForStatementNoSemicolonOrColon() {
        StatementsParser parser = getStatementsParser("""
                for (Int i = 0) {
                    out.println(i);
                }
                """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected semicolon or colon.");
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3() {
        StatementsParser parser = getStatementsParser("""
                for (Int i = 0; i < 10; i++) {
                    out.println(i);
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        System.out.println(node);
        ASTBasicForStatement basicForStmt = ensureIsa(node, ASTBasicForStatement.class);
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
            out.println("Hello world!");
        }
        """);
        ASTForStatement node = parser.parseForStatement();
        System.out.println(node);
        ASTBasicForStatement basicForStmt = ensureIsa(node, ASTBasicForStatement.class);
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
            out.println("Hello world!");
        }
        """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected semicolon.");
    }

    /**
     * Tests bad basic for statement of no second semicolon.
     */
    @Test
    public void testBasicForStatementBadNoSecondSemicolon() {
        StatementsParser parser = getStatementsParser("""
        for (Integer i = 0; i < length) {
            out.println("Hello world!");
        }
        """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected second semicolon.");
    }

    /**
     * Tests bad basic for statement of no close parenthesis.
     */
    @Test
    public void testBasicForStatementBadNoCloseParen() {
        StatementsParser parser = getStatementsParser("""
        for (Integer i = 0; i < length; i++ {
            out.println("Hello world!");
        }
        """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected ')'.");
    }

    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement() {
        StatementsParser parser = getStatementsParser("""
                for (Int i : array) {
                    sum += i;
                }
                """);
        ASTForStatement node = parser.parseForStatement();
        System.out.println(node);
        ASTEnhancedForStatement enhForStmt = ensureIsa(node, ASTEnhancedForStatement.class);
        assertNotNull(enhForStmt.getLocalVarDecl());
        assertInstanceOf(ASTPrimary.class, enhForStmt.getCondExpr());
        assertNotNull(enhForStmt.getBlock());
    }

    /**
     * Tests bad enhanced for statement of no close parenthesis.
     */
    @Test
    public void testEnhancedForStatementNoCloseParen() {
        StatementsParser parser = getStatementsParser("""
                for (Int i : array {
                    sum += i;
                }
                """);
        assertThrows(CompileException.class, parser::parseForStatement, "Expected ')'.");
    }

    /**
     * Tests bad enhanced for statement of bad init of statement expression list.
     */
    @Test
    public void testEnhancedForStatementBadInitOfStatementExprList() {
        StatementsParser parser = getStatementsParser("""
                for (i = 0, j = 0 : array {
                    sum += i;
                }
                """);
        assertThrows(CompileException.class, parser::parseForStatement,
                "Enhanced for loop requires a variable declaration before the colon.");
    }

    /**
     * Tests yield statement.
     */
    @Test
    public void testYieldStatement() {
        StatementsParser parser = getStatementsParser("yield x.y + 2;");
        ASTYieldStatement node = parser.parseYieldStatement();
        System.out.println(node);
        assertNotNull(node.getExpr());
    }

    /**
     * Tests bad yield statement of no semicolon.
     */
    @Test
    public void testYieldStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("yield x.y + 2}");
        assertThrows(CompileException.class, parser::parseYieldStatement, "Missing semicolon.");
    }

    /**
     * Tests use statement.
     */
    @Test
    public void testUseStatement() {
        StatementsParser parser = getStatementsParser("use x.y + 2;");
        ASTUseStatement node = parser.parseUseStatement();
        System.out.println(node);
        assertNotNull(node.getExpr());
    }

    /**
     * Tests bad use statement of no semicolon.
     */
    @Test
    public void testUseStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("use x.y + 2}");
        assertThrows(CompileException.class, parser::parseUseStatement, "Missing semicolon.");
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement() {
        StatementsParser parser = getStatementsParser("return;");
        ASTReturnStatement node = parser.parseReturnStatement();
        System.out.println(node);
        assertFalse(node.getExpr().isPresent());
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression() {
        StatementsParser parser = getStatementsParser("return x.y + 2;");
        ASTReturnStatement node = parser.parseReturnStatement();
        System.out.println(node);
        assertTrue(node.getExpr().isPresent());
    }

    /**
     * Tests bad return statement of no semicolon.
     */
    @Test
    public void testReturnStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("return}");
        assertThrows(CompileException.class, parser::parseReturnStatement, "Missing semicolon.");
    }

    /**
     * Tests bad return statement with expression of no semicolon.
     */
    @Test
    public void testReturnStatementWithExpressionNoSemicolon() {
        StatementsParser parser = getStatementsParser("return false}");
        assertThrows(CompileException.class, parser::parseReturnStatement, "Missing semicolon.");
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression() {
        StatementsParser parser = getStatementsParser("throw new Exception();");
        ASTThrowStatement node = parser.parseThrowStatement();
        System.out.println(node);
        assertNotNull(node.getValueExpr());
    }

    /**
     * Tests bad throw statement of no semicolon.
     */
    @Test
    public void testThrowStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("throw new Exception()}");
        assertThrows(CompileException.class, parser::parseThrowStatement, "Missing semicolon.");
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTBreakStatement node = parser.parseBreakStatement();
        System.out.println(node);
        assertEquals(BREAK, node.getBreakKeyword().getKeyword());
    }

    /**
     * Tests bad break statement of no semicolon.
     */
    @Test
    public void testBreakStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("break}");
        assertThrows(CompileException.class, parser::parseBreakStatement, "Missing semicolon.");
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTContinueStatement node = parser.parseContinueStatement();
        System.out.println(node);
        assertEquals(CONTINUE, node.getContinueKeyword().getKeyword());
    }

    /**
     * Tests bad continue statement of no semicolon.
     */
    @Test
    public void testContinueStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("continue}");
        assertThrows(CompileException.class, parser::parseBreakStatement, "Missing semicolon.");
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        System.out.println(node);
        assertEquals(FALLTHROUGH, node.getFallthroughKeyword().getKeyword());
    }

    /**
     * Tests bad fallthrough statement of no semicolon.
     */
    @Test
    public void testFallthroughStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("fallthrough}");
        assertThrows(CompileException.class, parser::parseBreakStatement, "Missing semicolon.");
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementOfExpression() {
        StatementsParser parser = getStatementsParser("assert result == true;");
        ASTAssertStatement node = parser.parseAssertStatement();
        System.out.println(node);
        assertInstanceOf(ASTBinaryExpression.class, node.getCondExprCondition());
        assertFalse(node.getCondExprMessage().isPresent());
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions() {
        StatementsParser parser = getStatementsParser("assert result == true : \"Assertion failed!\";");
        ASTAssertStatement node = parser.parseAssertStatement();
        System.out.println(node);
        assertInstanceOf(ASTBinaryExpression.class, node.getCondExprCondition());
        assertTrue(node.getCondExprMessage().isPresent());
        assertInstanceOf(ASTPrimary.class, node.getCondExprMessage().get());
    }

    /**
     * Tests bad assert statement of no semicolon.
     */
    @Test
    public void testAssertStatementNoSemicolon() {
        StatementsParser parser = getStatementsParser("assert whether : \"Missing semicolon!\"");
        assertThrows(CompileException.class, parser::parseAssertStatement, "Missing semicolon.");
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression() {
        StatementsParser parser = getStatementsParser("x++;");
        ASTExpressionStatement node = parser.parseExpressionStatement();
        System.out.println(node);
        assertInstanceOf(ASTPostfix.class, node.getStmtExpr());
    }

    /**
     * Tests bad expression statement of no semicolon.
     */
    @Test
    public void testExpressionStatementOfNoSemicolon() {
        StatementsParser parser = getStatementsParser("x++ return");
        assertThrows(CompileException.class, parser::parseExpressionStatement, "Semicolon expected.");
    }

    /**
     * Tests postfix of increment.
     */
    @Test
    public void testPostfixIncrement() {
        StatementsParser parser = getStatementsParser("a++;");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parsePrimary().getLeftHandSide();
        ASTPostfix node = parser.parsePostfix(lhs.getLocation(), lhs);
        assertNotNull(node.getLeftHandSide());
        assertEquals(INCREMENT, node.getOperator());
    }

    /**
     * Tests postfix of decrement.
     */
    @Test
    public void testPostfixDecrement() {
        StatementsParser parser = getStatementsParser("b--;");
        ASTLeftHandSide lhs = parser.getExpressionsParser().parsePrimary().getLeftHandSide();
        ASTPostfix node = parser.parsePostfix(lhs.getLocation(), lhs);
        assertNotNull(node.getLeftHandSide());
        assertEquals(DECREMENT, node.getOperator());
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Int i = 0, j = 0");
        ASTInit node = parser.parseInit();
        System.out.println(node);
        assertInstanceOf(ASTLocalVariableDeclaration.class, node);
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTInit node = parser.parseInit();
        System.out.println(node);
        ASTStatementExpressionList stmtExprList = ensureIsa(node, ASTStatementExpressionList.class);
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 1);
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTInit node = parser.parseInit();
        System.out.println(node);
        ASTStatementExpressionList stmtExprList = ensureIsa(node, ASTStatementExpressionList.class);
        checkList(stmtExprList, STMT_EXPRS, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        System.out.println(node);
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 1);
    }

    /**
     * Tests statement expression lists of multiple statement expressions.
     */
    @Test
    public void testStatementExpressionListMultiple() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        System.out.println(node);
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment() {
        StatementsParser parser = getStatementsParser("x = 0");
        ASTStatementExpression node = parser.parseStatementExpression();
        System.out.println(node);
        assertInstanceOf(ASTAssignment.class, node);
    }

    /**
     * Tests statement expression of assignment of left hand side of element access.
     */
    @Test
    public void testStatementExpressionOfAssignmentOfLhsOfElementAccess() {
        StatementsParser parser = getStatementsParser("x[i] = 0");
        ASTStatementExpression node = parser.parseStatementExpression();
        System.out.println(node);
        assertInstanceOf(ASTAssignment.class, node);
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression() {
        StatementsParser parser = getStatementsParser("x.y++");
        ASTStatementExpression node = parser.parseStatementExpression();
        System.out.println(node);
        assertInstanceOf(ASTPostfix.class, node);
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("x.y(2)");
        ASTStatementExpression node = parser.parseStatementExpression();
        System.out.println(node);
        assertInstanceOf(ASTMethodInvocation.class, node);
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression() {
        StatementsParser parser = getStatementsParser("new SideEffect()");
        ASTStatementExpression node = parser.parseStatementExpression();
        System.out.println(node);
        assertInstanceOf(ASTClassInstanceCreationExpression.class, node);
    }

    /**
     * Tests bad statement expression of bad left hand side of literal.
     */
    @Test
    public void testStatementExpressionBadLhsOfLiteral() {
        StatementsParser parser = getStatementsParser("1 = 1");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of class literal.
     */
    @Test
    public void testStatementExpressionBadLhsOfClassLiteral() {
        StatementsParser parser = getStatementsParser("Bad.class = 1");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of self.
     */
    @Test
    public void testStatementExpressionBadLhsOfSelf() {
        StatementsParser parser = getStatementsParser("self = other");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of typename dot self.
     */
    @Test
    public void testStatementExpressionBadLhsOfTypenameSelf() {
        StatementsParser parser = getStatementsParser("Enclosing.self = other");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of parenthesized expression.
     */
    @Test
    public void testStatementExpressionBadLhsOfParenthesizedExpression() {
        StatementsParser parser = getStatementsParser("(a, b) = (1, 2)");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of array creation expression.
     */
    @Test
    public void testStatementExpressionBadLhsOfArrayCreationExpression() {
        StatementsParser parser = getStatementsParser("new Integer[3] = 3");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests bad statement expression of bad left hand side of method reference.
     */
    @Test
    public void testStatementExpressionBadLhsOfMethodReference() {
        StatementsParser parser = getStatementsParser("Foo::bar = 3");
        assertThrows(CompileException.class, parser::parseStatementExpression,
                "Expected variable or element access.");
    }

    /**
     * Tests assignment expression of assignment made of lhs, "+=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPlusEquals() {
        StatementsParser parser = getStatementsParser("a += 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(PLUS_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "-=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfMinusEquals() {
        StatementsParser parser = getStatementsParser("a -= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(MINUS_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "*=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfStarEquals() {
        StatementsParser parser = getStatementsParser("a *= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(STAR_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "/=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfSlashEquals() {
        StatementsParser parser = getStatementsParser("a /= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(SLASH_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "%=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPercentEquals() {
        StatementsParser parser = getStatementsParser("a %= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(PERCENT_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "<<=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftLeftEquals() {
        StatementsParser parser = getStatementsParser("a <<= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(SHIFT_LEFT_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, ">>=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftRightEquals() {
        StatementsParser parser = getStatementsParser("a >>= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(SHIFT_RIGHT_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "|=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfOrEquals() {
        StatementsParser parser = getStatementsParser("a |= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(PIPE_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "&=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfAndEquals() {
        StatementsParser parser = getStatementsParser("a &= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(AMPERSAND_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
    }

    /**
     * Tests assignment expression of assignment made of lhs, "^=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfXorEquals() {
        StatementsParser parser = getStatementsParser("a ^= 1");
        ExpressionsParser exprParser = parser.getExpressionsParser();
        ASTPrimary primary = exprParser.parsePrimary();
        ASTLeftHandSide lhs = primary.getLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        System.out.println(node);
        assertEquals(CARET_EQUALS, node.getOperator());
        assertNotNull(node.getLeftHandSide());
        assertNotNull(node.getExpr());
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
        assertThrows(CompileException.class, () -> parser.parseAssignment(lhs.getLocation(), lhs),
                "Expected assignment operator.");
    }

    /**
     * Helper method to get a <code>StatementsParser</code> directly from code.
     * @param code The code to test.
     * @return A <code>StatementsParser</code> that will parse the given code.
     */
    private static StatementsParser getStatementsParser(String code) {
        return new Parser(new Scanner(code)).getStatementsParser();
    }
}
