package org.spruce.compiler.test;

import org.spruce.compiler.ast.ASTBinaryNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTModifierNode;
import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.StatementsParser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

import static org.spruce.compiler.ast.ASTListNode.Type.*;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest {

    @Test
    public void testNestedBlocks() {
        StatementsParser parser = getStatementsParser("""
            {
                {
                    Int a = 1;
                }
            }
            """);
        ASTBlock node = parser.parseBlock();
        node.print();
        checkSimple(node, ASTListNode.class, OPEN_BRACE);
        ASTListNode blockStmts = (ASTListNode) node.getChildren().get(0);
        checkList(blockStmts, BLOCK_STATEMENTS, ASTBlockStatement.class, 1);
        ASTBlockStatement blockStmt = (ASTBlockStatement) blockStmts.getChildren().get(0);
        checkSimple(blockStmt, ASTStatement.class);
        ASTStatement stmt = (ASTStatement) blockStmt.getChildren().get(0);
        checkSimple(stmt, ASTBlock.class);
    }

    /**
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing() {
        StatementsParser parser = getStatementsParser("{}");
        ASTBlock node = parser.parseBlock();
        node.print();
        checkEmpty(node, OPEN_BRACE);
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
        node.print();
        checkSimple(node, ASTListNode.class, OPEN_BRACE);
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
        ASTListNode node = parser.parseBlockStatements();
        node.print();
        checkList(node, BLOCK_STATEMENTS, ASTBlockStatement.class, 3);
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration() {
        StatementsParser parser = getStatementsParser("Integer i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment() {
        StatementsParser parser = getStatementsParser("i = 1;");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("i(j);");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE() {
        StatementsParser parser = getStatementsParser("i.new J();");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTBlockStatement node = parser.parseBlockStatement();
        node.print();
        checkSimple(node, ASTStatement.class);
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement() {
        StatementsParser parser = getStatementsParser("Integer[] values = {1, 2, 3};");
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        node.print();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
    }

    /**
     * Tests bad local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatementBad() {
        StatementsParser parser = getStatementsParser("Integer[] values := {1, 2, 3};");
        assertThrows(CompileException.class, parser::parseLocalVariableDeclarationStatement, "Error: Use '=' for assignment, not ':='.");
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Boolean result = true, done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        node.print();
        checkBinary(node, ASTLocalVariableType.class, ASTListNode.class);
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers() {
        StatementsParser parser = getStatementsParser("mut Boolean result = true, var done = false");
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        node.print();
        checkTrinary(node, null, ASTListNode.class, ASTLocalVariableType.class, ASTListNode.class);
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var");
        ASTListNode node = parser.parseVariableModifierList();
        node.print();
        checkList(node, VARIABLE_MODIFIERS, ASTModifierNode.class, 1);
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers() {
        StatementsParser parser = getStatementsParser("var mut");
        ASTListNode node = parser.parseVariableModifierList();
        node.print();
        checkList(node, VARIABLE_MODIFIERS, ASTModifierNode.class, 2);
    }

    /**
     * Tests variable modifier of "var".
     */
    @Test
    public void testVariableModifierOfVar() {
        StatementsParser parser = getStatementsParser("var");
        ASTModifierNode node = parser.parseVariableModifier();
        node.print();
        assertEquals(VAR, node.getOperation());
    }

    /**
     * Tests variable modifier of "mut".
     */
    @Test
    public void testVariableModifierOfMut() {
        StatementsParser parser = getStatementsParser("mut");
        ASTModifierNode node = parser.parseVariableModifier();
        node.print();
        assertEquals(MUT, node.getOperation());
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator() {
        StatementsParser parser = getStatementsParser("a = b");
        ASTListNode node = parser.parseVariableDeclaratorList();
        node.print();
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 1);
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList() {
        StatementsParser parser = getStatementsParser("x = 1, y = x");
        ASTListNode node = parser.parseVariableDeclaratorList();
        node.print();
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 2);
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested() {
        StatementsParser parser = getStatementsParser("a = 1, b = a + 1, c = 2 * b");
        ASTListNode node = parser.parseVariableDeclaratorList();
        node.print();
        checkList(node, VARIABLE_DECLARATORS, ASTVariableDeclarator.class, 3);
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier() {
        StatementsParser parser = getStatementsParser("varName");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        node.print();
        checkSimple(node, ASTIdentifier.class);
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer() {
        StatementsParser parser = getStatementsParser("count = 2");
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        node.print();
        checkBinary(node, EQUAL, ASTIdentifier.class, ASTPrimary.class);
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType() {
        StatementsParser parser = getStatementsParser("spruce.lang.String[][])");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        node.print();
        checkSimple(node, ASTDataType.class);
    }

    /**
     * Tests local variable type of "auto".
     */
    @Test
    public void testLocalVariableTypeOfAuto() {
        StatementsParser parser = getStatementsParser("auto");
        ASTLocalVariableType node = parser.parseLocalVariableType();
        node.print();
        checkEmpty(node, AUTO);
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock() {
        StatementsParser parser = getStatementsParser("{x = x + 1;}");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTBlock.class);
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement() {
        StatementsParser parser = getStatementsParser("x = x + 1;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTExpressionStatement.class);
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement() {
        StatementsParser parser = getStatementsParser("return true;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTReturnStatement.class);
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement() {
        StatementsParser parser = getStatementsParser("throw new CompileException(\"Error message\");");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTThrowStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTBreakStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTContinueStatement.class);
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTFallthroughStatement.class);
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement() {
        StatementsParser parser = getStatementsParser("assert status == true;");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTAssertStatement.class);
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTStatement node = parser.parseStatement();
        node.print();
        checkSimple(node, ASTIfStatement.class);
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
        node.print();
        checkSimple(node, ASTWhileStatement.class);
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
        node.print();
        checkSimple(node, ASTDoStatement.class);
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
        node.print();
        checkSimple(node, ASTCriticalStatement.class);
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
        node.print();
        checkSimple(node, ASTForStatement.class);
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
        node.print();
        checkSimple(node, ASTTryStatement.class);
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
        node.print();
        checkSimple(node, ASTSwitchStatement.class);
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
        node.print();
        checkBinary(node, SWITCH, ASTPrimary.class, ASTSwitchStatementBlock.class);
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
        node.print();
        checkBinary(node, TRY, ASTBlock.class, ASTListNode.class);
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
        node.print();
        checkBinary(node, TRY, ASTBlock.class, ASTFinally.class);
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
        node.print();
        checkBinary(node, TRY, ASTResourceSpecification.class, ASTBlock.class);
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
        node.print();
        checkNary(node, TRY, ASTResourceSpecification.class, ASTBlock.class, ASTListNode.class, ASTFinally.class);
    }

    /**
     * Tests resource specification of resource list.
     */
    @Test
    public void testResourceSpecification() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr))");
        ASTResourceSpecification node = parser.parseResourceSpecification();
        node.print();
        checkSimple(node, ASTListNode.class);
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon() {
        StatementsParser parser = getStatementsParser("(fr; BufferedReader br = new BufferedReader(fr);)");
        ASTResourceSpecification node = parser.parseResourceSpecification();
        node.print();
        checkSimple(node, ASTListNode.class);
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTListNode node = parser.parseResourceList();
        node.print();
        checkList(node, RESOURCES, ASTResource.class, 1);
    }

    /**
     * Tests resource list of nested resource lists (here, just multiple resources).
     */
    @Test
    public void testResourceListNested() {
        StatementsParser parser = getStatementsParser("fr; BufferedReader br = new BufferedReader(fr)");
        ASTListNode node = parser.parseResourceList();
        node.print();
        checkList(node, RESOURCES, ASTResource.class, 2);
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResource node = parser.parseResource();
        node.print();
        checkSimple(node, ASTResourceDeclaration.class);
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName() {
        StatementsParser parser = getStatementsParser("br");
        ASTResource node = parser.parseResource();
        node.print();
        checkSimple(node, ASTListNode.class);
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess() {
        StatementsParser parser = getStatementsParser("super.br");
        ASTResource node = parser.parseResource();
        node.print();
        checkSimple(node, ASTFieldAccess.class);
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration() {
        StatementsParser parser = getStatementsParser("BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        node.print();
        checkTrinary(node, EQUAL, ASTLocalVariableType.class, ASTIdentifier.class, ASTPrimary.class);
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier() {
        StatementsParser parser = getStatementsParser("var BufferedReader br = new BufferedReader()");
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        node.print();
        checkNary(node, EQUAL, ASTListNode.class, ASTLocalVariableType.class, ASTIdentifier.class, ASTPrimary.class);
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
        ASTListNode node = parser.parseCatches();
        node.print();
        checkList(node, CATCH_CLAUSES, ASTCatchClause.class, 2);
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause() {
        StatementsParser parser = getStatementsParser("catch (CompileException ce) { out.println(ce.getMessage()); }");
        ASTCatchClause node = parser.parseCatchClause();
        node.print();
        checkBinary(node, CATCH, ASTCatchFormalParameter.class, ASTBlock.class);
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType() {
        StatementsParser parser = getStatementsParser("Exception");
        ASTListNode node = parser.parseCatchType();
        node.print();
        checkList(node, DATA_TYPES, ASTDataType.class, 1);
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter() {
        StatementsParser parser = getStatementsParser("Exception e");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        node.print();
        checkBinary(node, ASTListNode.class, ASTIdentifier.class);
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers() {
        StatementsParser parser = getStatementsParser("var CustomException ce");
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        node.print();
        checkTrinary(node, null, ASTListNode.class, ASTListNode.class, ASTIdentifier.class);
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType() {
        StatementsParser parser = getStatementsParser("IOException | SQLException");
        ASTListNode node = parser.parseCatchType();
        node.print();
        checkList(node, DATA_TYPES, ASTDataType.class, 2);
    }

    /**
     * Tests nested catch types, here, just a list of data types.
     */
    @Test
    public void testCatchTypeNested() {
        StatementsParser parser = getStatementsParser("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException");
        ASTListNode node = parser.parseCatchType();
        node.print();
        checkList(node, DATA_TYPES, ASTDataType.class, 3);
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
        ASTFinally node = parser.parseFinally();
        node.print();
        checkSimple(node, ASTBlock.class, FINALLY);
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple() {
        StatementsParser parser = getStatementsParser("if (success) { return true; }");
        ASTIfStatement node = parser.parseIfStatement();
        node.print();
        checkBinary(node, IF, ASTPrimary.class, ASTBlock.class);
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
        node.print();
        checkTrinary(node, IF, ASTInit.class, ASTBinaryNode.class, ASTBlock.class);
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
        node.print();
        checkTrinary(node, IF, ASTPrimary.class, ASTBlock.class, ASTBlock.class);
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
        node.print();

        checkTrinary(node, IF, ASTPrimary.class, ASTBlock.class, ASTIfStatement.class);

        ASTIfStatement nestedIf = (ASTIfStatement) node.getChildren().get(2);
        checkTrinary(nestedIf, IF, ASTPrimary.class, ASTBlock.class, ASTBlock.class);
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple() {
        StatementsParser parser = getStatementsParser("while shouldContinue { doWork(); }");
        ASTWhileStatement node = parser.parseWhileStatement();
        node.print();
        checkBinary(node, WHILE, ASTPrimary.class, ASTBlock.class);
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
        node.print();
        checkTrinary(node, WHILE, ASTInit.class, ASTBinaryNode.class, ASTBlock.class);
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement() {
        StatementsParser parser = getStatementsParser("do { work(); } while shouldContinue;");
        ASTDoStatement node = parser.parseDoStatement();
        node.print();
        checkBinary(node, DO, ASTBlock.class, ASTPrimary.class);
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
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        node.print();
        checkNary(basicForStmt, SEMICOLON, ASTInit.class, ASTBinaryNode.class, ASTListNode.class, ASTBlock.class);
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
        node.print();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkSimple(basicForStmt, ASTBlock.class, SEMICOLON);
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
        node.print();
        checkSimple(node, ASTEnhancedForStatement.class, FOR);
        ASTEnhancedForStatement enhForStmt = (ASTEnhancedForStatement) node.getChildren().get(0);
        checkTrinary(enhForStmt, COLON, ASTLocalVariableDeclaration.class, ASTPrimary.class, ASTBlock.class);
    }

    /**
     * Tests yield statement.
     */
    @Test
    public void testYieldStatement() {
        StatementsParser parser = getStatementsParser("yield x.y + 2;");
        ASTYieldStatement node = parser.parseYieldStatement();
        node.print();
        checkSimple(node, ASTBinaryNode.class, YIELD);
    }

    /**
     * Tests use statement.
     */
    @Test
    public void testUseStatement() {
        StatementsParser parser = getStatementsParser("use x.y + 2;");
        ASTUseStatement node = parser.parseUseStatement();
        node.print();
        checkSimple(node, ASTBinaryNode.class, USE);
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement() {
        StatementsParser parser = getStatementsParser("return;");
        ASTReturnStatement node = parser.parseReturnStatement();
        node.print();
        checkEmpty(node, RETURN);
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression() {
        StatementsParser parser = getStatementsParser("return x.y + 2;");
        ASTReturnStatement node = parser.parseReturnStatement();
        node.print();
        checkSimple(node, ASTBinaryNode.class, RETURN);
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression() {
        StatementsParser parser = getStatementsParser("throw new Exception();");
        ASTThrowStatement node = parser.parseThrowStatement();
        node.print();
        checkSimple(node, ASTPrimary.class, THROW);
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement() {
        StatementsParser parser = getStatementsParser("break;");
        ASTBreakStatement node = parser.parseBreakStatement();
        node.print();
        checkEmpty(node, BREAK);
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement() {
        StatementsParser parser = getStatementsParser("continue;");
        ASTContinueStatement node = parser.parseContinueStatement();
        node.print();
        checkEmpty(node, CONTINUE);
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement() {
        StatementsParser parser = getStatementsParser("fallthrough;");
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        node.print();
        checkEmpty(node, FALLTHROUGH);
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementOfExpression() {
        StatementsParser parser = getStatementsParser("assert result == true;");
        ASTAssertStatement node = parser.parseAssertStatement();
        node.print();
        checkSimple(node, ASTBinaryNode.class, ASSERT);
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions() {
        StatementsParser parser = getStatementsParser("assert result == true : \"Assertion failed!\";");
        ASTAssertStatement node = parser.parseAssertStatement();
        node.print();
        checkBinary(node, ASSERT, ASTBinaryNode.class, ASTPrimary.class);
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression() {
        StatementsParser parser = getStatementsParser("x++;");
        ASTExpressionStatement node = parser.parseExpressionStatement();
        node.print();
        checkSimple(node, ASTStatementExpression.class, SEMICOLON);
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration() {
        StatementsParser parser = getStatementsParser("Int i = 0, j = 0");
        ASTInit node = parser.parseInit();
        node.print();
        checkSimple(node, ASTLocalVariableDeclaration.class);
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTInit node = parser.parseInit();
        node.print();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkSimple(list, ASTStatementExpression.class, COMMA);
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTInit node = parser.parseInit();
        node.print();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkList(list, COMMA, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression() {
        StatementsParser parser = getStatementsParser("i = 0");
        ASTListNode node = parser.parseStatementExpressionList();
        node.print();
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 1);
    }

    /**
     * Tests statement expression lists of nested statement expression lists
     * (here, just multiple statement expressions).
     */
    @Test
    public void testStatementExpressionListNested() {
        StatementsParser parser = getStatementsParser("i = 0, j = 0, k = 1");
        ASTListNode node = parser.parseStatementExpressionList();
        node.print();
        checkList(node, STMT_EXPRS, ASTStatementExpression.class, 3);
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment() {
        StatementsParser parser = getStatementsParser("x = 0");
        ASTStatementExpression node = parser.parseStatementExpression();
        node.print();
        checkSimple(node, ASTAssignment.class);
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression() {
        StatementsParser parser = getStatementsParser("x.y++");
        ASTStatementExpression node = parser.parseStatementExpression();
        node.print();
        checkSimple(node, ASTPostfix.class);
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation() {
        StatementsParser parser = getStatementsParser("x.y(2)");
        ASTStatementExpression node = parser.parseStatementExpression();
        node.print();
        checkSimple(node, ASTMethodInvocation.class);
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression() {
        StatementsParser parser = getStatementsParser("new SideEffect()");
        ASTStatementExpression node = parser.parseStatementExpression();
        node.print();
        checkSimple(node, ASTUnqualifiedClassInstanceCreationExpression.class);
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
        node.print();
        checkBinary(node, PLUS_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, MINUS_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, STAR_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, SLASH_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, PERCENT_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, SHIFT_LEFT_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, SHIFT_RIGHT_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, PIPE_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, AMPERSAND_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
        node.print();
        checkBinary(node, CARET_EQUALS, ASTLeftHandSide.class, ASTPrimary.class);
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
