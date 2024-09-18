package org.spruce.compiler.test;

import org.spruce.compiler.ast.expressions.*;
import org.spruce.compiler.ast.names.*;
import org.spruce.compiler.ast.statements.*;
import org.spruce.compiler.ast.types.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.parser.StatementsParser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

import org.junit.jupiter.api.Test;

/**
 * All tests for the parser related to statements.
 */
public class ParserStatementsTest {

    @Test
    public void testNestedBlocks() {
        StatementsParser parser = new Parser(new Scanner("""
            {
                {
                    Int a = 1;
                }
            }
            """)).getStatementsParser();
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
        ASTBlockStatements blockStmts = (ASTBlockStatements) node.getChildren().get(0);
        checkSimple(blockStmts, ASTBlockStatement.class);
        ASTBlockStatement blockStmt = (ASTBlockStatement) blockStmts.getChildren().get(0);
        checkSimple(blockStmt, ASTStatement.class);
        ASTStatement stmt = (ASTStatement) blockStmt.getChildren().get(0);
        checkSimple(stmt, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block of empty braces.
     */
    @Test
    public void testBlockOfNothing() {
        StatementsParser parser = new Parser(new Scanner("{}")).getStatementsParser();
        ASTBlock node = parser.parseBlock();
        checkEmpty(node, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Tests block of block statements.
     */
    @Test
    public void testBlockOfBlockStatements() {
        StatementsParser parser = new Parser(new Scanner("""
            {
                Integer a = 1;
                Integer b = 2;
                return a + b;
            }
            """)).getStatementsParser();
        ASTBlock node = parser.parseBlock();
        checkSimple(node, ASTBlockStatements.class, OPEN_BRACE);
        node.collapseThenPrint();
    }

    /**
     * Test block statements of block statement instances.
     */
    @Test
    public void testBlockStatements() {
        StatementsParser parser = new Parser(new Scanner("""
            String stmt = "Statement one!";
            Integer stmt2Nbr = 2;
            i++;}
            """)).getStatementsParser();
        ASTBlockStatements node = parser.parseBlockStatements();
        checkList(node, null, ASTBlockStatement.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of modifier and local variable declaration.
     */
    @Test
    public void testBlockStatementOfModifierDeclaration() {
        StatementsParser parser = new Parser(new Scanner("Integer i = 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of local variable declaration.
     */
    @Test
    public void testBlockStatementOfDeclaration() {
        StatementsParser parser = new Parser(new Scanner("Integer i = 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTLocalVariableDeclarationStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of assignment.
     */
    @Test
    public void testBlockStatementOfAssignment() {
        StatementsParser parser = new Parser(new Scanner("i = 1;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of method invocation.
     */
    @Test
    public void testBlockStatementOfMethodInvocation() {
        StatementsParser parser = new Parser(new Scanner("i(j);")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of qualified class instance creation expression.
     */
    @Test
    public void testBlockStatementOfCICE() {
        StatementsParser parser = new Parser(new Scanner("i.new J();")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests block statement of return statement.
     */
    @Test
    public void testBlockStatementOfReturn() {
        StatementsParser parser = new Parser(new Scanner("return true;")).getStatementsParser();
        ASTBlockStatement node = parser.parseBlockStatement();
        checkSimple(node, ASTStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatement() {
        StatementsParser parser = new Parser(new Scanner("Integer[] values = {1, 2, 3};")).getStatementsParser();
        ASTLocalVariableDeclarationStatement node = parser.parseLocalVariableDeclarationStatement();
        checkSimple(node, ASTLocalVariableDeclaration.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests bad local variable declaration statement.
     */
    @Test
    public void testLocalVariableDeclarationStatementBad() {
        StatementsParser parser = new Parser(new Scanner("Integer[] values := {1, 2, 3};")).getStatementsParser();
        assertThrows(CompileException.class, parser::parseLocalVariableDeclarationStatement, "Error: Use '=' for assignment, not ':='.");
    }

    /**
     * Tests local variable declaration without modifiers.
     */
    @Test
    public void testLocalVariableDeclaration() {
        StatementsParser parser = new Parser(new Scanner("Boolean result = true, done = false")).getStatementsParser();
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkBinary(node, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable declaration with modifiers.
     */
    @Test
    public void testLocalVariableDeclarationOfModifiers() {
        StatementsParser parser = new Parser(new Scanner("mut Boolean result = true, var done = false")).getStatementsParser();
        ASTLocalVariableDeclaration node = parser.parseLocalVariableDeclaration();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTVariableDeclaratorList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier list of variable modifier.
     */
    @Test
    public void testVariableModifierListOfVariableModifier() {
        StatementsParser parser = new Parser(new Scanner("var")).getStatementsParser();
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkSimple(node, ASTVariableModifier.class);
        node.collapseThenPrint();
    }
    /**
     * Tests variable modifier list of variable modifiers.
     */
    @Test
    public void testVariableModifierListOfVariableModifiers() {
        StatementsParser parser = new Parser(new Scanner("var mut")).getStatementsParser();
        ASTVariableModifierList node = parser.parseVariableModifierList();
        checkList(node, null, ASTVariableModifier.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier of "var".
     */
    @Test
    public void testVariableModifierOfVar() {
        StatementsParser parser = new Parser(new Scanner("var")).getStatementsParser();
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, VAR);
        node.collapseThenPrint();
    }

    /**
     * Tests variable modifier of "mut".
     */
    @Test
    public void testVariableModifierOfMut() {
        StatementsParser parser = new Parser(new Scanner("mut")).getStatementsParser();
        ASTVariableModifier node = parser.parseVariableModifier();
        checkEmpty(node, MUT);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator list of variable declarator.
     */
    @Test
    public void testVariableDeclaratorListOfVariableDeclarator() {
        StatementsParser parser = new Parser(new Scanner("a = b")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkSimple(node, ASTVariableDeclarator.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator list.
     */
    @Test
    public void testVariableDeclaratorList() {
        StatementsParser parser = new Parser(new Scanner("x = 1, y = x")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested variable declarator lists.
     */
    @Test
    public void testVariableDeclaratorListNested() {
        StatementsParser parser = new Parser(new Scanner("a = 1, b = a + 1, c = 2 * b")).getStatementsParser();
        ASTVariableDeclaratorList node = parser.parseVariableDeclaratorList();
        checkList(node, COMMA, ASTVariableDeclarator.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator of identifier.
     */
    @Test
    public void testVariableDeclaratorOfIdentifier() {
        StatementsParser parser = new Parser(new Scanner("varName")).getStatementsParser();
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkSimple(node, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests variable declarator of identifier and variable initializer.
     */
    @Test
    public void testVariableDeclaratorOfIdentifierVariableInitializer() {
        StatementsParser parser = new Parser(new Scanner("count = 2")).getStatementsParser();
        ASTVariableDeclarator node = parser.parseVariableDeclarator();
        checkBinary(node, EQUAL, ASTIdentifier.class, ASTVariableInitializer.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable type of data type.
     */
    @Test
    public void testLocalVariableTypeOfDataType() {
        StatementsParser parser = new Parser(new Scanner("spruce.lang.String[][])")).getStatementsParser();
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkSimple(node, ASTDataType.class);
        node.collapseThenPrint();
    }

    /**
     * Tests local variable type of "var".
     */
    @Test
    public void testLocalVariableTypeOfVar() {
        StatementsParser parser = new Parser(new Scanner("var")).getStatementsParser();
        ASTLocalVariableType node = parser.parseLocalVariableType();
        checkEmpty(node, VAR);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of block.
     */
    @Test
    public void testStatementOfBlock() {
        StatementsParser parser = new Parser(new Scanner("{x = x + 1;}")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of expression statement.
     */
    @Test
    public void testStatementOfExpressionStatement() {
        StatementsParser parser = new Parser(new Scanner("x = x + 1;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTExpressionStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of return statement.
     */
    @Test
    public void testStatementOfReturnStatement() {
        StatementsParser parser = new Parser(new Scanner("return true;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTReturnStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of throw statement.
     */
    @Test
    public void testStatementOfThrowStatement() {
        StatementsParser parser = new Parser(new Scanner("throw new CompileException(\"Error message\");")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTThrowStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfBreakStatement() {
        StatementsParser parser = new Parser(new Scanner("break;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTBreakStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfContinueStatement() {
        StatementsParser parser = new Parser(new Scanner("continue;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTContinueStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of break statement.
     */
    @Test
    public void testStatementOfFallthroughStatement() {
        StatementsParser parser = new Parser(new Scanner("fallthrough;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTFallthroughStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of assert statement.
     */
    @Test
    public void testStatementOfAssertStatement() {
        StatementsParser parser = new Parser(new Scanner("assert status == true;")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTAssertStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of if statement.
     */
    @Test
    public void testStatementOfIfStatement() {
        StatementsParser parser = new Parser(new Scanner("if (success) { return true; }")).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTIfStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of while statement without braces.
     */
    @Test
    public void testStatementOfWhileStatementNoBlock() {
        StatementsParser parser = new Parser(new Scanner("while (shouldContinue) doWork();")).getStatementsParser();
        assertThrows(CompileException.class, parser::parseStatement, "Error: Expected '{'.");
    }

    /**
     * Tests statement of while statement.
     */
    @Test
    public void testStatementOfWhileStatement() {
        StatementsParser parser = new Parser(new Scanner("""
            while (shouldContinue) {
                doWork();
            }
            """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTWhileStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of do statement.
     */
    @Test
    public void testStatementOfDoStatement() {
        StatementsParser parser = new Parser(new Scanner("""
            do {
                work();
            } while shouldContinue;
            """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTDoStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of critical statement.
     */
    @Test
    public void testStatementOfCriticalStatement() {
        StatementsParser parser = new Parser(new Scanner("""
            critical myLock {
                myLock.wait();
            }
        """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTCriticalStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of for statement, no block.
     */
    @Test
    public void testStatementOfForStatementNoBlock() {
        StatementsParser parser = new Parser(new Scanner("for (;;) doWork();")).getStatementsParser();
        assertThrows(CompileException.class, parser::parseStatement, "Expected '{'.");
    }

    /**
     * Tests statement of for statement.
     */
    @Test
    public void testStatementOfForStatement() {
        StatementsParser parser = new Parser(new Scanner("""
                for (;;) {
                    doWork();
                }
                """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTForStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of try statement.
     */
    @Test
    public void testStatementOfTryStatement() {
        StatementsParser parser = new Parser(new Scanner("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTTryStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement of switch statement.
     */
    @Test
    public void testStatementOfSwitchStatement() {
        StatementsParser parser = new Parser(new Scanner("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """)).getStatementsParser();
        ASTStatement node = parser.parseStatement();
        checkSimple(node, ASTSwitchStatement.class);
        node.collapseThenPrint();
    }

    /**
     * Tests switch statement.
     */
    @Test
    public void testSwitchStatement() {
        StatementsParser parser = new Parser(new Scanner("""
                switch code {
                case 1 -> out.println("One");
                case 2 -> out.println("Two");
                default -> out.println("Unexpected");
                }
                """)).getStatementsParser();
        ASTSwitchStatement node = parser.parseSwitchStatement();
        checkBinary(node, SWITCH, ASTExpression.class, ASTSwitchStatementBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests empty switch block.
     */
    @Test
    public void testSwitchBlockEmpty() {
        StatementsParser parser = new Parser(new Scanner("{}")).getStatementsParser();
        assertThrows(CompileException.class, parser::parseSwitchStatementBlock, "Error at code \"{}\".");
    }

    /**
     * Tests try statement of catch.
     */
    @Test
    public void testTryStatementOfCatch() {
        StatementsParser parser = new Parser(new Scanner("""
            try {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            }
            """)).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTCatches.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of finally.
     */
    @Test
    public void testTryStatementOfFinally() {
        StatementsParser parser = new Parser(new Scanner("""
                try {
                    br.readLine();
                } finally {
                    br.close();
                }
                """)).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTBlock.class, ASTFinally.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of resource specification.
     */
    @Test
    public void testTryStatementOfResourceSpecification() {
        StatementsParser parser = new Parser(new Scanner("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            }
            """)).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkBinary(node, TRY, ASTResourceSpecification.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests try statement of all optionals.
     */
    @Test
    public void testTryStatementOfAll() {
        StatementsParser parser = new Parser(new Scanner("""
            try (BufferedReader br = new BufferedReader()) {
                br.readLine();
            } catch (IOException e) {
                out.println(e.getMessage());
            } finally {
                br.close();
            }
            """)).getStatementsParser();
        ASTTryStatement node = parser.parseTryStatement();
        checkNary(node, TRY, ASTResourceSpecification.class, ASTBlock.class, ASTCatches.class, ASTFinally.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource specification of resource list.
     */
    @Test
    public void testResourceSpecification() {
        StatementsParser parser = new Parser(new Scanner("(fr; BufferedReader br = new BufferedReader(fr))")).getStatementsParser();
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource specification of resource list and semicolon.
     */
    @Test
    public void testResourceSpecificationSemicolon() {
        StatementsParser parser = new Parser(new Scanner("(fr; BufferedReader br = new BufferedReader(fr);)")).getStatementsParser();
        ASTResourceSpecification node = parser.parseResourceSpecification();
        checkSimple(node, ASTResourceList.class);
        node.collapseThenPrint();
    }

    /**
     * Tests resource list of resource.
     */
    @Test
    public void testResourceListOfResource() {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br = new BufferedReader()")).getStatementsParser();
        ASTResourceList node = parser.parseResourceList();
        checkSimple(node, ASTResource.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests resource list of nested resource lists (here, just multiple resources).
     */
    @Test
    public void testResourceListNested() {
        StatementsParser parser = new Parser(new Scanner("fr; BufferedReader br = new BufferedReader(fr)")).getStatementsParser();
        ASTResourceList node = parser.parseResourceList();
        checkList(node, SEMICOLON, ASTResource.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Test resource of resource declaration.
     */
    @Test
    public void testResourceOfResourceDeclaration() {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br = new BufferedReader()")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTResourceDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource of expression name.
     */
    @Test
    public void testResourceOfExpressionName() {
        StatementsParser parser = new Parser(new Scanner("br")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTExpressionName.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource of field access.
     */
    @Test
    public void testResourceOfFieldAccess() {
        StatementsParser parser = new Parser(new Scanner("super.br")).getStatementsParser();
        ASTResource node = parser.parseResource();
        checkSimple(node, ASTFieldAccess.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource declaration, no variable modifiers.
     */
    @Test
    public void testResourceDeclaration() {
        StatementsParser parser = new Parser(new Scanner("BufferedReader br = new BufferedReader()")).getStatementsParser();
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkTrinary(node, EQUAL, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Test resource declaration, with variable modifiers.
     */
    @Test
    public void testResourceDeclarationOfVariableModifier() {
        StatementsParser parser = new Parser(new Scanner("var BufferedReader br = new BufferedReader()")).getStatementsParser();
        ASTResourceDeclaration node = parser.parseResourceDeclaration();
        checkNary(node, EQUAL, ASTVariableModifierList.class, ASTLocalVariableType.class, ASTIdentifier.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Test catches of catch clauses.
     */
    @Test
    public void testCatches() {
        StatementsParser parser = new Parser(new Scanner("""
            catch (FileNotFoundException e) {
                err.println(e.getMessage());
            }
            catch (IOException e) {
                out.println(e.getMessage());
            }
            """)).getStatementsParser();
        ASTCatches node = parser.parseCatches();
        checkList(node, null, ASTCatchClause.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests catch clause.
     */
    @Test
    public void testCatchClause() {
        StatementsParser parser = new Parser(new Scanner("catch (CompileException ce) { out.println(ce.getMessage()); }")).getStatementsParser();
        ASTCatchClause node = parser.parseCatchClause();
        checkBinary(node, CATCH, ASTCatchFormalParameter.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch type of data type.
     */
    @Test
    public void testCatchTypeOfDataType() {
        StatementsParser parser = new Parser(new Scanner("Exception")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkSimple(node, ASTDataType.class, PIPE);
        node.collapseThenPrint();
    }

    /**
     * Tests catch formal parameter without modifiers.
     */
    @Test
    public void testCatchFormalParameter() {
        StatementsParser parser = new Parser(new Scanner("Exception e")).getStatementsParser();
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkBinary(node, ASTCatchType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch formal parameter with modifiers.
     */
    @Test
    public void testCatchFormalParameterOfModifiers() {
        StatementsParser parser = new Parser(new Scanner("var CustomException ce")).getStatementsParser();
        ASTCatchFormalParameter node = parser.parseCatchFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTCatchType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests catch type.
     */
    @Test
    public void testCatchType() {
        StatementsParser parser = new Parser(new Scanner("IOException | SQLException")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkList(node, PIPE, ASTDataType.class, 2);
        node.collapseThenPrint();
    }

    /**
     * Tests nested catch types, here, just a list of data types.
     */
    @Test
    public void testCatchTypeNested() {
        StatementsParser parser = new Parser(new Scanner("ArrayIndexOutOfBoundsException | NullPointerException | IllegalArgumentException")).getStatementsParser();
        ASTCatchType node = parser.parseCatchType();
        checkList(node, PIPE, ASTDataType.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests finally block.
     */
    @Test
    public void testFinally() {
        StatementsParser parser = new Parser(new Scanner("""
                finally {
                    out.println("Always executed!");
                }
                """)).getStatementsParser();
        ASTFinally node = parser.parseFinally();
        checkSimple(node, ASTBlock.class, FINALLY);
        node.collapseThenPrint();
    }

    /**
     * Tests simple if statement.
     */
    @Test
    public void testIfStatementOfSimple() {
        StatementsParser parser = new Parser(new Scanner("if (success) { return true; }")).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkBinary(node, IF, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests if statement with init no block.
     */
    @Test
    public void testIfStatementOfInitNoBlock() {
        StatementsParser parser = new Parser(new Scanner("""
                if {String line = br.readLine()} (line != null) out.println(line);
                """)).getStatementsParser();
        assertThrows(CompileException.class, parser::parseIfStatement, "Expected '{'.");
    }

    /**
     * Tests if statement with init.
     */
    @Test
    public void testIfStatementOfInit() {
        StatementsParser parser = new Parser(new Scanner("""
                if {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """)).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTInit.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests if statement with else.
     */
    @Test
    public void testIfStatementOfElse() {
        StatementsParser parser = new Parser(new Scanner("""
            if result {
                out.println("Test passed.");
            } else {
                out.println("Test FAILED!");
            }
            """)).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTConditionalExpression.class, ASTBlock.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests nested if statements (if/else if/else).
     */
    @Test
    public void testIfStatementNested() {
        StatementsParser parser = new Parser(new Scanner("""
            if result {
                out.println("Test passed.");
            } else if DEBUG {
                out.println("Test failed in debug mode!");
            } else {
                out.println("Test FAILED!");
            }
            """)).getStatementsParser();
        ASTIfStatement node = parser.parseIfStatement();
        checkTrinary(node, IF, ASTConditionalExpression.class, ASTBlock.class, ASTIfStatement.class);

        ASTIfStatement nestedIf = (ASTIfStatement) node.getChildren().get(2);
        checkTrinary(nestedIf, IF, ASTConditionalExpression.class, ASTBlock.class, ASTBlock.class);

        node.collapseThenPrint();
    }

    /**
     * Tests simple while statement.
     */
    @Test
    public void testWhileStatementOfSimple() {
        StatementsParser parser = new Parser(new Scanner("while shouldContinue { doWork(); }")).getStatementsParser();
        ASTWhileStatement node = parser.parseWhileStatement();
        checkBinary(node, WHILE, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests while statement with init without block.
     */
    @Test
    public void testWhileStatementOfInitNoBlock() {
        StatementsParser parser = new Parser(new Scanner("""
                while {String line = br.readLine()} line != null out.println(line);
                """)).getStatementsParser();
        assertThrows(CompileException.class, parser::parseWhileStatement, "Expected '{'.");
    }

    /**
     * Tests while statement with init.
     */
    @Test
    public void testWhileStatementOfInit() {
        StatementsParser parser = new Parser(new Scanner("""
                while {String line = br.readLine()} line != null {
                    out.println(line);
                }
                """)).getStatementsParser();
        ASTWhileStatement node = parser.parseWhileStatement();
        checkTrinary(node, WHILE, ASTInit.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests do statement.
     */
    @Test
    public void testDoStatement() {
        StatementsParser parser = new Parser(new Scanner("do { work(); } while shouldContinue;")).getStatementsParser();
        ASTDoStatement node = parser.parseDoStatement();
        checkBinary(node, DO, ASTBlock.class, ASTConditionalExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of all 3 parts.
     */
    @Test
    public void testForStatementOfBasicForStatementAll3() {
        StatementsParser parser = new Parser(new Scanner("""
                for (Int i = 0; i < 10; i++) {
                    out.println(i);
                }
                """)).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkNary(basicForStmt, SEMICOLON, ASTInit.class, ASTConditionalExpression.class, ASTStatementExpressionList.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of basic for statement of infinite loop.
     */
    @Test
    public void testForStatementOfBasicForStatementInfiniteLoop() {
        StatementsParser parser = new Parser(new Scanner("""
        for (;;) {
            out.println("Hello world!");
        }
        """)).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTBasicForStatement.class, FOR);
        ASTBasicForStatement basicForStmt = (ASTBasicForStatement) node.getChildren().get(0);
        checkSimple(basicForStmt, ASTBlock.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests for statement of enhanced for statement.
     */
    @Test
    public void testForStatementOfEnhancedForStatement() {
        StatementsParser parser = new Parser(new Scanner("""
                for (Int i : array) {
                    sum += i;
                }
                """)).getStatementsParser();
        ASTForStatement node = parser.parseForStatement();
        checkSimple(node, ASTEnhancedForStatement.class, FOR);
        ASTEnhancedForStatement enhForStmt = (ASTEnhancedForStatement) node.getChildren().get(0);
        checkTrinary(enhForStmt, COLON, ASTLocalVariableDeclaration.class, ASTConditionalExpression.class, ASTBlock.class);
        node.collapseThenPrint();
    }

    /**
     * Tests yield statement.
     */
    @Test
    public void testYieldStatement() {
        StatementsParser parser = new Parser(new Scanner("yield x.y + 2;")).getStatementsParser();
        ASTYieldStatement node = parser.parseYieldStatement();
        checkSimple(node, ASTExpression.class, YIELD);
        node.collapseThenPrint();
    }

    /**
     * Tests use statement.
     */
    @Test
    public void testUseStatement() {
        StatementsParser parser = new Parser(new Scanner("use x.y + 2;")).getStatementsParser();
        ASTUseStatement node = parser.parseUseStatement();
        checkSimple(node, ASTExpression.class, USE);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement.
     */
    @Test
    public void testReturnStatement() {
        StatementsParser parser = new Parser(new Scanner("return;")).getStatementsParser();
        ASTReturnStatement node = parser.parseReturnStatement();
        checkEmpty(node, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests return statement with expression.
     */
    @Test
    public void testReturnStatementOfExpression() {
        StatementsParser parser = new Parser(new Scanner("return x.y + 2;")).getStatementsParser();
        ASTReturnStatement node = parser.parseReturnStatement();
        checkSimple(node, ASTExpression.class, RETURN);
        node.collapseThenPrint();
    }

    /**
     * Tests throw statement with expression.
     */
    @Test
    public void testThrowStatementOfExpression() {
        StatementsParser parser = new Parser(new Scanner("throw new Exception();")).getStatementsParser();
        ASTThrowStatement node = parser.parseThrowStatement();
        checkSimple(node, ASTExpression.class, THROW);
        node.collapseThenPrint();
    }

    /**
     * Tests break statement.
     */
    @Test
    public void testBreakStatement() {
        StatementsParser parser = new Parser(new Scanner("break;")).getStatementsParser();
        ASTBreakStatement node = parser.parseBreakStatement();
        checkEmpty(node, BREAK);
        node.collapseThenPrint();
    }

    /**
     * Tests continue statement.
     */
    @Test
    public void testContinueStatement() {
        StatementsParser parser = new Parser(new Scanner("continue;")).getStatementsParser();
        ASTContinueStatement node = parser.parseContinueStatement();
        checkEmpty(node, CONTINUE);
        node.collapseThenPrint();
    }

    /**
     * Tests fallthrough statement.
     */
    @Test
    public void testFallthroughStatement() {
        StatementsParser parser = new Parser(new Scanner("fallthrough;")).getStatementsParser();
        ASTFallthroughStatement node = parser.parseFallthroughStatement();
        checkEmpty(node, FALLTHROUGH);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of expression.
     */
    @Test
    public void testAssertStatementOfExpression() {
        StatementsParser parser = new Parser(new Scanner("assert result == true;")).getStatementsParser();
        ASTAssertStatement node = parser.parseAssertStatement();
        checkSimple(node, ASTExpression.class, ASSERT);
        node.collapseThenPrint();
    }

    /**
     * Tests assert statement of 2 expressions.
     */
    @Test
    public void testAssertStatementOfTwoExpressions() {
        StatementsParser parser = new Parser(new Scanner("assert result == true : \"Assertion failed!\";")).getStatementsParser();
        ASTAssertStatement node = parser.parseAssertStatement();
        checkBinary(node, ASSERT, ASTExpression.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests expression statement of statement expression.
     */
    @Test
    public void testExpressionStatementOfStatementExpression() {
        StatementsParser parser = new Parser(new Scanner("x++;")).getStatementsParser();
        ASTExpressionStatement node = parser.parseExpressionStatement();
        checkSimple(node, ASTStatementExpression.class, SEMICOLON);
        node.collapseThenPrint();
    }

    /**
     * Tests init of local variable declaration.
     */
    @Test
    public void testInitOfLocalVariableDeclaration() {
        StatementsParser parser = new Parser(new Scanner("Int i = 0, j = 0")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTLocalVariableDeclaration.class);
        node.collapseThenPrint();
    }

    /**
     * Tests init of statement expression.
     */
    @Test
    public void testInitOfStatementExpression() {
        StatementsParser parser = new Parser(new Scanner("i = 0")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkSimple(list, ASTStatementExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests init of statement expression list.
     */
    @Test
    public void testInitOfStatementExpressionList() {
        StatementsParser parser = new Parser(new Scanner("i = 0, j = 0, k = 1")).getStatementsParser();
        ASTInit node = parser.parseInit();
        checkSimple(node, ASTStatementExpressionList.class);
        ASTStatementExpressionList list = (ASTStatementExpressionList) node.getChildren().get(0);
        checkList(list, COMMA, ASTStatementExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression list of statement expression.
     */
    @Test
    public void testStatementExpressionListOfStatementExpression() {
        StatementsParser parser = new Parser(new Scanner("i = 0")).getStatementsParser();
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkSimple(node, ASTStatementExpression.class, COMMA);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression lists of nested statement expression lists
     * (here, just multiple statement expressions).
     */
    @Test
    public void testStatementExpressionListNested() {
        StatementsParser parser = new Parser(new Scanner("i = 0, j = 0, k = 1")).getStatementsParser();
        ASTStatementExpressionList node = parser.parseStatementExpressionList();
        checkList(node, COMMA, ASTStatementExpression.class, 3);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of assignment.
     */
    @Test
    public void testStatementExpressionOfAssignment() {
        StatementsParser parser = new Parser(new Scanner("x = 0")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTAssignment.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of postfix expression.
     */
    @Test
    public void testStatementExpressionOfPostfixExpression() {
        StatementsParser parser = new Parser(new Scanner("x.y++")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTPostfix.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of method invocation.
     */
    @Test
    public void testStatementExpressionOfMethodInvocation() {
        StatementsParser parser = new Parser(new Scanner("x.y(2)")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTMethodInvocation.class);
        node.collapseThenPrint();
    }

    /**
     * Tests statement expression of class instance creation expression.
     */
    @Test
    public void testStatementExpressionOfClassInstanceCreationExpression() {
        StatementsParser parser = new Parser(new Scanner("new SideEffect()")).getStatementsParser();
        ASTStatementExpression node = parser.parseStatementExpression();
        checkSimple(node, ASTClassInstanceCreationExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "+=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPlusEquals() {
        StatementsParser parser = new Parser(new Scanner("a += 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PLUS_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "-=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfMinusEquals() {
        StatementsParser parser = new Parser(new Scanner("a -= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, MINUS_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "*=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfStarEquals() {
        StatementsParser parser = new Parser(new Scanner("a *= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, STAR_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "/=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfSlashEquals() {
        StatementsParser parser = new Parser(new Scanner("a /= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SLASH_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "%=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfPercentEquals() {
        StatementsParser parser = new Parser(new Scanner("a %= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PERCENT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "<<=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftLeftEquals() {
        StatementsParser parser = new Parser(new Scanner("a <<= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SHIFT_LEFT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, ">>=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfShiftRightEquals() {
        StatementsParser parser = new Parser(new Scanner("a >>= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, SHIFT_RIGHT_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "|=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfOrEquals() {
        StatementsParser parser = new Parser(new Scanner("a |= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, PIPE_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "&=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfAndEquals() {
        StatementsParser parser = new Parser(new Scanner("a &= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, AMPERSAND_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }

    /**
     * Tests assignment expression of assignment made of lhs, "^=", and
     * assignment expression.
     */
    @Test
    public void testAssignmentExpressionOfXorEquals() {
        StatementsParser parser = new Parser(new Scanner("a ^= 1")).getStatementsParser();
        ASTLeftHandSide lhs = parser.getExpressionsParser().parseLeftHandSide();
        ASTAssignment node = parser.parseAssignment(lhs.getLocation(), lhs);
        checkBinary(node, CARET_EQUALS, ASTLeftHandSide.class, ASTExpression.class);
        node.collapseThenPrint();
    }
}
